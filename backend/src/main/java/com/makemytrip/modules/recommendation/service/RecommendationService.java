package com.makemytrip.modules.recommendation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.makemytrip.modules.recommendation.dto.RecommendationResponse;
import com.makemytrip.modules.flights.model.Flight;
import com.makemytrip.modules.flights.repository.FlightRepository;
import com.makemytrip.modules.hotels.model.Hotel;
import com.makemytrip.modules.hotels.repository.HotelRepository;
import com.makemytrip.modules.recommendation.model.Recommendation;
import com.makemytrip.modules.recommendation.model.RecommendationFeedback;
import com.makemytrip.modules.recommendation.model.RecommendationFeedback.FeedbackType;
import com.makemytrip.modules.recommendation.model.UserEvent;
import com.makemytrip.modules.recommendation.repository.FeedbackRepository;
import com.makemytrip.modules.recommendation.repository.RecommendationRepository;
import com.makemytrip.modules.recommendation.repository.UserEventRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class RecommendationService {

    private static final int MAX_RESULTS = 10;
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[a-zA-Z]{4,}");

    private final RecommendationRepository recommendationRepository;
    private final UserEventRepository userEventRepository;
    private final FeedbackRepository feedbackRepository;
    private final FlightRepository flightRepository;
    private final HotelRepository hotelRepository;

    @PostConstruct
    void loadMockData() {
        if (recommendationRepository.count() > 0) {
            log.info("Recommendations already loaded, skipping mock import");
            return;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            InputStream is = new ClassPathResource("mock/recommendations.mock.json").getInputStream();
            List<Recommendation> recs = mapper.readValue(is, new TypeReference<>() {});
            // Recommendation documents are TTL-indexed, so always refresh createdAt at load time.
            LocalDateTime now = LocalDateTime.now();
            recs.forEach(r -> r.setCreatedAt(now));
            recommendationRepository.saveAll(recs);
            log.info("Loaded {} mock recommendations", recs.size());
        } catch (Exception e) {
            log.warn("Could not load mock recommendations: {}", e.getMessage());
        }
    }

    /**
     * Get recommendations for a user. Falls back to collaborative filtering
     * if no direct recommendations exist.
     */
    public List<RecommendationResponse> getRecommendationsForUser(String userId, String itemType) {
        List<UserEvent> myEvents = safeList(userEventRepository.findByUserIdOrderByCreatedAtDesc(userId));
        List<Recommendation> recs = safeList(recommendationRepository.findByUserIdOrderByScoreDesc(userId));

        if (itemType != null && !itemType.isBlank()) {
            recs = recs.stream()
                    .filter(r -> r.getItemType().equalsIgnoreCase(itemType))
                    .collect(Collectors.toList());
        }

        // If no stored recommendations, try collaborative filtering
        if (recs.isEmpty()) {
            List<RecommendationResponse> collaborative = collaborativeFilter(userId, itemType);
            if (!collaborative.isEmpty()) {
                return applyFeedbackLoop(collaborative, userId);
            }

            List<RecommendationResponse> fallback = globalFallback(userId, itemType, myEvents);
            return applyFeedbackLoop(fallback, userId);
        }

        return applyFeedbackLoop(recs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList()), userId);
    }

    /**
     * Find items similar to a given item by looking at users who interacted
     * with this item and returning their other recommendations.
     */
    public List<RecommendationResponse> getSimilarItems(String itemId, String itemType) {
        // Find users who interacted with this item
        List<UserEvent> events = safeList(userEventRepository.findByEntityIdAndEntityType(itemId, itemType));
        Set<String> relatedUserIds = events.stream()
                .map(UserEvent::getUserId)
                .collect(Collectors.toSet());

        // Gather unique recommendations from those users, excluding the query item
        Map<String, Recommendation> seen = new LinkedHashMap<>();
        for (String uid : relatedUserIds) {
            for (Recommendation r : safeList(recommendationRepository.findByUserIdOrderByScoreDesc(uid))) {
                if (!r.getItemId().equals(itemId) && !seen.containsKey(r.getItemId())) {
                    seen.put(r.getItemId(), r);
                }
            }
        }

        // Also include any recommendations directly tied to this item
        safeList(recommendationRepository.findByItemTypeAndItemId(itemType, itemId))
                .forEach(r -> seen.putIfAbsent(r.getItemId(), r));

        return seen.values().stream()
                .sorted(Comparator.comparingDouble(Recommendation::getScore).reversed())
                .limit(10)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Record a user event (VIEW, SEARCH, BOOK, etc.)
     */
    public UserEvent recordEvent(String userId, String eventType, String entityId,
                                 String entityType, String metadata) {
        UserEvent event = UserEvent.builder()
                .userId(userId)
                .eventType(eventType)
                .entityId(entityId)
                .entityType(entityType)
                .metadata(metadata)
                .createdAt(LocalDateTime.now())
                .build();
        return userEventRepository.save(event);
    }

    /**
     * Submit feedback on a recommendation. Upserts existing feedback.
     */
    public RecommendationFeedback submitFeedback(String userId, String itemId,
                                                  String itemType, String feedbackType) {
        FeedbackType ft = FeedbackType.valueOf(feedbackType.toUpperCase());

        Optional<RecommendationFeedback> existing =
                feedbackRepository.findByUserIdAndItemIdAndItemType(userId, itemId, itemType);

        RecommendationFeedback fb;
        if (existing.isPresent()) {
            fb = existing.get();
            fb.setFeedbackType(ft);
            fb.setCreatedAt(LocalDateTime.now());
        } else {
            fb = RecommendationFeedback.builder()
                    .userId(userId)
                    .itemId(itemId)
                    .itemType(itemType)
                    .feedbackType(ft)
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        return feedbackRepository.save(fb);
    }

    /**
     * Get explanation text for why a recommendation was generated.
     */
    public String getExplanation(String itemId, String userId) {
        List<RecommendationResponse> recs = getRecommendationsForUser(userId, null);
        Optional<RecommendationResponse> fromUserFlow = recs.stream()
            .filter(r -> r.getItemId().equalsIgnoreCase(itemId))
                .findFirst()
            .map(r -> r);
        if (fromUserFlow.isPresent()) {
            return fromUserFlow.get().getReason();
        }

        return safeList(recommendationRepository.findByItemId(itemId)).stream()
            .max(Comparator.comparingDouble(Recommendation::getScore))
            .map(Recommendation::getReason)
            .orElse("This recommendation is based on popular items among similar travelers.");
    }

    // --- private helpers ---

    /**
     * Basic collaborative filtering: find users with similar event history
     * and return their recommendations.
     */
    private List<RecommendationResponse> collaborativeFilter(String userId, String itemType) {
        List<UserEvent> myEvents = safeList(userEventRepository.findByUserIdOrderByCreatedAtDesc(userId));
        if (myEvents.isEmpty()) {
            return Collections.emptyList();
        }

        // Find entity ids this user interacted with
        Set<String> myEntities = myEvents.stream()
                .map(UserEvent::getEntityId)
                .collect(Collectors.toSet());
        Set<String> userTokens = extractHistoryTokens(myEvents);

        // Find other users who interacted with the same entities
        Map<String, Integer> similarUsersOverlap = new HashMap<>();
        for (String entityId : myEntities) {
            String entityType = myEvents.stream()
                    .filter(e -> e.getEntityId().equals(entityId))
                    .map(UserEvent::getEntityType)
                    .findFirst().orElse("FLIGHT");
                safeList(userEventRepository.findByEntityIdAndEntityType(entityId, entityType)).stream()
                    .map(UserEvent::getUserId)
                    .filter(uid -> !uid.equals(userId))
                    .forEach(uid -> similarUsersOverlap.merge(uid, 1, Integer::sum));
        }

        // Gather their recommendations and rank by source overlap + base relevance.
        Map<String, RecommendationResponse> candidates = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> similar : similarUsersOverlap.entrySet()) {
            String uid = similar.getKey();
            int overlap = similar.getValue();
            for (Recommendation r : safeList(recommendationRepository.findByUserIdOrderByScoreDesc(uid))) {
                if (myEntities.contains(r.getItemId())) {
                    continue;
                }
                if (itemType != null && !itemType.isBlank() && !r.getItemType().equalsIgnoreCase(itemType)) {
                    continue;
                }

                double collaborativeScore = Math.min(0.99, r.getScore() * (1.0 + (0.08 * overlap)));
                String reason = buildCollaborativeReason(r, userTokens, overlap);

                RecommendationResponse candidate = RecommendationResponse.builder()
                        .id(r.getId())
                        .userId(userId)
                        .itemId(r.getItemId())
                        .itemType(r.getItemType())
                        .score(collaborativeScore)
                        .reason(reason)
                        .tags(r.getTags())
                        .createdAt(LocalDateTime.now())
                        .build();

                RecommendationResponse existing = candidates.get(r.getItemId());
                if (existing == null || candidate.getScore() > existing.getScore()) {
                    candidates.put(r.getItemId(), candidate);
                }
            }
        }

        return candidates.values().stream()
                .sorted(Comparator.comparingDouble(RecommendationResponse::getScore).reversed())
                .limit(MAX_RESULTS)
                .collect(Collectors.toList());
    }

    private List<RecommendationResponse> globalFallback(String userId, String itemType, List<UserEvent> myEvents) {
        List<Recommendation> pool;
        if (itemType != null && !itemType.isBlank()) {
            pool = safeList(recommendationRepository.findTop100ByItemTypeOrderByScoreDesc(itemType));
        } else {
            pool = safeList(recommendationRepository.findTop100ByOrderByScoreDesc());
        }

        Map<String, RecommendationResponse> uniqueByItem = new LinkedHashMap<>();
        for (Recommendation rec : pool) {
            String key = feedbackKey(rec.getItemType(), rec.getItemId());
            uniqueByItem.putIfAbsent(key, RecommendationResponse.builder()
                    .id(rec.getId())
                    .userId(userId)
                    .itemId(rec.getItemId())
                    .itemType(rec.getItemType())
                    .score(Math.min(0.95, rec.getScore()))
                    .reason("Popular with travelers right now. Interact with flights/hotels to personalize this feed.")
                    .tags(rec.getTags())
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        // If recommendations collection is empty or sparse, synthesize from flights/hotels catalog.
        for (RecommendationResponse rec : catalogFallback(userId, itemType, myEvents)) {
            String key = feedbackKey(rec.getItemType(), rec.getItemId());
            uniqueByItem.putIfAbsent(key, rec);
        }

        return uniqueByItem.values().stream()
                .limit(MAX_RESULTS)
                .collect(Collectors.toList());
    }

    private List<RecommendationResponse> catalogFallback(String userId, String itemType, List<UserEvent> myEvents) {
        Set<String> userTokens = extractHistoryTokens(myEvents);
        List<RecommendationResponse> out = new ArrayList<>();

        if (itemType == null || itemType.isBlank() || "FLIGHT".equalsIgnoreCase(itemType)) {
            List<Flight> flights = flightRepository == null ? Collections.emptyList() : safeList(flightRepository.findAll());
            for (Flight flight : flights) {
                if (flight == null || flight.getId() == null) {
                    continue;
                }
                String text = ((flight.getFrom() == null ? "" : flight.getFrom()) + " "
                        + (flight.getTo() == null ? "" : flight.getTo()) + " "
                        + (flight.getFlightName() == null ? "" : flight.getFlightName())).toLowerCase(Locale.ROOT);

                out.add(RecommendationResponse.builder()
                        .id("catalog-flight-" + flight.getId())
                        .userId(userId)
                        .itemId(flight.getId())
                        .itemType("FLIGHT")
                        .score(Math.max(0.55, 0.9 - (Math.max(0, flight.getPrice()) / 200000.0)))
                        .reason(buildCatalogReason(userTokens, text, safeString(flight.getTo())))
                        .tags(extractTags(text))
                        .createdAt(LocalDateTime.now())
                        .build());
            }
        }

        if (itemType == null || itemType.isBlank() || "HOTEL".equalsIgnoreCase(itemType)) {
            List<Hotel> hotels = hotelRepository == null ? Collections.emptyList() : safeList(hotelRepository.findAll());
            for (Hotel hotel : hotels) {
                if (hotel == null || hotel.getId() == null) {
                    continue;
                }

                String name = safeString(hotel.gethotelName());
                String location = safeString(hotel.getLocation());
                String amenities = safeString(hotel.getamenities());
                String text = (name + " " + location + " " + amenities).toLowerCase(Locale.ROOT);

                out.add(RecommendationResponse.builder()
                        .id("catalog-hotel-" + hotel.getId())
                        .userId(userId)
                        .itemId(hotel.getId())
                        .itemType("HOTEL")
                        .score(Math.max(0.55, 0.92 - (Math.max(0, hotel.getPricePerNight()) / 100000.0)))
                        .reason(buildCatalogReason(userTokens, text, location.isBlank() ? name : location))
                        .tags(extractTags(text))
                        .createdAt(LocalDateTime.now())
                        .build());
            }
        }

        return out.stream()
                .sorted(Comparator.comparingDouble(RecommendationResponse::getScore).reversed())
                .limit(MAX_RESULTS)
                .collect(Collectors.toList());
    }

    private String buildCatalogReason(Set<String> userTokens, String normalizedText, String destinationLabel) {
        if (!userTokens.isEmpty()) {
            if (userTokens.contains("beach") || userTokens.contains("beaches") || userTokens.contains("coast") || userTokens.contains("island")) {
                if (containsAny(normalizedText, "beach", "goa", "bali", "island", "coast")) {
                    return "You liked beaches! Try " + destinationLabel + ".";
                }
            }
            if (userTokens.contains("mountain") || userTokens.contains("trek") || userTokens.contains("hills")) {
                if (containsAny(normalizedText, "mountain", "hill", "manali", "shimla", "leh")) {
                    return "You seem to enjoy mountain trips. Consider " + destinationLabel + ".";
                }
            }
            return "Based on your recent activity history, this option matches your travel preferences.";
        }

        return "Popular with travelers right now. Explore a few items so this feed becomes more personalized.";
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private List<String> extractTags(String text) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        Matcher matcher = TOKEN_PATTERN.matcher(text == null ? "" : text.toLowerCase(Locale.ROOT));
        while (matcher.find() && tags.size() < 4) {
            tags.add(matcher.group());
        }
        return new ArrayList<>(tags);
    }

    private List<RecommendationResponse> applyFeedbackLoop(List<RecommendationResponse> recs, String userId) {
        List<RecommendationFeedback> feedback = safeList(feedbackRepository.findByUserId(userId));
        Map<String, FeedbackType> feedbackByKey = new HashMap<>();
        for (RecommendationFeedback fb : feedback) {
            feedbackByKey.put(feedbackKey(fb.getItemType(), fb.getItemId()), fb.getFeedbackType());
        }

        List<RecommendationResponse> reranked = new ArrayList<>();
        for (RecommendationResponse rec : recs) {
            FeedbackType type = feedbackByKey.get(feedbackKey(rec.getItemType(), rec.getItemId()));
            if (type == FeedbackType.NOT_INTERESTED) {
                continue;
            }

            double adjustedScore = rec.getScore();
            String adjustedReason = rec.getReason();
            if (type == FeedbackType.LIKE) {
                adjustedScore = Math.min(0.99, adjustedScore * 1.12);
                adjustedReason = adjustedReason + " You liked similar picks, so this was boosted.";
            } else if (type == FeedbackType.SAVE) {
                adjustedScore = Math.min(0.99, adjustedScore * 1.08);
                adjustedReason = adjustedReason + " Saved items in this style increased this ranking.";
            }

            rec.setScore(adjustedScore);
            rec.setReason(adjustedReason);
            reranked.add(rec);
        }

        reranked.sort(Comparator.comparingDouble(RecommendationResponse::getScore).reversed());
        return reranked.stream().limit(MAX_RESULTS).collect(Collectors.toList());
    }

    private String buildCollaborativeReason(Recommendation rec, Set<String> userTokens, int overlap) {
        if (rec.getTags() != null) {
            for (String tag : rec.getTags()) {
                String normalizedTag = tag == null ? "" : tag.trim().toLowerCase(Locale.ROOT);
                if (!normalizedTag.isBlank() && userTokens.contains(normalizedTag)) {
                    return "You liked " + normalizedTag + " trips. Travelers with similar history also chose this option.";
                }
            }
        }

        if (overlap > 1) {
            return "Multiple travelers with similar booking/search history interacted with this option.";
        }
        return "A traveler with similar recent activity also engaged with this option.";
    }

    private Set<String> extractHistoryTokens(List<UserEvent> events) {
        Set<String> tokens = new HashSet<>();
        for (UserEvent event : events) {
            extractTokensInto(event.getEntityId(), tokens);
            extractTokensInto(event.getMetadata(), tokens);
        }
        return tokens;
    }

    private void extractTokensInto(String text, Set<String> into) {
        if (text == null || text.isBlank()) {
            return;
        }
        Matcher matcher = TOKEN_PATTERN.matcher(text.toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            into.add(matcher.group());
        }
    }

    private String feedbackKey(String itemType, String itemId) {
        return (itemType == null ? "" : itemType.toUpperCase(Locale.ROOT)) + "::" + itemId;
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    private RecommendationResponse toResponse(Recommendation r) {
        return RecommendationResponse.builder()
                .id(r.getId())
                .userId(r.getUserId())
                .itemId(r.getItemId())
                .itemType(r.getItemType())
                .score(r.getScore())
                .reason(r.getReason())
                .tags(r.getTags())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
