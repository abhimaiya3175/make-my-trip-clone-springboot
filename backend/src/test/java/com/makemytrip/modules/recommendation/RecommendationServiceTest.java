package com.makemytrip.modules.recommendation;

import com.makemytrip.modules.recommendation.dto.RecommendationResponse;
import com.makemytrip.modules.recommendation.model.Recommendation;
import com.makemytrip.modules.recommendation.model.RecommendationFeedback;
import com.makemytrip.modules.recommendation.model.RecommendationFeedback.FeedbackType;
import com.makemytrip.modules.recommendation.model.UserEvent;
import com.makemytrip.modules.recommendation.repository.FeedbackRepository;
import com.makemytrip.modules.recommendation.repository.RecommendationRepository;
import com.makemytrip.modules.recommendation.repository.UserEventRepository;
import com.makemytrip.modules.recommendation.service.RecommendationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class RecommendationServiceTest {

    @Mock private RecommendationRepository recommendationRepository;
    @Mock private UserEventRepository userEventRepository;
    @Mock private FeedbackRepository feedbackRepository;
    @InjectMocks private RecommendationService service;

    private Recommendation rec(String userId, String itemId, String type, double score, String reason) {
        return Recommendation.builder()
                .id("rec-" + itemId)
                .userId(userId)
                .itemId(itemId)
                .itemType(type)
                .score(score)
                .reason(reason)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getRecommendationsForUser_returnsDirectMatches() {
        Recommendation r1 = rec("u1", "hotel-goa", "HOTEL", 0.91, "Beach lover");
        Recommendation r2 = rec("u1", "flight-blr", "FLIGHT", 0.86, "Frequent route");
        when(recommendationRepository.findByUserIdOrderByScoreDesc("u1")).thenReturn(List.of(r1, r2));
        when(feedbackRepository.findByUserId("u1")).thenReturn(Collections.emptyList());

        List<RecommendationResponse> result = service.getRecommendationsForUser("u1", null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getItemId()).isEqualTo("hotel-goa");
    }

    @Test
    void getRecommendationsForUser_filtersByItemType() {
        Recommendation r1 = rec("u1", "hotel-goa", "HOTEL", 0.91, "Beach");
        Recommendation r2 = rec("u1", "flight-blr", "FLIGHT", 0.86, "Route");
        when(recommendationRepository.findByUserIdOrderByScoreDesc("u1")).thenReturn(List.of(r1, r2));
        when(feedbackRepository.findByUserId("u1")).thenReturn(Collections.emptyList());

        List<RecommendationResponse> result = service.getRecommendationsForUser("u1", "FLIGHT");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItemType()).isEqualTo("FLIGHT");
    }

    @Test
    void getRecommendationsForUser_excludesNotInterested() {
        Recommendation r1 = rec("u1", "hotel-goa", "HOTEL", 0.91, "Beach");
        when(recommendationRepository.findByUserIdOrderByScoreDesc("u1")).thenReturn(List.of(r1));

        RecommendationFeedback fb = RecommendationFeedback.builder()
                .userId("u1").itemId("hotel-goa").itemType("HOTEL")
                .feedbackType(FeedbackType.NOT_INTERESTED).build();
        when(feedbackRepository.findByUserId("u1")).thenReturn(List.of(fb));

        List<RecommendationResponse> result = service.getRecommendationsForUser("u1", null);

        assertThat(result).isEmpty();
    }

    @Test
    void getRecommendationsForUser_fallsBackToCollaborativeFiltering() {
        // No direct recommendations
        when(recommendationRepository.findByUserIdOrderByScoreDesc("u1")).thenReturn(Collections.emptyList());
        when(feedbackRepository.findByUserId("u1")).thenReturn(Collections.emptyList());

        // User's events
        UserEvent event = UserEvent.builder()
                .userId("u1").entityId("fl-123").entityType("FLIGHT").build();
        when(userEventRepository.findByUserIdOrderByCreatedAtDesc("u1")).thenReturn(List.of(event));

        // Similar users found through same entity
        UserEvent otherEvent = UserEvent.builder()
                .userId("u2").entityId("fl-123").entityType("FLIGHT").build();
        when(userEventRepository.findByEntityIdAndEntityType("fl-123", "FLIGHT"))
                .thenReturn(List.of(event, otherEvent));

        // Other user's recommendations
        Recommendation otherRec = rec("u2", "hotel-bali", "HOTEL", 0.88, "Island vibes");
        when(recommendationRepository.findByUserIdOrderByScoreDesc("u2")).thenReturn(List.of(otherRec));

        List<RecommendationResponse> result = service.getRecommendationsForUser("u1", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItemId()).isEqualTo("hotel-bali");
    }

    @Test
    void recordEvent_savesAndReturns() {
        when(userEventRepository.save(any(UserEvent.class))).thenAnswer(i -> {
            UserEvent e = i.getArgument(0);
            e.setId("evt-1");
            return e;
        });

        UserEvent result = service.recordEvent("u1", "VIEW", "fl-123", "FLIGHT", null);

        assertThat(result.getUserId()).isEqualTo("u1");
        assertThat(result.getEventType()).isEqualTo("VIEW");
        verify(userEventRepository).save(any());
    }

    @Test
    void submitFeedback_createsNew() {
        when(feedbackRepository.findByUserIdAndItemIdAndItemType("u1", "h1", "HOTEL"))
                .thenReturn(Optional.empty());
        when(feedbackRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RecommendationFeedback result = service.submitFeedback("u1", "h1", "HOTEL", "LIKE");

        assertThat(result.getFeedbackType()).isEqualTo(FeedbackType.LIKE);
    }

    @Test
    void submitFeedback_updatesExisting() {
        RecommendationFeedback existing = RecommendationFeedback.builder()
                .id("fb-1").userId("u1").itemId("h1").itemType("HOTEL")
                .feedbackType(FeedbackType.LIKE).createdAt(LocalDateTime.now().minusDays(1))
                .build();
        when(feedbackRepository.findByUserIdAndItemIdAndItemType("u1", "h1", "HOTEL"))
                .thenReturn(Optional.of(existing));
        when(feedbackRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RecommendationFeedback result = service.submitFeedback("u1", "h1", "HOTEL", "NOT_INTERESTED");

        assertThat(result.getFeedbackType()).isEqualTo(FeedbackType.NOT_INTERESTED);
        assertThat(result.getId()).isEqualTo("fb-1"); // same document updated
    }

    @Test
    void getExplanation_returnsStoredReason() {
        Recommendation r = rec("u1", "hotel-goa", "HOTEL", 0.91, "You love beaches");
        when(recommendationRepository.findByUserIdOrderByScoreDesc("u1")).thenReturn(List.of(r));

        String explanation = service.getExplanation("hotel-goa", "u1");

        assertThat(explanation).isEqualTo("You love beaches");
    }

    @Test
    void getExplanation_returnsDefault_whenNoMatch() {
        when(recommendationRepository.findByUserIdOrderByScoreDesc("u1")).thenReturn(Collections.emptyList());

        String explanation = service.getExplanation("unknown", "u1");

        assertThat(explanation).contains("popular items");
    }

    @Test
    void getSimilarItems_findsRelatedRecommendations() {
        UserEvent e1 = UserEvent.builder().userId("u2").entityId("fl-123").entityType("FLIGHT").build();
        when(userEventRepository.findByEntityIdAndEntityType("fl-123", "FLIGHT")).thenReturn(List.of(e1));

        Recommendation r = rec("u2", "hotel-nearby", "HOTEL", 0.85, "Nearby hotel");
        when(recommendationRepository.findByUserIdOrderByScoreDesc("u2")).thenReturn(List.of(r));
        when(recommendationRepository.findByItemTypeAndItemId("FLIGHT", "fl-123")).thenReturn(Collections.emptyList());

        List<RecommendationResponse> result = service.getSimilarItems("fl-123", "FLIGHT");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItemId()).isEqualTo("hotel-nearby");
    }
}
