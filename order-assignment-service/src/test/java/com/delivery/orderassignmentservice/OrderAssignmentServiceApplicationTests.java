package com.delivery.orderassignmentservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestWebSocketConfig.class)

class OrderAssignmentServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
