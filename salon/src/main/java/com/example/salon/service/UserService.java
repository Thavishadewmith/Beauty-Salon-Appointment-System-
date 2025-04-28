package com.example.salon.service;

import com.example.salon.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private static final String USERS_FILE = "users.json";
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        List<User> users = fileStorageService.readFromFile(USERS_FILE, User.class);
        
        // Check if username already exists
        if (users.stream().anyMatch(u -> u.getUsername().equals(user.getUsername()))) {
            throw new RuntimeException("Username already exists");
        }

        // Encrypt password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Generate ID
        user.setId(System.currentTimeMillis());
        
        users.add(user);
        fileStorageService.saveToFile(USERS_FILE, users);
        return user;
    }

    public Optional<User> findByUsername(String username) {
        List<User> users = fileStorageService.readFromFile(USERS_FILE, User.class);
        return users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    public boolean validateUser(String username, String password) {
        return findByUsername(username)
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);
    }
} 