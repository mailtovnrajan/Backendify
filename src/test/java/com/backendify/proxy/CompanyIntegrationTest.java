package com.backendify.proxy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8080)
public class CompanyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
}
