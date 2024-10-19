package com.backendify.proxy;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(CompanyController.class)
public class CompanyControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @Test
    public void whenGetCompany_thenReturns200() throws Exception {
        CompanyResponse mockResponse = new CompanyResponse("123", "Company1", true, null);
        Mockito.when(companyService.getCompany("123", "us")).thenReturn(mockResponse);

        mockMvc.perform(get("/company?id=123&country_iso=us"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.name").value("Company1"))
                .andExpect(jsonPath("$.active").value(true));
    }
}
