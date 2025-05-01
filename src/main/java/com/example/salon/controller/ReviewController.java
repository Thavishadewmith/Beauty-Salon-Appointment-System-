package com.example.salon.controller;

import com.example.salon.model.Review;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReviewController {
    private static final Logger logger = Logger.getLogger(ReviewController.class.getName());
    private final String REVIEWS_FILE;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReviewController() {
        // Get the user's home directory
        String userHome = System.getProperty("user.home");
        // Create a directory for the application if it doesn't exist
        Path appDir = Paths.get(userHome, "salon-reviews");
        try {
            Files.createDirectories(appDir);
            REVIEWS_FILE = appDir.resolve("reviews.json").toString();
            logger.info("Reviews file path: " + REVIEWS_FILE);
        } catch (IOException e) {
            logger.severe("Failed to create reviews directory: " + e.getMessage());
            throw new RuntimeException("Failed to initialize reviews storage", e);
        }
    }

    @GetMapping
    public ResponseEntity<?> getReviews() {
        try {
            List<Review> reviews = loadReviews();
            logger.info("Loaded " + reviews.size() + " reviews");
            return ResponseEntity.ok().body(Map.of("status", "success", "reviews", reviews));
        } catch (IOException e) {
            logger.severe("Failed to load reviews: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Failed to load reviews: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> addReview(@RequestBody Review review) {
        try {
            logger.info("Received review from user: " + review.getUsername());
            List<Review> reviews = loadReviews();
            
            // Add current date to the review
            review.setDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            reviews.add(review);
            saveReviews(reviews);
            
            logger.info("Successfully saved review for user: " + review.getUsername());
            return ResponseEntity.ok().body(Map.of("status", "success", "message", "Review added successfully"));
        } catch (IOException e) {
            logger.severe("Failed to add review: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Failed to add review: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{username}/{date}")
    public ResponseEntity<?> deleteReview(@PathVariable String username, @PathVariable String date) {
        try {
            List<Review> reviews = loadReviews();
            logger.info("Attempting to delete review for user: " + username + " with date: " + date);
            
            // Find the review to delete
            Review reviewToDelete = reviews.stream()
                .filter(review -> review.getUsername().equals(username) && review.getDate().equals(date))
                .findFirst()
                .orElse(null);
            
            if (reviewToDelete != null) {
                reviews.remove(reviewToDelete);
                saveReviews(reviews);
                logger.info("Successfully deleted review by user: " + username);
                return ResponseEntity.ok().body(Map.of("status", "success", "message", "Review deleted successfully"));
            } else {
                logger.warning("Review not found for user: " + username + " with date: " + date);
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Review not found"));
            }
        } catch (IOException e) {
            logger.severe("Failed to delete review: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Failed to delete review: " + e.getMessage()));
        }
    }

    private List<Review> loadReviews() throws IOException {
        Path filePath = Paths.get(REVIEWS_FILE);
        logger.info("Loading reviews from: " + filePath);
        
        if (!Files.exists(filePath)) {
            logger.info("Reviews file does not exist, creating new file");
            Files.createFile(filePath);
            return new ArrayList<>();
        }
        
        String content = Files.readString(filePath);
        if (content.trim().isEmpty()) {
            logger.info("Reviews file is empty");
            return new ArrayList<>();
        }
        
        List<Review> reviews = objectMapper.readValue(content, new TypeReference<List<Review>>() {});
        logger.info("Successfully loaded " + reviews.size() + " reviews");
        return reviews;
    }

    private void saveReviews(List<Review> reviews) throws IOException {
        Path filePath = Paths.get(REVIEWS_FILE);
        logger.info("Saving " + reviews.size() + " reviews to: " + filePath);
        
        String jsonContent = objectMapper.writeValueAsString(reviews);
        Files.writeString(filePath, jsonContent);
        logger.info("Successfully saved reviews");
    }
} 