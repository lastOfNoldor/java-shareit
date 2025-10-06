package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = ShareItApp.class)
@TestPropertySource(locations = "classpath:application.properties")
class ShareItTests {

    @Test
    void contextLoads() {
    }
}
