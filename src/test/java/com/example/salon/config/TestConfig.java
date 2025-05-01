package com.example.salon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.TestPropertySource;

@Configuration
@Profile("test")
@TestPropertySource(properties = {
    "app.data.dir=target/test-data",
    "spring.main.allow-bean-definition-overriding=true"
})
public class TestConfig {
} 