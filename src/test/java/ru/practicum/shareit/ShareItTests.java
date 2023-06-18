package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ShareItTests {
    // Test class added ONLY to cover main() invocation not covered by application tests.
    @Test
    void contextLoads() {
        ShareItApp.main(new String[] {});
    }
}
