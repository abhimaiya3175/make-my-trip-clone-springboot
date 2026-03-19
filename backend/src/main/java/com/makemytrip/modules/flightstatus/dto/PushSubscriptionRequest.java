package com.makemytrip.modules.flightstatus.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PushSubscriptionRequest {

    @NotBlank(message = "Flight ID is required")
    private String flightId;

    @NotBlank(message = "Subscription endpoint is required")
    private String endpoint;

    @Valid
    @NotNull(message = "Subscription keys are required")
    private Keys keys;

    @Data
    public static class Keys {
        @NotBlank(message = "Subscription key p256dh is required")
        private String p256dh;

        @NotBlank(message = "Subscription key auth is required")
        private String auth;
    }
}
