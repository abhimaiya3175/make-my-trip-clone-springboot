package com.makemytrip.modules.pricing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FreezeRequest {
    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "entityId is required")
    private String entityId;

    @NotBlank(message = "entityType is required")
    private String entityType; // "FLIGHT" or "HOTEL"

    @NotNull(message = "travelDate is required")
    private LocalDate travelDate;
}
