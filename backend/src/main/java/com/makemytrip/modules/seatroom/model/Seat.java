package com.makemytrip.modules.seatroom.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "seats")
public class Seat {
    @Id
    private String id;
    private String flightId;
    private String seatNumber;
    private String seatClass;
    private boolean available;
    private double price;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public String getSeatClass() { return seatClass; }
    public void setSeatClass(String seatClass) { this.seatClass = seatClass; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
