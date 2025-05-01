package com.example.salon.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalonService {
    private String id;
    private String name;
    private double price;
    private int duration; // in minutes
    private String description;
} 