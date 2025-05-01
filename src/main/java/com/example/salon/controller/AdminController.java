package com.example.salon.controller;

import com.example.salon.model.Admin;
import com.example.salon.model.Appointment;
import com.example.salon.model.SalonService;
import com.example.salon.model.User;
import com.example.salon.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AdminController {
    
    @Autowired
    private AdminService adminService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        System.out.println("Received login request for username: " + credentials.get("username"));
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");
            
            if (username == null || password == null) {
                System.out.println("Missing username or password");
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Username and password are required"
                ));
            }
            
            Admin admin = adminService.authenticateAdmin(username, password);
            System.out.println("Login successful for user: " + username);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Login successful",
                "admin", admin
            ));
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Admin admin) {
        try {
            Admin newAdmin = adminService.createAdmin(admin);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Admin registered successfully",
                "admin", newAdmin
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        try {
            List<User> users = adminService.getAllUsers();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "users", users
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable String userId) {
        try {
            adminService.toggleUserStatus(userId);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "User status updated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/services")
    public ResponseEntity<?> getServices() {
        try {
            List<SalonService> services = adminService.getAllServices();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "services", services
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/services")
    public ResponseEntity<?> addService(@RequestBody SalonService service) {
        try {
            SalonService newService = adminService.addService(service);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "service", newService
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/services/{serviceId}")
    public ResponseEntity<?> updateService(@PathVariable String serviceId, @RequestBody SalonService service) {
        try {
            SalonService updatedService = adminService.updateService(serviceId, service);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "service", updatedService
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/services/{serviceId}")
    public ResponseEntity<?> deleteService(@PathVariable String serviceId) {
        try {
            adminService.deleteService(serviceId);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Service deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/reports")
    public ResponseEntity<?> getReports() {
        try {
            Map<String, Object> reports = adminService.getReports();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "revenue", reports.get("revenue"),
                "appointments", reports.get("appointments")
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/appointments")
    public ResponseEntity<?> getAppointments() {
        try {
            List<Appointment> appointments = adminService.getAllAppointments();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "appointments", appointments
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/appointments/date/{date}")
    public ResponseEntity<?> getAppointmentsByDate(@PathVariable String date) {
        try {
            List<Appointment> appointments = adminService.getAppointmentsByDate(date);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "appointments", appointments
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/appointments/details/{id}")
    public ResponseEntity<?> getAppointmentById(@PathVariable String id) {
        System.out.println("=== Starting getAppointmentById ===");
        System.out.println("Requested appointment ID: " + id);
        
        try {
            Appointment appointment = adminService.getAppointmentById(id);
            System.out.println("Found appointment: " + (appointment != null ? appointment.getId() : "null"));
            
            if (appointment != null) {
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "appointment", appointment
                ));
            } else {
                System.out.println("Appointment not found with ID: " + id);
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Appointment not found"
                ));
            }
        } catch (Exception e) {
            System.out.println("Error in getAppointmentById: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } finally {
            System.out.println("=== End getAppointmentById ===");
        }
    }

    @PutMapping("/appointments/update/{id}")
    public ResponseEntity<?> updateAppointment(@PathVariable String id, @RequestBody Appointment updatedAppointment) {
        try {
            Appointment appointment = adminService.updateAppointment(id, updatedAppointment);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Appointment updated successfully",
                "appointment", appointment
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/appointments/delete/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable String id) {
        try {
            adminService.deleteAppointment(id);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Appointment deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/admins")
    public ResponseEntity<?> getAllAdmins() {
        try {
            List<Admin> admins = adminService.getAllAdmins();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "admins", admins
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/admins/{id}/status")
    public ResponseEntity<?> updateAdminStatus(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> status) {
        try {
            adminService.updateAdminStatus(id, status.get("active"));
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Admin status updated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/admins/{id}")
    public ResponseEntity<?> deleteAdmin(@PathVariable String id) {
        try {
            adminService.deleteAdmin(id);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Admin deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        try {
            Map<String, Object> stats = adminService.getDashboardStats();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "stats", stats
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        try {
            adminService.deleteUser(userId);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "User deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable String userId, @RequestBody User user) {
        try {
            adminService.updateUser(userId, user);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "User updated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
} 