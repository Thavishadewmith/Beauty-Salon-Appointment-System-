package com.example.salon.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.*;
import java.nio.file.*;

@Service
public class FileStorageService {
    private static final String DATA_DIR = "data";
    private final ObjectMapper objectMapper;
    private final Path dataPath;

    public FileStorageService() {
        System.out.println("Initializing FileStorageService...");
        this.objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.dataPath = Paths.get(DATA_DIR).toAbsolutePath();
        initializeDataDirectory();
    }

    private void initializeDataDirectory() {
        try {
            System.out.println("Initializing data directory at: " + dataPath);
            
            // Create directory if it doesn't exist
            if (!Files.exists(dataPath)) {
                System.out.println("Creating data directory...");
                Files.createDirectories(dataPath);
                System.out.println("Data directory created successfully");
            } else {
                System.out.println("Data directory already exists");
            }
            
            // Verify directory is writable
            Path testFile = dataPath.resolve("test.txt");
            try {
                Files.writeString(testFile, "test");
                Files.delete(testFile);
                System.out.println("Data directory is writable");
            } catch (IOException e) {
                System.err.println("Data directory is not writable: " + e.getMessage());
                throw new RuntimeException("Data directory is not writable", e);
            }
        } catch (IOException e) {
            System.err.println("Error initializing data directory: " + e.getMessage());
            throw new RuntimeException("Could not initialize data directory", e);
        }
    }

    public <T> void saveToFile(String filename, List<T> data) {
        try {
            Path filePath = dataPath.resolve(filename);
            System.out.println("Saving to file: " + filePath);
            
            // Create file if it doesn't exist
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
            
            // Write data to file with proper formatting
            String jsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            System.out.println("JSON content to be saved: " + jsonContent);
            Files.writeString(filePath, jsonContent);
            System.out.println("File saved successfully");
            
        } catch (IOException e) {
            System.err.println("Error saving to file: " + filename);
            e.printStackTrace();
            throw new RuntimeException("Error saving to file: " + filename, e);
        }
    }

    public <T> List<T> readFromFile(String filename, Class<T> type) {
        try {
            Path filePath = dataPath.resolve(filename);
            System.out.println("Reading from file: " + filePath);
            
            // Create file with empty array if it doesn't exist
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
                Files.writeString(filePath, "[]");
                return new ArrayList<>();
            }
            
            // Read and parse file content
            String content = Files.readString(filePath);
            System.out.println("File content: " + content);
            
            if (content.trim().isEmpty()) {
                return new ArrayList<>();
            }
            
            List<T> result = objectMapper.readValue(content,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, type));
            System.out.println("Parsed " + result.size() + " items from file");
            return result;
                    
        } catch (IOException e) {
            System.err.println("Error reading file: " + filename);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void saveObject(String filename, Object data) {
        try {
            Path filePath = dataPath.resolve(filename);
            System.out.println("Saving object to file: " + filePath);
            
            // Ensure parent directory exists
            Files.createDirectories(filePath.getParent());
            
            // Create file if it doesn't exist
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
                System.out.println("Created new file: " + filename);
            }
            
            // Write data to file
            String jsonContent = objectMapper.writeValueAsString(data);
            System.out.println("JSON content to be saved: " + jsonContent);
            Files.writeString(filePath, jsonContent);
            System.out.println("Successfully saved object to file: " + filename);
            
            // Verify the file was written
            if (!Files.exists(filePath) || Files.size(filePath) == 0) {
                throw new RuntimeException("File was not written successfully");
            }
            
            // Verify the content can be read back
            String verifyContent = Files.readString(filePath);
            System.out.println("Verification - File content: " + verifyContent);
        } catch (IOException e) {
            System.err.println("Error saving object to file: " + filename);
            e.printStackTrace();
            throw new RuntimeException("Error saving to file: " + filename, e);
        }
    }

    public <T> T readObject(String filename, Class<T> type) {
        try {
            Path filePath = dataPath.resolve(filename);
            System.out.println("Reading object from file: " + filePath);
            
            if (!Files.exists(filePath)) {
                System.out.println("File does not exist: " + filePath);
                return null;
            }
            
            String content = Files.readString(filePath);
            System.out.println("File content: " + content);
            
            return objectMapper.readValue(content, type);
        } catch (IOException e) {
            System.err.println("Error reading object from file: " + filename);
            e.printStackTrace();
            throw new RuntimeException("Error reading from file: " + filename, e);
        }
    }
} 