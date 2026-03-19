package com.makemytrip.modules.recommendation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.makemytrip.modules.recommendation.dto.RecommendationResponse;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserEventRepository userEventRepository;
    private final FeedbackRepository feedbackRepository;

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
        List<Recommendation> recs = recommendationRepository.findByUserIdOrderByScoreDesc(userId);

        if (itemType != null && !itemType.isBlank()) {
            recs = recs.stream()
                    .filter(r -> r.getItemType().equalsIgnoreCase(itemType))
                    .collect(Collectors.toList());
        }

        // If no stored recommendations, try collaborative filtering
        if (recs.isEmpty()) {
            recs = collaborativeFilter(userId, itemType);
        }

        // Demote items the user marked NOT_INTERESTED
        Set<String> disliked = feedbackRepository.findByUserId(userId).stream()
                .filter(f -> f.getFeedbackType() == FeedbackType.NOT_INTERESTED)
                .map(f -> f.getItemId())
                .collect(Collectors.toSet());

        return recs.stream()
                .filter(r -> !disliked.contains(r.getItemId()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Find items similar to a given item by looking at users who interacted
     * with this item and returning their other recommendations.
     */
    public List<RecommendationResponse> getSimilarItems(String itemId, String itemType) {
        // Find users who interacted with this item
        List<UserEvent> events = userEventRepository.findByEntityIdAndEntityType(itemId, itemType);
        Set<String> relatedUserIds = events.stream()
                .map(UserEvent::getUserId)
                .collect(Collectors.toSet());

        // Gather unique recommendations from those users, excluding the query item
        Map<String, Recommendation> seen = new LinkedHashMap<>();
        for (String uid : relatedUserIds) {
            for (Recommendation r : recommendationRepository.findByUserIdOrderByScoreDesc(uid)) {
                if (!r.getItemId().equals(itemId) && !seen.containsKey(r.getItemId())) {
                    seen.put(r.getItemId(), r);
                }
            }
        }

        // Also include any recommendations directly tied to this item
        recommendationRepository.findByItemTypeAndItemId(itemType, itemId)
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
        List<Recommendation> recs = recommendationRepository.findByUserIdOrderByScoreDesc(userId);
        return recs.stream()
                .filter(r -> r.getItemId().equals(itemId))
                .findFirst()
                .map(Recommendation::getReason)
                .orElse("This recommendation is based on popular items among similar travelers.");
    }

    // --- private helpers ---

    /**
     * Basic collaborative filtering: find users with similar event history
     * and return their recommendations.
     */
    private List<Recommendation> collaborativeFilter(String userId, String itemType) {
        List<UserEvent> myEvents = userEventRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (myEvents.isEmpty()) {
            return Collections.emptyList();
        }

        // Find entity ids this user interacted with
        Set<String> myEntities = myEvents.stream()
                .map(UserEvent::getEntityId)
                .collect(Collectors.toSet());

        // Find other users who interacted with the same entities
        Set<String> similarUsers = new HashSet<>();
        for (String entityId : myEntities) {
            String entityType = myEvents.stream()
                    .filter(e -> e.getEntityId().equals(entityId))
                    .map(UserEvent::getEntityType)
                    .findFirst().orElse("FLIGHT");
            userEventRepository.findByEntityIdAndEntityType(entityId, entityType).stream()
                    .map(UserEvent::getUserId)
                    .filter(uid -> !uid.equals(userId))
                    .forEach(similarUsers::add);
        }

        // Gather their recommendations
        Map<String, Recommendation> candidates = new LinkedHashMap<>();
        for (String uid : similarUsers) {
            for (Recommendation r : recommendationRepository.findByUserIdOrderByScoreDesc(uid)) {
                if (!myEntities.contains(r.getItemId()) && !candidates.containsKey(r.getItemId())) {
                    if (itemType == null || itemType.isBlank() || r.getItemType().equalsIgnoreCase(itemType)) {
                        candidates.put(r.getItemId(), r);
                    }
                }
            }
        }

        return candidates.values().stream()
                .sorted(Comparator.comparingDouble(Recommendation::getScore).reversed())
                .limit(10)
                .collect(Collectors.toList());
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
