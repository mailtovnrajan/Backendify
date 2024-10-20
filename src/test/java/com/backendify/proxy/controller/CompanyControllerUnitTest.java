package com.backendify.proxy.controller;

import com.backendify.proxy.exception.BackendResponseFormatException;
import com.backendify.proxy.model.CompanyResponse;
import com.backendify.proxy.service.CompanyService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyController.class)
public class CompanyControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @Test
    public void whenGetCompany_thenReturns200() throws Exception, BackendResponseFormatException {
        CompanyResponse mockResponse = new CompanyResponse("123", "Company1", true, null);
        Mockito.when(companyService.getCompany("123", "us")).thenReturn(mockResponse);

        mockMvc.perform(get("/company?id=123&country_iso=us"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.name").value("Company1"))
                .andExpect(jsonPath("$.active").value(true));
    }
}