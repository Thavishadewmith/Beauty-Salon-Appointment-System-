package com.example.salon;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "app.data.dir=target/test-data",
    "spring.main.allow-bean-definition-overriding=true"
})
class SalonApplicationTests {

    @Test
    void contextLoads() {
    }

}
