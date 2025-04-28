package com.example.salon.service;

import com.example.salon.model.Appointment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {
    private static final String APPOINTMENTS_FILE = "appointments.json";
    
    @Autowired
    private FileStorageService fileStorageService;

    public Appointment createAppointment(Appointment appointment) {
        List<Appointment> appointments = fileStorageService.readFromFile(APPOINTMENTS_FILE, Appointment.class);
        
        // Generate ID
        appointment.setId(System.currentTimeMillis());
        
        appointments.add(appointment);
        fileStorageService.saveToFile(APPOINTMENTS_FILE, appointments);
        return appointment;
    }

    public List<Appointment> getAppointmentsByUsername(String username) {
        List<Appointment> appointments = fileStorageService.readFromFile(APPOINTMENTS_FILE, Appointment.class);
        return appointments.stream()
                .filter(a -> a.getUsername().equals(username))
                .collect(Collectors.toList());
    }

    public void deleteAppointment(Long id, String username) {
        List<Appointment> appointments = fileStorageService.readFromFile(APPOINTMENTS_FILE, Appointment.class);
        boolean removed = appointments.removeIf(a -> a.getId().equals(id) && a.getUsername().equals(username));
        
        if (!removed) {
            throw new RuntimeException("Appointment not found or unauthorized");
        }
        
        fileStorageService.saveToFile(APPOINTMENTS_FILE, appointments);
    }

    public List<Appointment> getAllAppointments() {
        return fileStorageService.readFromFile(APPOINTMENTS_FILE, Appointment.class);
    }
} 