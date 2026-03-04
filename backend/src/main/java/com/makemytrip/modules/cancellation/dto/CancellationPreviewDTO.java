package com.makemytrip.modules.cancellation.dto;

/**
 * DTO for cancellation preview - shows user details before confirming cancellation
 */
public class CancellationPreviewDTO {
    private String bookingId;
    private String bookingType;
    private int totalQuantity;
    private int quintityToCancel;
    private int remainingQuantity;
    private double originalPrice;
    private double pricePerUnit;
    private double cancellationPrice;
    private double refundAmount;
    private double refundPercentage;
    private String refundPolicy;
    private String hoursUntilTravel;
    private boolean eligibleFor90Percent;
    private String travelDate;

    // Getters and Setters
    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingType() {
        return bookingType;
    }

    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public int getQuintityToCancel() {
        return quintityToCancel;
    }

    public void setQuintityToCancel(int quintityToCancel) {
        this.quintityToCancel = quintityToCancel;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(int remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public double getCancellationPrice() {
        return cancellationPrice;
    }

    public void setCancellationPrice(double cancellationPrice) {
        this.cancellationPrice = cancellationPrice;
    }

    public double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(double refundAmount) {
        this.refundAmount = refundAmount;
    }

    public double getRefundPercentage() {
        return refundPercentage;
    }

    public void setRefundPercentage(double refundPercentage) {
        this.refundPercentage = refundPercentage;
    }

    public String getRefundPolicy() {
        return refundPolicy;
    }

    public void setRefundPolicy(String refundPolicy) {
        this.refundPolicy = refundPolicy;
    }

    public String getHoursUntilTravel() {
        return hoursUntilTravel;
    }

    public void setHoursUntilTravel(String hoursUntilTravel) {
        this.hoursUntilTravel = hoursUntilTravel;
    }

    public boolean isEligibleFor90Percent() {
        return eligibleFor90Percent;
    }

    public void setEligibleFor90Percent(boolean eligibleFor90Percent) {
        this.eligibleFor90Percent = eligibleFor90Percent;
    }

    public String getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(String travelDate) {
        this.travelDate = travelDate;
    }
}
