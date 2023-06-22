package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ShareItServerTests {
    // Test class added ONLY to cover main() invocation not covered by application tests.
    @Test
    void contextLoads() {
        ShareItServer.main(new String[]{});
    }
}
