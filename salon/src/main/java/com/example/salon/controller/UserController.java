package com.example.salon.controller;

import com.example.salon.model.User;
import com.example.salon.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Username and password are required"
            ));
        }

        if (userService.validateUser(username, password)) {
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Login successful",
                "username", username
            ));
        } else {
            return ResponseEntity.status(401).body(Map.of(
                "status", "error",
                "message", "Invalid username or password"
            ));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (user.getUsername() == null || user.getPassword() == null || 
            user.getEmail() == null || user.getFullName() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "All fields are required"
            ));
        }

        try {
            User registeredUser = userService.registerUser(user);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Registration successful",
                "username", registeredUser.getUsername()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
} 