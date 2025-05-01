package com.example.salon.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("date")
    private String date;
    
    @JsonProperty("time")
    private String time;
    
    @JsonProperty("service")
    private String service;
    
    @JsonProperty("status")
    private String status; // Pending, Confirmed, Completed, Cancelled
    
    @JsonProperty("paymentStatus")
    private String paymentStatus; // Pending, Paid
    
    @JsonProperty("paymentMethod")
    private String paymentMethod; // online, cash
    
    @JsonProperty("amount")
    private double amount;
    
    @JsonProperty("notes")
    private String notes;
} 