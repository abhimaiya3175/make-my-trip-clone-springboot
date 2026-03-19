package com.makemytrip.modules.recommendation.controller;

import com.makemytrip.common.api.ApiError;
import com.makemytrip.common.api.ApiResponse;
import com.makemytrip.modules.recommendation.dto.EventRequest;
import com.makemytrip.modules.recommendation.dto.FeedbackRequest;
import com.makemytrip.modules.recommendation.dto.RecommendationResponse;
import com.makemytrip.modules.recommendation.model.RecommendationFeedback;
import com.makemytrip.modules.recommendation.model.UserEvent;
import com.makemytrip.modules.recommendation.service.RecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> getRecommendations(
            @PathVariable String userId,
            @RequestParam(required = false) String itemType) {
        List<RecommendationResponse> recs = recommendationService.getRecommendationsForUser(userId, itemType);
        return ResponseEntity.ok(ApiResponse.ok(recs, UUID.randomUUID().toString()));
    }

    @GetMapping("/similar/{itemType}/{itemId}")
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> getSimilarItems(
            @PathVariable String itemType, @PathVariable String itemId) {
        List<RecommendationResponse> items = recommendationService.getSimilarItems(itemId, itemType);
        return ResponseEntity.ok(ApiResponse.ok(items, UUID.randomUUID().toString()));
    }

    @PostMapping("/events")
    public ResponseEntity<ApiResponse<UserEvent>> recordEvent(@Valid @RequestBody EventRequest req) {
        UserEvent event = recommendationService.recordEvent(
                req.getUserId(), req.getEventType(),
                req.getEntityId(), req.getEntityType(), req.getMetadata());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(event, UUID.randomUUID().toString()));
    }

    @PostMapping("/feedback")
    public ResponseEntity<ApiResponse<RecommendationFeedback>> submitFeedback(
            @Valid @RequestBody FeedbackRequest req) {
        RecommendationFeedback fb = recommendationService.submitFeedback(
                req.getUserId(), req.getItemId(), req.getItemType(), req.getFeedbackType());
        return ResponseEntity.ok(ApiResponse.ok(fb, UUID.randomUUID().toString()));
    }

    @GetMapping("/{itemId}/explain")
    public ResponseEntity<ApiResponse<Map<String, String>>> explain(
            @PathVariable String itemId, @RequestParam String userId) {
        String explanation = recommendationService.getExplanation(itemId, userId);
        return ResponseEntity.ok(ApiResponse.ok(
                Map.of("itemId", itemId, "explanation", explanation),
                UUID.randomUUID().toString()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(
                ApiResponse.fail(new ApiError("BAD_REQUEST", ex.getMessage(), List.of()),
                        UUID.randomUUID().toString()));
    }
}
