package com.backendify.proxy;

import com.backendify.proxy.application.Application;
import com.backendify.proxy.service.CompanyService;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8080)
@ComponentScan(basePackages = "com.backendify.proxy")
public class CompanyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CompanyService companyService;

    @BeforeEach
    public void setUp() {
        // Set the backend mappings manually for the test
        Map<String, String> backendMappings = Map.of(
                "us", "http://localhost:8080",  // WireMock server URL
                "ru", "http://localhost:8080"
        );
        companyService.setBackendMappings(backendMappings);
    }

    // Integration test for successful company retrieval (V1)
    @Test
    public void whenGetCompanyV1_thenReturnsCompanyResponse() throws Exception {
        // Mock the backend response for a company (V1)
       stubFor(get(urlEqualTo("/companies/123"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/x-company-v1")
                        .withBody("{ \"cn\": \"Company V1\", \"created_on\": \"2020-01-01T00:00:00Z\" }")
                        .withStatus(200)));

        mockMvc.perform(MockMvcRequestBuilders.get("/company?id=123&country_iso=us")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("123")))
                .andExpect(jsonPath("$.name", is("Company V1")))
                .andExpect(jsonPath("$.active", is(true)));
    }

    // Test for successful V2 response
    @Test
    public void whenGetCompanyV2_thenReturnsCompanyResponse() throws Exception {
        // Stub a V2 response from the backend
        stubFor(get(urlEqualTo("/companies/456"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/x-company-v2")
                        .withBody("{ \"company_name\": \"Company V2\", \"tin\": \"123456789\", \"dissolved_on\": \"2022-12-31T00:00:00Z\" }")
                        .withStatus(200)));

        // Perform the request and check the result
        mockMvc.perform(MockMvcRequestBuilders.get("/company?id=456&country_iso=us")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("456")))
                .andExpect(jsonPath("$.name", is("Company V2")))
                .andExpect(jsonPath("$.active", is(false)))
                .andExpect(jsonPath("$.activeUntil", is("2022-12-31T00:00:00Z")));
    }
}
