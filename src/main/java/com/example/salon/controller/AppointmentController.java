package com.example.salon.controller;

import com.example.salon.model.Appointment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.salon.service.FileStorageService;
import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AppointmentController {
    private static final Logger logger = Logger.getLogger(AppointmentController.class.getName());
    private static final String APPOINTMENTS_FILE = "appointments.json";
    
    @Autowired
    private FileStorageService fileStorageService;

    @PostConstruct
    public void init() {
        try {
            // Initialize appointments file if it doesn't exist
            List<Appointment> appointments = fileStorageService.readFromFile(APPOINTMENTS_FILE, Appointment.class);
            if (appointments == null) {
                appointments = new ArrayList<>();
                fileStorageService.saveToFile(APPOINTMENTS_FILE, appointments);
                logger.info("Initialized empty appointments file");
            }
        } catch (Exception e) {
            logger.severe("Failed to initialize appointments file: " + e.getMessage());
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserAppointments(@RequestParam String username) {
        try {
            List<Appointment> appointments = fileStorageService.readFromFile(APPOINTMENTS_FILE, Appointment.class);
            if (appointments == null) {
                appointments = new ArrayList<>();
            }
            List<Appointment> userAppointments = appointments.stream()
                .filter(a -> a.getUsername().equals(username))
                .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "appointments", userAppointments
            ));
        } catch (Exception e) {
            logger.severe("Failed to load user appointments: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAppointments() {
        try {
            List<Appointment> appointments = fileStorageService.readFromFile(APPOINTMENTS_FILE, Appointment.class);
            if (appointments == null) {
                appointments = new ArrayList<>();
            }
            return ResponseEntity.ok().body(Map.of("status", "success", "appointments", appointments));
        } catch (Exception e) {
            logger.severe("Failed to load appointments: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Failed to load appointments: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createAppointment(@RequestBody Appointment appointment) {
        try {
            logger.info("Creating new appointment: " + appointment);
            
            // Generate a unique ID for the appointment
            appointment.setId(UUID.randomUUID().toString());
            
            // Set payment status based on payment method
            String paymentMethod = appointment.getPaymentMethod();
            logger.info("Payment method received: " + paymentMethod);
            
            if (paymentMethod != null && paymentMethod.equalsIgnoreCase("online")) {
                appointment.setPaymentStatus("Paid");
                logger.info("Setting payment status to 'Paid' for online payment");
            } else {
                appointment.setPaymentStatus("Pending");
                logger.info("Setting payment status to 'Pending' for cash payment");
            }
            
            // Set appointment status
            appointment.setStatus("Pending");
            
            List<Appointment> existingAppointments = fileStorageService.readFromFile(APPOINTMENTS_FILE, Appointment.class);
            if (existingAppointments == null) {
                existingAppointments = new ArrayList<>();
            }
            existingAppointments.add(appointment);
            fileStorageService.saveToFile(APPOINTMENTS_FILE, existingAppointments);
            
            logger.info("Successfully created appointment with ID: " + appointment.getId());
            logger.info("Final appointment details: " + appointment);
            
            return ResponseEntity.ok().body(Map.of(
                "status", "success",
                "message", "Appointment created successfully",
                "appointment", appointment
            ));
        } catch (Exception e) {
            logger.severe("Failed to create appointment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Failed to create appointment: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable String id, @RequestParam String username) {
        try {
            List<Appointment> appointments = fileStorageService.readFromFile(APPOINTMENTS_FILE, Appointment.class);
            if (appointments == null) {
                appointments = new ArrayList<>();
            }
            boolean removed = appointments.removeIf(a -> a.getId().equals(id) && a.getUsername().equals(username));
            
            if (removed) {
                fileStorageService.saveToFile(APPOINTMENTS_FILE, appointments);
                logger.info("Successfully deleted appointment with ID: " + id);
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Appointment deleted successfully"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Appointment not found or unauthorized"
                ));
            }
        } catch (Exception e) {
            logger.severe("Failed to delete appointment: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
} 