package com.makemytrip.modules.pricing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PriceHistoryResponse {
    private String entityId;
    private List<SnapshotPoint> history;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SnapshotPoint {
        private LocalDateTime time;
        private double price;
        private double multiplier;
        private String rules;
    }
}
