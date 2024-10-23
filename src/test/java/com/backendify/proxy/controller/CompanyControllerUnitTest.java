package com.backendify.proxy.controller;

import com.backendify.proxy.application.Application;
import com.backendify.proxy.exception.*;
import com.backendify.proxy.model.CompanyResponse;
import com.backendify.proxy.service.CompanyService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@ComponentScan(basePackages = "com.backendify.proxy")
public class CompanyControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    //Test for successful retrieval response (200 OK)
    @Test
    public void whenGetCompany_thenReturns200() throws Exception, BackendResponseFormatException, CompanyNotFoundException, BackendServerException, ConnectivityTimeoutException, CountryNotFoundException {
        CompanyResponse mockResponse = new CompanyResponse("123", "Company1", true, null);
        Mockito.when(companyService.getCompany("123", "us")).thenReturn(mockResponse);

        mockMvc.perform(get("/company?id=123&country_iso=us"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.name").value("Company1"))
                .andExpect(jsonPath("$.active").value(true));
    }

    // Test for CompanyNotFoundException (404 Not Found)
    @Test
    public void whenCompanyNotFound_thenReturns404() throws Exception, BackendResponseFormatException, CompanyNotFoundException, CountryNotFoundException, BackendServerException, ConnectivityTimeoutException {
        // Simulate CompanyNotFoundException
        doThrow(new CompanyNotFoundException("Company not found"))
                .when(companyService).getCompany(anyString(), anyString());

        mockMvc.perform(get("/company")
                        .param("id", "123")
                        .param("country_iso", "us")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());  // Expect 404 Not Found
    }

    // Test for BackendServerException (500 Internal Server Error)
    @Test
    public void whenBackendServerError_thenReturns500() throws Exception, BackendResponseFormatException, CompanyNotFoundException, CountryNotFoundException, BackendServerException, ConnectivityTimeoutException {
        // Simulate BackendServerException
        doThrow(new BackendServerException("Backend server error"))
                .when(companyService).getCompany(anyString(), anyString());

        mockMvc.perform(get("/company")
                        .param("id", "123")
                        .param("country_iso", "us")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());  // Expect 500 Internal Server Error
    }

    // Test for ConnectivityTimeoutException (504 Gateway Timeout)
    @Test
    public void whenConnectivityTimeout_thenReturns504() throws Exception, BackendResponseFormatException, CompanyNotFoundException, CountryNotFoundException, BackendServerException, ConnectivityTimeoutException {
        // Simulate ConnectivityTimeoutException
        doThrow(new ConnectivityTimeoutException("Timeout or connectivity issue"))
                .when(companyService).getCompany(anyString(), anyString());

        mockMvc.perform(get("/company")
                        .param("id", "123")
                        .param("country_iso", "us")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isGatewayTimeout());  // Expect 504 Gateway Timeout
    }

    // Test for UnexpectedContentTypeException (415 Unsupported Media Type)
    @Test
    public void whenUnexpectedContentType_thenReturns415() throws Exception, BackendResponseFormatException, CompanyNotFoundException, CountryNotFoundException, BackendServerException, ConnectivityTimeoutException {
        // Simulate UnexpectedContentTypeException
        doThrow(new UnexpectedContentTypeException("Unsupported content type"))
                .when(companyService).getCompany(anyString(), anyString());

        mockMvc.perform(get("/company")
                        .param("id", "123")
                        .param("country_iso", "us")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnsupportedMediaType());  // Expect 415 Unsupported Media Type
    }

    // Test for BackendResponseFormatException (502 Bad Gateway)
    @Test
    public void whenBackendResponseFormatError_thenReturns502() throws Exception, BackendResponseFormatException, CompanyNotFoundException, CountryNotFoundException, BackendServerException, ConnectivityTimeoutException {
        // Simulate BackendResponseFormatException
        doThrow(new BackendResponseFormatException("Backend response format error"))
                .when(companyService).getCompany(anyString(), anyString());

        mockMvc.perform(get("/company")
                        .param("id", "123")
                        .param("country_iso", "us")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway());  // Expect 502 Bad Gateway
    }

}