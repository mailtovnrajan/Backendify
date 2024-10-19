package com.backendify.proxy;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class CompanyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setupWireMock() {
        WireMock.reset();
    }

    @Test
    public void whenGetCompanyV1_thenReturns200() throws Exception {
        // Stub the backend V1 response using WireMock
        stubFor(get(urlEqualTo("/companies/123"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/x-company-v1")
                        .withBody("{\"cn\": \"Company V1\", \"created_on\": \"2022-01-01T00:00:00Z\"}")));

        // Perform the request to /company endpoint
        mockMvc.perform(MockMvcRequestBuilders.get("/company?id=123&country_iso=us"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.name").value("Company V1"))
                .andExpect(jsonPath("$.active").value(true));
    }
}
