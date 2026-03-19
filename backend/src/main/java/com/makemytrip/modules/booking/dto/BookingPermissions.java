package com.makemytrip.modules.booking.dto;

public class BookingPermissions {
    private boolean canPay;
    private boolean canCancel;
    private boolean canModify;
    private boolean canReview;
    private String message;

    public BookingPermissions() {}

    public BookingPermissions(boolean canPay, boolean canCancel, boolean canModify, boolean canReview, String message) {
        this.canPay = canPay;
        this.canCancel = canCancel;
        this.canModify = canModify;
        this.canReview = canReview;
        this.message = message;
    }

    // Getters and Setters
    public boolean isCanPay() {
        return canPay;
    }

    public void setCanPay(boolean canPay) {
        this.canPay = canPay;
    }

    public boolean isCanCancel() {
        return canCancel;
    }

    public void setCanCancel(boolean canCancel) {
        this.canCancel = canCancel;
    }

    public boolean isCanModify() {
        return canModify;
    }

    public void setCanModify(boolean canModify) {
        this.canModify = canModify;
    }

    public boolean isCanReview() {
        return canReview;
    }

    public void setCanReview(boolean canReview) {
        this.canReview = canReview;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
