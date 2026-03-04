package com.makemytrip.modules.cancellation.dto;

import com.makemytrip.modules.cancellation.model.RefundStatus;
import java.time.LocalDateTime;

/**
 * DTO for refund tracker information
 */
public class RefundTrackerDTO {
    private String id;
    private String cancellationId;
    private RefundStatus status;
    private double refundAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String notes;
    private String statusDisplay;

    // Constructors
    public RefundTrackerDTO() {}

    public RefundTrackerDTO(String id, String cancellationId, RefundStatus status, 
                           double refundAmount, LocalDateTime updatedAt) {
        this.id = id;
        this.cancellationId = cancellationId;
        this.status = status;
        this.refundAmount = refundAmount;
        this.updatedAt = updatedAt;
        this.statusDisplay = status.getDisplayName();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCancellationId() {
        return cancellationId;
    }

    public void setCancellationId(String cancellationId) {
        this.cancellationId = cancellationId;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public void setStatus(RefundStatus status) {
        this.status = status;
        this.statusDisplay = status.getDisplayName();
    }

    public double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(double refundAmount) {
        this.refundAmount = refundAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatusDisplay() {
        return statusDisplay;
    }

    public void setStatusDisplay(String statusDisplay) {
        this.statusDisplay = statusDisplay;
    }
}
