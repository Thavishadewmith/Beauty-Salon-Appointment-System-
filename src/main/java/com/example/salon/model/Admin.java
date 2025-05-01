package com.example.salon.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin {
    private String id;
    private String username;
    private String password; // In production, this should be hashed
    private String email;
    private String role; // "super_admin" or "admin"
    private boolean isActive; // This will generate isActive() and setActive() methods
} 