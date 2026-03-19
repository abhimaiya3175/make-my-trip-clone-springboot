package com.makemytrip.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, Bucket> reviewBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> feedbackBuckets = new ConcurrentHashMap<>();

    @Override
        public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String userId = request.getHeader("X-User-ID");
        String key = userId != null ? userId : request.getRemoteAddr();

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

        return true;
    }

    private void writeTooManyRequests(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"success\":false,\"data\":null,\"error\":{\"code\":\"RATE_LIMITED\",\"message\":\""
                        + message + "\",\"details\":[]},\"timestamp\":\""
                        + java.time.Instant.now() + "\",\"requestId\":\"rate-limit\"}");
    }
}
