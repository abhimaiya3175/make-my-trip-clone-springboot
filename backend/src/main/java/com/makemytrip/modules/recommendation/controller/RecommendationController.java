package com.makemytrip.modules.recommendation.controller;

import com.makemytrip.modules.recommendation.model.Recommendation;
import com.makemytrip.modules.recommendation.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*")
public class RecommendationController {
    @Autowired
    private RecommendationService recommendationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Recommendation>> getRecommendations(@PathVariable String userId) {
        return ResponseEntity.ok(recommendationService.getRecommendationsForUser(userId));
    }

    @GetMapping("/similar/{itemType}/{itemId}")
    public ResponseEntity<List<Recommendation>> getSimilarItems(
            @PathVariable String itemType, @PathVariable String itemId) {
        return ResponseEntity.ok(recommendationService.getSimilarItems(itemId, itemType));
    }
}
