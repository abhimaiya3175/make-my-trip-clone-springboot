package com.makemytrip.modules.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.ArrayList;

@Document(collection = "users")
public class User {
    @Id
    @JsonProperty("id")
    private String _id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role;
    private String phoneNumber;
    private List<com.makemytrip.modules.booking.model.Booking> bookings = new ArrayList<>();

    public String getFirstName() {return firstName;}
    public String getId() {
        return _id;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public String getPassword() {return password;}
    public String getEmail() {return email;}
    public String getRole() {return role;}
    public void setPassword(String password) {this.password = password;}
    public void setRole(String role) {this.role = role;}
    public List<com.makemytrip.modules.booking.model.Booking> getBookings(){return bookings;}
    public void setBookings(List<com.makemytrip.modules.booking.model.Booking> bookings){this.bookings=bookings;}
}
