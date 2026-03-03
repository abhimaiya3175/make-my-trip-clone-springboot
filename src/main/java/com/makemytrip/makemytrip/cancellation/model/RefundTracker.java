package com.makemytrip.makemytrip.cancellation.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * RefundTracker entity - tracks refund status and progress
 */
@Document(collection = "refund_trackers")
public class RefundTracker {
    @Id
    private String id;
    private String cancellationId;
    private RefundStatus status;
    private double refundAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String notes;

    public RefundTracker() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = RefundStatus.CANCELLATION_REQUESTED;
    }

    public RefundTracker(String cancellationId, double refundAmount) {
        this();
        this.cancellationId = cancellationId;
        this.refundAmount = refundAmount;
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
        this.updatedAt = LocalDateTime.now();
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
}
