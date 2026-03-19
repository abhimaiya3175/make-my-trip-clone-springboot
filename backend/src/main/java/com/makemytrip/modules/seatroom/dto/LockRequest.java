package com.makemytrip.modules.seatroom.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LockRequest {
    @NotBlank(message = "userId is required")
    private String userId;
}
