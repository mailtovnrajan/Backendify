package com.backendify.proxy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(StatusController.class)  // Test only the controller layer
public class StatusControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;  // Simulates HTTP requests to the controller

    @Test
    public void whenGetStatus_thenReturns200() throws Exception {
        // Perform a GET request to /status and verify 200 OK status and correct message
        mockMvc.perform(get("/status"))
                .andExpect(status().isOk())  // Verify HTTP 200 OK status
                .andExpect(content().string("Service is running"));  // Verify response content
    }
}
