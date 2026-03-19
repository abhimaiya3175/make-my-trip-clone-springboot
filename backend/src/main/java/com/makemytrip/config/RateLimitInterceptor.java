package com.makemytrip.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final long STALE_ENTRY_TTL_MILLIS = Duration.ofHours(1).toMillis();
    private static final long CLEANUP_INTERVAL_MILLIS = Duration.ofMinutes(1).toMillis();
    private static final int MAX_TRACKED_KEYS = 10_000;

    private final Map<String, Bucket> reviewBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> feedbackBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> signupBuckets = new ConcurrentHashMap<>();
    private final Map<String, Long> lastAccess = new ConcurrentHashMap<>();
    private final AtomicLong lastCleanupAt = new AtomicLong(0);

    @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {
        cleanupIfNeeded();

        String path = request.getRequestURI();
        String method = request.getMethod();
        String key = resolveRateLimitKey(request);
        lastAccess.put(key, System.currentTimeMillis());

        // Rate limit review submit: 5/min/user
        if ("POST".equalsIgnoreCase(method) && "/api/reviews".equals(path)) {
            Bucket bucket = reviewBuckets.computeIfAbsent(key, k -> Bucket.builder()
                    .addLimit(Bandwidth.builder().capacity(5).refillGreedy(5, Duration.ofMinutes(1)).build())
                    .build());
            if (!bucket.tryConsume(1)) {
                writeTooManyRequests(response, "Review submission rate limit exceeded. Max 5 per minute.");
                return false;
            }
        }

        // Rate limit recommendation feedback: 20/min/user
        if ("POST".equalsIgnoreCase(method) && "/api/recommendations/feedback".equals(path)) {
            Bucket bucket = feedbackBuckets.computeIfAbsent(key, k -> Bucket.builder()
                    .addLimit(Bandwidth.builder().capacity(20).refillGreedy(20, Duration.ofMinutes(1)).build())
                    .build());
            if (!bucket.tryConsume(1)) {
                writeTooManyRequests(response, "Feedback rate limit exceeded. Max 20 per minute.");
                return false;
            }
        }

        // Rate limit auth login: 10/min/client
        if ("POST".equalsIgnoreCase(method) && "/user/login".equals(path)) {
            Bucket bucket = loginBuckets.computeIfAbsent(key, k -> Bucket.builder()
                    .addLimit(Bandwidth.builder().capacity(10).refillGreedy(10, Duration.ofMinutes(1)).build())
                    .build());
            if (!bucket.tryConsume(1)) {
                writeTooManyRequests(response, "Login rate limit exceeded. Max 10 per minute.");
                return false;
            }
        }

        // Rate limit auth signup: 5/min/client
        if ("POST".equalsIgnoreCase(method) && "/user/signup".equals(path)) {
            Bucket bucket = signupBuckets.computeIfAbsent(key, k -> Bucket.builder()
                    .addLimit(Bandwidth.builder().capacity(5).refillGreedy(5, Duration.ofMinutes(1)).build())
                    .build());
            if (!bucket.tryConsume(1)) {
                writeTooManyRequests(response, "Signup rate limit exceeded. Max 5 per minute.");
                return false;
            }
        }

        return true;
    }

    private void cleanupIfNeeded() {
        long now = System.currentTimeMillis();
        long previous = lastCleanupAt.get();
        if (now - previous < CLEANUP_INTERVAL_MILLIS) {
            return;
        }
        if (!lastCleanupAt.compareAndSet(previous, now)) {
            return;
        }

        long staleCutoff = now - STALE_ENTRY_TTL_MILLIS;
        lastAccess.entrySet().removeIf(entry -> entry.getValue() < staleCutoff);

        reviewBuckets.keySet().removeIf(key -> !lastAccess.containsKey(key));
        feedbackBuckets.keySet().removeIf(key -> !lastAccess.containsKey(key));
        loginBuckets.keySet().removeIf(key -> !lastAccess.containsKey(key));
        signupBuckets.keySet().removeIf(key -> !lastAccess.containsKey(key));

        if (lastAccess.size() <= MAX_TRACKED_KEYS) {
            return;
        }

        int removeCount = lastAccess.size() - MAX_TRACKED_KEYS;
        lastAccess.entrySet().stream()
                .sorted(Comparator.comparingLong(Map.Entry::getValue))
                .limit(removeCount)
                .map(Map.Entry::getKey)
                .toList()
                .forEach(this::removeClientState);
    }

    private void removeClientState(String key) {
        lastAccess.remove(key);
        reviewBuckets.remove(key);
        feedbackBuckets.remove(key);
        loginBuckets.remove(key);
        signupBuckets.remove(key);
    }

    private String resolveRateLimitKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String name = authentication.getName();
            if (name != null && !name.isBlank() && !"anonymousUser".equals(name)) {
                return name;
            }
        }
        String userIdHeader = request.getHeader("X-User-ID");
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            return userIdHeader;
        }
        String ip = request.getRemoteAddr();
        return ip == null || ip.isBlank() ? "unknown-client" : ip;
    }

    private void writeTooManyRequests(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"success\":false,\"data\":null,\"error\":{\"code\":\"RATE_LIMITED\",\"message\":\""
                        + message + "\",\"details\":[]},\"timestamp\":\""
                    + Instant.now() + "\",\"requestId\":\"rate-limit\"}");
    }
}
