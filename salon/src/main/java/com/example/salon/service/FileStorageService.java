package com.example.salon.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.*;
import java.nio.file.*;

@Service
public class FileStorageService {
    private static final String DATA_DIR = "data";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FileStorageService() {
        createDataDirectory();
    }

    private void createDataDirectory() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Could not create data directory", e);
        }
    }

    public <T> void saveToFile(String filename, List<T> data) {
        try {
            String filePath = DATA_DIR + "/" + filename;
            objectMapper.writeValue(new File(filePath), data);
        } catch (IOException e) {
            throw new RuntimeException("Error saving to file: " + filename, e);
        }
    }

    public <T> List<T> readFromFile(String filename, Class<T> type) {
        try {
            String filePath = DATA_DIR + "/" + filename;
            File file = new File(filePath);
            if (!file.exists()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(file,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, type));
        } catch (IOException e) {
            throw new RuntimeException("Error reading from file: " + filename, e);
        }
    }

    public void saveObject(String filename, Object data) {
        try {
            String filePath = DATA_DIR + "/" + filename;
            objectMapper.writeValue(new File(filePath), data);
        } catch (IOException e) {
            throw new RuntimeException("Error saving to file: " + filename, e);
        }
    }

    public <T> T readObject(String filename, Class<T> type) {
        try {
            String filePath = DATA_DIR + "/" + filename;
            File file = new File(filePath);
            if (!file.exists()) {
                return null;
            }
            return objectMapper.readValue(file, type);
        } catch (IOException e) {
            throw new RuntimeException("Error reading from file: " + filename, e);
        }
    }
} 