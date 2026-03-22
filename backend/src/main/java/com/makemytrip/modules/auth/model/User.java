package com.makemytrip.modules.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    @JsonIgnore
    public String getPassword() {return password;}
    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}
    public String getRole() {return role;}
    @JsonProperty
    public void setPassword(String password) {this.password = password;}
    public void setRole(String role) {this.role = role;}
}
