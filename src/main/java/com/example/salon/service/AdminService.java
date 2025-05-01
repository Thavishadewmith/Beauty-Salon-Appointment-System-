package com.example.salon.service;

import com.example.salon.model.Admin;
import com.example.salon.model.Appointment;
import com.example.salon.model.SalonService;
import com.example.salon.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.*;

@org.springframework.stereotype.Service
public class AdminService {
    private static final String ADMINS_FILE = "admins.json";
    private static final String APPOINTMENTS_FILE = "appointments.json";
    private static final String USERS_FILE = "users.json";
    private static final String SERVICES_FILE = "services.json";
    private final Path dataDir;
    private final FileStorageService fileStorageService;

    @Autowired
    public AdminService(FileStorageService fileStorageService) {
        System.out.println("AdminService constructor called");
        this.dataDir = Paths.get("data").toAbsolutePath();
        this.fileStorageService = fileStorageService;
        System.out.println("FileStorageService: " + (fileStorageService != null ? "initialized" : "null"));
        createDefaultAdmin();
    }

    private void createDefaultAdmin() {
        try {
            System.out.println("Creating default admin account...");
            
            // Force delete existing files
            try {
                Files.deleteIfExists(dataDir.resolve(ADMINS_FILE));
                System.out.println("Deleted existing admin file");
            } catch (IOException e) {
                System.err.println("Error deleting admin file: " + e.getMessage());
            }
            
            // Create new admin
            Admin defaultAdmin = new Admin();
            defaultAdmin.setId("1");
            defaultAdmin.setUsername("admin");
            defaultAdmin.setPassword("admin123");
            defaultAdmin.setEmail("admin@salon.com");
            defaultAdmin.setRole("super_admin");
            defaultAdmin.setActive(true);
            
            // Save the admin
            List<Admin> admins = new ArrayList<>();
            admins.add(defaultAdmin);
            fileStorageService.saveToFile(ADMINS_FILE, admins);
            System.out.println("Default admin created successfully");
        } catch (Exception e) {
            System.err.println("Error creating default admin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Admin createAdmin(Admin admin) {
        List<Admin> admins = fileStorageService.readFromFile(ADMINS_FILE, Admin.class);
        
        // Check if username already exists
        if (admins.stream().anyMatch(a -> a.getUsername().equals(admin.getUsername()))) {
            throw new RuntimeException("Username already exists");
        }
        
        // Generate a unique ID for the admin
        admin.setId(UUID.randomUUID().toString());
        admin.setActive(true);
        
        admins.add(admin);
        fileStorageService.saveToFile(ADMINS_FILE, admins);
        return admin;
    }

    public Admin authenticateAdmin(String username, String password) {
        System.out.println("Attempting to authenticate admin: " + username);
        try {
            List<Admin> admins = fileStorageService.readFromFile(ADMINS_FILE, Admin.class);
            System.out.println("Found " + admins.size() + " admins in the system");
            System.out.println("Admin list: " + admins);
            
            // Print raw file contents
            try {
                String fileContent = Files.readString(dataDir.resolve(ADMINS_FILE));
                System.out.println("Raw file contents: " + fileContent);
            } catch (Exception e) {
                System.err.println("Error reading raw file: " + e.getMessage());
            }
            
            if (admins == null || admins.isEmpty()) {
                System.out.println("No admins found in the system");
                // Try to create default admin if none exists
                createDefaultAdmin();
                // Read admins again after creation
                admins = fileStorageService.readFromFile(ADMINS_FILE, Admin.class);
                if (admins.isEmpty()) {
                    throw new RuntimeException("Failed to create default admin");
                }
            }
            
            Admin admin = admins.stream()
                    .filter(a -> {
                        System.out.println("Checking admin: " + a.getUsername());
                        System.out.println("Stored password: " + a.getPassword());
                        System.out.println("Input password: " + password);
                        boolean passwordMatch = a.getPassword().equals(password);
                        System.out.println("Password match: " + passwordMatch);
                        System.out.println("Active status: " + a.isActive());
                        return a.getUsername().equals(username) && 
                               passwordMatch && 
                               a.isActive();
                    })
                    .findFirst()
                    .orElseThrow(() -> {
                        System.out.println("Invalid credentials for user: " + username);
                        return new RuntimeException("Invalid credentials");
                    });
            
            System.out.println("Admin authenticated successfully: " + username);
            return admin;
        } catch (Exception e) {
            System.out.println("Error during authentication: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = fileStorageService.readFromFile(APPOINTMENTS_FILE, Appointment.class);
        
        // Calculate total revenue from paid appointments
        double totalRevenue = appointments.stream()
                .filter(a -> "Paid".equals(a.getPaymentStatus()))
                .mapToDouble(Appointment::getAmount)
                .sum();
        
        // Update the total revenue in the dashboard
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRevenue", totalRevenue);
        fileStorageService.saveObject("dashboard_stats.json", stats);
        
        return appointments;
    }

    public List<Appointment> getAppointmentsByDate(String date) {
        System.out.println("\n=== Starting getAppointmentsByDate ===");
        System.out.println("Input date: " + date);
        
        try {
            List<Appointment> appointments = fileStorageService.readFromFile(APPOINTMENTS_FILE, Appointment.class);
            System.out.println("Total appointments loaded: " + appointments.size());
            
            // Print all appointment dates for debugging
            System.out.println("\nAll appointment dates:");
            appointments.forEach(a -> {
                System.out.println("Appointment ID: " + a.getId() + 
                    ", Date: " + a.getDate() + 
                    ", Service: " + a.getService() + 
                    ", Customer: " + a.getUsername());
            });
            
            // Parse the input date to match the stored format
            String standardizedDate = date; // The date from the frontend is already in YYYY-MM-DD format
            System.out.println("\nStandardized date for comparison: " + standardizedDate);
            
            List<Appointment> filteredAppointments = appointments.stream()
                    .filter(a -> {
                        // Extract just the date part from the appointment date (in case it includes time)
                        String appointmentDate = a.getDate();
                        if (appointmentDate.contains(" ")) {
                            appointmentDate = appointmentDate.split(" ")[0];
                        }
                        boolean matches = appointmentDate.equals(standardizedDate);
                        System.out.println("Comparing appointment date: " + appointmentDate + 
                            " with standardized date: " + standardizedDate + " -> " + matches);
                        return matches;
                    })
                    .collect(Collectors.toList());
            
            System.out.println("\nFiltered appointments count: " + filteredAppointments.size());
            if (filteredAppointments.size() > 0) {
                System.out.println("Filtered appointments:");
                filteredAppointments.forEach(a -> {
                    System.out.println("Appointment ID: " + a.getId() + 
                        ", Date: " + a.getDate() + 
                        ", Service: " + a.getService() + 
                        ", Customer: " + a.getUsername());
                });
            }
            
            System.out.println("=== End getAppointmentsByDate ===\n");
            return filteredAppointments;
        } catch (Exception e) {
            System.err.println("Error in getAppointmentsByDate: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void updateAppointmentStatus(String appointmentId, String status) {
        List<Appointment> appointments = fileStorageService.readFromFile(APPOINTMENTS_FILE, Appointment.class);
        appointments.stream()
                .filter(a -> a.getId().equals(appointmentId))
                .findFirst()
                .ifPresent(a -> {
                    a.setStatus(status);
                    fileStorageService.saveToFile(APPOINTMENTS_FILE, appointments);
                });
    }

    public void updatePaymentStatus(String appointmentId, String paymentStatus) {
        List<Appointment> appointments = fileStorageService.readFromFile(APPOINTMENTS_FILE, Appointment.class);
        appointments.stream()
                .filter(a -> a.getId().equals(appointmentId))
                .findFirst()
                .ifPresent(a -> {
                    a.setPaymentStatus(paymentStatus);
                    fileStorageService.saveToFile(APPOINTMENTS_FILE, appointments);
                });
    }

    public void deleteAppointment(String appointmentId) {
        List<Appointment> appointments = fileStorageService.readFromFile(APPOINTMENTS_FILE, Appointment.class);
        boolean removed = appointments.removeIf(a -> a.getId().equals(appointmentId));
        if (removed) {
            fileStorageService.saveToFile(APPOINTMENTS_FILE, appointments);
        }
    }

    public List<User> getAllUsers() {
        return fileStorageService.readFromFile(USERS_FILE, User.class);
    }

    public User getUserById(String userId) {
        List<User> users = fileStorageService.readFromFile(USERS_FILE, User.class);
        return users.stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    public void updateUserStatus(String userId, boolean active) {
        List<User> users = fileStorageService.readFromFile(USERS_FILE, User.class);
        users.stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .ifPresent(u -> {
                    u.setActive(active);
                    fileStorageService.saveToFile(USERS_FILE, users);
                });
    }

    public void deleteUser(String userId) {
        try {
            System.out.println("=== Starting deleteUser ===");
            System.out.println("Attempting to delete user with ID: " + userId);
            
            // Convert userId to Long
            Long userLongId = Long.parseLong(userId);
            
            // Read current users
            List<User> users = fileStorageService.readFromFile(USERS_FILE, User.class);
            System.out.println("Current number of users: " + users.size());
            
            // Remove user with matching ID
            boolean removed = users.removeIf(u -> u.getId() != null && u.getId().equals(userLongId));
            System.out.println("User removed: " + removed);
            
            // Save updated users list
            fileStorageService.saveToFile(USERS_FILE, users);
            System.out.println("Users file updated successfully");
            
            // Verify the deletion
            List<User> updatedUsers = fileStorageService.readFromFile(USERS_FILE, User.class);
            boolean stillExists = updatedUsers.stream()
                .anyMatch(u -> u.getId() != null && u.getId().equals(userLongId));
            
            if (stillExists) {
                throw new RuntimeException("User was not properly deleted");
            }
            
            System.out.println("=== End deleteUser ===");
        } catch (NumberFormatException e) {
            System.out.println("Invalid user ID format: " + userId);
            throw new RuntimeException("Invalid user ID format", e);
        } catch (Exception e) {
            System.out.println("Error in deleteUser: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete user: " + e.getMessage(), e);
        }
    }

    public void updateUser(String userId, User updatedUser) {
        try {
            Long userLongId = Long.parseLong(userId);
            List<User> users = fileStorageService.readFromFile(USERS_FILE, User.class);
            
            boolean updated = false;
            for (User user : users) {
                if (user.getId() != null && user.getId().equals(userLongId)) {
                    // Update user fields
                    user.setUsername(updatedUser.getUsername());
                    user.setEmail(updatedUser.getEmail());
                    user.setFullName(updatedUser.getFullName());
                    user.setActive(updatedUser.isActive());
                    updated = true;
                    break;
                }
            }
            
            if (!updated) {
                throw new RuntimeException("User not found");
            }
            
            fileStorageService.saveToFile(USERS_FILE, users);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid user ID format", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update user: " + e.getMessage(), e);
        }
    }

    public List<Admin> getAllAdmins() {
        return fileStorageService.readFromFile(ADMINS_FILE, Admin.class);
    }

    public void updateAdminStatus(String adminId, boolean active) {
        List<Admin> admins = fileStorageService.readFromFile(ADMINS_FILE, Admin.class);
        admins.stream()
                .filter(a -> a.getId().equals(adminId))
                .findFirst()
                .ifPresent(a -> {
                    a.setActive(active);
                    fileStorageService.saveToFile(ADMINS_FILE, admins);
                });
    }

    public void deleteAdmin(String adminId) {
        List<Admin> admins = fileStorageService.readFromFile(ADMINS_FILE, Admin.class);
        boolean removed = admins.removeIf(a -> a.getId().equals(adminId));
        if (removed) {
            fileStorageService.saveToFile(ADMINS_FILE, admins);
        }
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get all appointments
        List<Appointment> appointments = getAllAppointments();
        stats.put("totalAppointments", appointments.size());
        
        // Count appointments by status
        Map<String, Long> appointmentsByStatus = appointments.stream()
                .collect(Collectors.groupingBy(Appointment::getStatus, Collectors.counting()));
        stats.put("appointmentsByStatus", appointmentsByStatus);
        
        // Count appointments by payment status
        Map<String, Long> appointmentsByPayment = appointments.stream()
                .collect(Collectors.groupingBy(Appointment::getPaymentStatus, Collectors.counting()));
        stats.put("appointmentsByPayment", appointmentsByPayment);
        
        // Calculate total revenue from paid appointments
        double totalRevenue = appointments.stream()
                .filter(a -> "Paid".equals(a.getPaymentStatus()))
                .mapToDouble(Appointment::getAmount)
                .sum();
        stats.put("totalRevenue", totalRevenue);
        
        // Get user statistics
        List<User> users = getAllUsers();
        stats.put("totalUsers", users.size());
        
        // Get admin statistics
        List<Admin> admins = getAllAdmins();
        stats.put("totalAdmins", admins.size());
        
        return stats;
    }

    public List<SalonService> getAllServices() {
        return fileStorageService.readFromFile(SERVICES_FILE, SalonService.class);
    }

    public SalonService addService(SalonService service) {
        List<SalonService> services = getAllServices();
        service.setId(UUID.randomUUID().toString());
        services.add(service);
        fileStorageService.saveToFile(SERVICES_FILE, services);
        return service;
    }

    public SalonService updateService(String serviceId, SalonService updatedService) {
        List<SalonService> services = getAllServices();
        SalonService service = services.stream()
                .filter(s -> s.getId().equals(serviceId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Service not found"));
        
        service.setName(updatedService.getName());
        service.setPrice(updatedService.getPrice());
        service.setDuration(updatedService.getDuration());
        service.setDescription(updatedService.getDescription());
        
        fileStorageService.saveToFile(SERVICES_FILE, services);
        return service;
    }

    public void deleteService(String serviceId) {
        List<SalonService> services = getAllServices();
        services.removeIf(s -> s.getId().equals(serviceId));
        fileStorageService.saveToFile(SERVICES_FILE, services);
    }

    public Map<String, Object> getReports() {
        List<Appointment> appointments = getAllAppointments();
        List<SalonService> services = getAllServices();

        // Revenue data
        Map<String, Double> revenueByDate = appointments.stream()
                .filter(a -> a.getPaymentStatus().equals("Paid"))
                .collect(Collectors.groupingBy(
                    Appointment::getDate,
                    Collectors.summingDouble(Appointment::getAmount)
                ));

        // Appointments by service
        Map<String, Long> appointmentsByService = appointments.stream()
                .collect(Collectors.groupingBy(
                    Appointment::getService,
                    Collectors.counting()
                ));

        return Map.of(
            "revenue", Map.of(
                "labels", new ArrayList<>(revenueByDate.keySet()),
                "data", new ArrayList<>(revenueByDate.values())
            ),
            "appointments", Map.of(
                "labels", new ArrayList<>(appointmentsByService.keySet()),
                "data", new ArrayList<>(appointmentsByService.values())
            )
        );
    }

    public void toggleUserStatus(String userId) {
        List<User> users = getAllUsers();
        User user = users.stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setActive(!user.isActive());
        fileStorageService.saveToFile(USERS_FILE, users);
    }

    public Appointment getAppointmentById(String id) {
        System.out.println("=== Starting getAppointmentById in AdminService ===");
        System.out.println("Looking for appointment with ID: " + id);
        
        try {
            List<Appointment> appointments = fileStorageService.readFromFile(APPOINTMENTS_FILE, Appointment.class);
            System.out.println("Total appointments loaded: " + appointments.size());
            
            // Print all appointment IDs for debugging
            System.out.println("All appointment IDs:");
            appointments.forEach(a -> {
                System.out.println("Appointment ID: " + a.getId() + 
                    ", Service: " + a.getService() + 
                    ", Date: " + a.getDate() + 
                    ", Time: " + a.getTime());
            });
            
            Appointment appointment = appointments.stream()
                    .filter(a -> {
                        boolean matches = a.getId() != null && a.getId().equals(id);
                        System.out.println("Comparing appointment ID: " + a.getId() + " with " + id + " -> " + matches);
                        return matches;
                    })
                    .findFirst()
                    .orElse(null);
            
            if (appointment != null) {
                System.out.println("Found appointment: " + appointment.getId());
                System.out.println("Appointment details: " + appointment);
            } else {
                System.out.println("No appointment found with ID: " + id);
            }
            
            System.out.println("=== End getAppointmentById in AdminService ===");
            return appointment;
        } catch (Exception e) {
            System.out.println("Error in getAppointmentById: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public Appointment updateAppointment(String id, Appointment updatedAppointment) {
        System.out.println("=== Starting updateAppointment ===");
        System.out.println("Updating appointment with ID: " + id);
        System.out.println("Updated appointment data: " + updatedAppointment);
        
        try {
            List<Appointment> appointments = fileStorageService.readFromFile(APPOINTMENTS_FILE, Appointment.class);
            System.out.println("Total appointments loaded: " + appointments.size());
            
            // Find the appointment to update
            Appointment appointment = appointments.stream()
                    .filter(a -> a.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
            System.out.println("Found appointment to update: " + appointment);
            
            // Update the appointment fields
            if (updatedAppointment.getStatus() != null) {
                appointment.setStatus(updatedAppointment.getStatus());
                System.out.println("Updated status to: " + updatedAppointment.getStatus());
            }
            if (updatedAppointment.getPaymentStatus() != null) {
                appointment.setPaymentStatus(updatedAppointment.getPaymentStatus());
                System.out.println("Updated payment status to: " + updatedAppointment.getPaymentStatus());
            }
            if (updatedAppointment.getService() != null) {
                appointment.setService(updatedAppointment.getService());
            }
            if (updatedAppointment.getDate() != null) {
                appointment.setDate(updatedAppointment.getDate());
            }
            if (updatedAppointment.getTime() != null) {
                appointment.setTime(updatedAppointment.getTime());
            }
            if (updatedAppointment.getAmount() > 0) {
                appointment.setAmount(updatedAppointment.getAmount());
            }
            if (updatedAppointment.getNotes() != null) {
                appointment.setNotes(updatedAppointment.getNotes());
            }
            
            // Save the updated appointments
            fileStorageService.saveToFile(APPOINTMENTS_FILE, appointments);
            System.out.println("Appointments saved successfully");
            
            System.out.println("=== End updateAppointment ===");
            return appointment;
        } catch (Exception e) {
            System.err.println("Error in updateAppointment: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
} 