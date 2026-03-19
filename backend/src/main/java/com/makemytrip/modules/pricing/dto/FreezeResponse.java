package com.makemytrip.modules.pricing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FreezeResponse {
    private String id;
    private String entityId;
    private String entityType;
    private double frozenPrice;
    private LocalDateTime frozenAt;
    private LocalDateTime expiresAt;
    private boolean active;
}
