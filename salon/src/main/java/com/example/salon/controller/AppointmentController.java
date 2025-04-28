package com.example.salon.controller;

import com.example.salon.model.Appointment;
import com.example.salon.service.AppointmentService;
import com.example.salon.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    @GetMapping("/user")
    public ResponseEntity<?> getUserAppointments(@RequestParam String username) {
        try {
            List<Appointment> appointments = appointmentService.getAppointmentsByUsername(username);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "appointments", appointments
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping
    public ResponseEntity<?> createAppointment(@RequestBody Appointment appointment) {
        try {
            Appointment createdAppointment = appointmentService.createAppointment(appointment);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Appointment created successfully",
                "appointment", createdAppointment
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id, @RequestParam String username) {
        try {
            appointmentService.deleteAppointment(id, username);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Appointment deleted successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
} 