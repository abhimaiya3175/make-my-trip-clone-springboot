package com.makemytrip.modules.booking.model;

public class Booking {
    private String type;
    private String bookingId;
    private String date;
    private int quantity;
    private double totalPrice;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
}
