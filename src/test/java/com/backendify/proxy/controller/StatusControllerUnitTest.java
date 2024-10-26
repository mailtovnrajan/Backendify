package com.backendify.proxy.controller;

import com.backendify.proxy.application.Application;
import com.backendify.proxy.service.MetricsService;
import com.timgroup.statsd.StatsDClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@ComponentScan(basePackages = "com.backendify.proxy")
public class StatusControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;  // Simulates HTTP requests to the controller

    @MockBean
    private MetricsService metricsService;

    @MockBean
    private StatsDClient statsDClient;

    @Test
    public void whenGetStatus_thenReturns200() throws Exception {
        // Perform a GET request to /status and verify 200 OK status and correct message
        mockMvc.perform(get("/status"))
                .andExpect(status().isOk())  // Verify HTTP 200 OK status
                .andExpect(content().string("Service is running"));  // Verify response content
    }
}
