package com.makemytrip.modules.pricing.controller;

import com.makemytrip.common.api.ApiError;
import com.makemytrip.common.api.ApiResponse;
import com.makemytrip.modules.pricing.dto.*;
import com.makemytrip.modules.pricing.service.PricingService;
import com.makemytrip.security.AuthContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;

    /**
     * Get current dynamic price for a flight or hotel.
     */
    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<PriceResponse>> getPrice(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) LocalDate travelDate) {
        LocalDate effectiveTravelDate = travelDate != null ? travelDate : LocalDate.now().plusDays(7);
        var price = pricingService.calculatePrice(entityId, entityType.toUpperCase(), userId, effectiveTravelDate);
        return ResponseEntity.ok(ApiResponse.ok(price, reqId()));
    }

    /**
     * Get price history for the last N days.
     */
    @GetMapping("/{entityType}/{entityId}/history")
    public ResponseEntity<ApiResponse<PriceHistoryResponse>> getHistory(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @RequestParam(defaultValue = "7") int days) {
        var history = pricingService.getHistory(entityId, days);
        return ResponseEntity.ok(ApiResponse.ok(history, reqId()));
    }

    /**
     * Freeze current price for 24 hours.
     */
    @PostMapping("/freeze")
    public ResponseEntity<ApiResponse<FreezeResponse>> freezePrice(
            @Valid @RequestBody FreezeRequest request,
            Authentication authentication) {
        String userId = AuthContext.userId(authentication);
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(new ApiError("UNAUTHORIZED", "Authentication required", null), reqId()));
        }

        request.setUserId(userId); // trust server-side identity, not client input
        var freeze = pricingService.freezePrice(request);
        return ResponseEntity.ok(ApiResponse.ok(freeze, reqId()));
    }

    /**
     * Get user's active price freezes.
     */
    @GetMapping("/freeze/user/{userId}")
    public ResponseEntity<ApiResponse<List<FreezeResponse>>> getUserFreezes(
            @PathVariable String userId,
            Authentication authentication) {
        String authUserId = AuthContext.userId(authentication);
        if (authUserId == null || authUserId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(new ApiError("UNAUTHORIZED", "Authentication required", null), reqId()));
        }

        var freezes = pricingService.getUserFreezes(authUserId);
        return ResponseEntity.ok(ApiResponse.ok(freezes, reqId()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(new ApiError("CONFLICT", e.getMessage(), null), reqId()));
    }

    private String reqId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
