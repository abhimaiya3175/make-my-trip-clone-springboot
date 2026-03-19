package com.makemytrip.modules.pricing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FreezeRequest {
    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "entityId is required")
    private String entityId;

    @NotBlank(message = "entityType is required")
    private String entityType; // "FLIGHT" or "HOTEL"
}
