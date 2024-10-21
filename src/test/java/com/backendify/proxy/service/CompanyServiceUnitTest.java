package com.backendify.proxy.service;

import com.backendify.proxy.exception.*;
import com.backendify.proxy.model.CompanyResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
public class CompanyServiceUnitTest {

    @Autowired
    private CompanyService companyService;
    @MockBean
    private RestTemplate restTemplate;

    @Test
    public void whenGetCompanyV1_thenReturnCompanyResponse() throws UnexpectedContentTypeException, BackendResponseFormatException, CompanyNotFoundException, BackendServerException, ConnectivityTimeoutException {
        // Simulate V1 backend response
        String v1ResponseBody = "{\"cn\": \"Company V1\", \"created_on\": \"2022-01-01T00:00:00Z\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/x-company-v1"));
        
        // Mock the RestTemplate to return a V1 response
        ResponseEntity<String> responseEntity = new ResponseEntity<>(v1ResponseBody, headers, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), Mockito.eq(String.class))).thenReturn(responseEntity);

        // Call the service method
        CompanyResponse companyResponse = companyService.getCompany("123", "us");

        // Verify the service's response
        assertEquals("123", companyResponse.getId());
        assertEquals("Company V1", companyResponse.getName());
        assertTrue(companyResponse.isActive());
    }

    @Test
    public void whenGetCompanyV2_thenReturnCompanyResponse() throws UnexpectedContentTypeException, BackendResponseFormatException, CompanyNotFoundException, BackendServerException, ConnectivityTimeoutException {
        // Simulate V2 backend response
        String v2ResponseBody = "{\"company_name\": \"Company V2\", \"tin\": \"2022-01-01T00:00:00Z\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/x-company-v2"));

        // Mock the RestTemplate to return a V2 response
        ResponseEntity<String> responseEntity = new ResponseEntity<>(v2ResponseBody, headers, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), Mockito.eq(String.class))).thenReturn(responseEntity);

        // Call the service method
        CompanyResponse companyResponse = companyService.getCompany("123", "us");

        // Verify the service's response
        assertEquals("123", companyResponse.getId());
        assertEquals("Company V2", companyResponse.getName());
        assertTrue(companyResponse.isActive());
    }

    @Test
    public void whenUnExpectedContentType_thenThrowException() {
        // Simulate unsupported content type response
        String unsupportedResponseBody = "{}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/unsupported"));

        // Mock the RestTemplate to return an unsupported content type response
        ResponseEntity<String> responseEntity = new ResponseEntity<>(unsupportedResponseBody, headers, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), Mockito.eq(String.class))).thenReturn(responseEntity);

        // Expect a RuntimeException due to unsupported content type
        assertThrows(UnexpectedContentTypeException.class, () -> {
            companyService.getCompany("123", "us");
        });
    }

    @Test
    public void whenContentTypeIsNull_thenThrowException() {
        // Simulate unsupported content type response
        String v1ResponseBody = "{\"cn\": \"Company V1\", \"created_on\": \"2022-01-01T00:00:00Z\"}";
        HttpHeaders headers = new HttpHeaders();

        // Mock the RestTemplate to return an unsupported content type response
        ResponseEntity<String> responseEntity = new ResponseEntity<>(v1ResponseBody, headers, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), Mockito.eq(String.class))).thenReturn(responseEntity);

        // Expect a RuntimeException due to unsupported content type
        assertThrows(IllegalStateException.class, () -> {
            companyService.getCompany("123", "us");
        });
    }

    @Test
    public void whenCompanyV1IsInactive_thenParseCorrectly() throws UnexpectedContentTypeException, BackendResponseFormatException, CompanyNotFoundException, BackendServerException, ConnectivityTimeoutException {
        // Simulate V1 backend response
        String v1ResponseBody = "{\"cn\": \"Backendify Ltd\", \"created_on\": \"2022-01-01T00:00:00Z\", \"closed_on\": \"2022-01-28T00:00:00Z\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/x-company-v1"));

        // Mock the RestTemplate to return a V1 response
        ResponseEntity<String> responseEntity = new ResponseEntity<>(v1ResponseBody, headers, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), Mockito.eq(String.class))).thenReturn(responseEntity);

        // Call the service method
        CompanyResponse companyResponse = companyService.getCompany("123", "us");

        // Verify the service's response
        assertEquals("123", companyResponse.getId());
        assertEquals("Backendify Ltd", companyResponse.getName());
        assertFalse(companyResponse.isActive());
        assertEquals("2022-01-28T00:00:00Z", companyResponse.getActiveUntil());
    }

    @Test
    public void whenCompanyV2IsInactive_thenParseCorrectly() throws UnexpectedContentTypeException, BackendResponseFormatException, CompanyNotFoundException, BackendServerException, ConnectivityTimeoutException {
        // Simulate V2 backend response
        String v2ResponseBody = "{\"company_name\": \"Backendify Ltd\", \"tin\": \"123456\", \"dissolved_on\": \"2022-01-28T00:00:00Z\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/x-company-v2"));

        // Mock the RestTemplate to return a V1 response
        ResponseEntity<String> responseEntity = new ResponseEntity<>(v2ResponseBody, headers, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), Mockito.eq(String.class))).thenReturn(responseEntity);

        // Call the service method
        CompanyResponse companyResponse = companyService.getCompany("123", "us");

        // Verify the service's response
        assertEquals("123", companyResponse.getId());
        assertEquals("Backendify Ltd", companyResponse.getName());
        assertFalse(companyResponse.isActive());
        assertEquals("2022-01-28T00:00:00Z", companyResponse.getActiveUntil());
    }

    @Test
    public void whenCompanyV1IsActive_thenParseCorrectly() throws UnexpectedContentTypeException, BackendResponseFormatException, CompanyNotFoundException, BackendServerException, ConnectivityTimeoutException {
        // Simulate V1 backend response
        String v1ResponseBody = "{\"cn\": \"Backendify Ltd\", \"created_on\": \"2022-01-01T00:00:00Z\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/x-company-v1"));

        // Mock the RestTemplate to return a V1 response
        ResponseEntity<String> responseEntity = new ResponseEntity<>(v1ResponseBody, headers, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), Mockito.eq(String.class))).thenReturn(responseEntity);

        // Call the service method
        CompanyResponse companyResponse = companyService.getCompany("Backendify", "us");

        // Verify the service's response
        assertEquals("Backendify", companyResponse.getId());
        assertEquals("Backendify Ltd", companyResponse.getName());
        assertTrue(companyResponse.isActive());
    }

    @Test
    public void whenCompanyV2IsActive_thenParseCorrectly() throws UnexpectedContentTypeException, BackendResponseFormatException, CompanyNotFoundException, BackendServerException, ConnectivityTimeoutException {
        // Simulate V2 backend response
        String v2ResponseBody = "{\"company_name\": \"Backendify Ltd\", \"tin\": \"123456\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/x-company-v2"));

        // Mock the RestTemplate to return a V1 response
        ResponseEntity<String> responseEntity = new ResponseEntity<>(v2ResponseBody, headers, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), Mockito.eq(String.class))).thenReturn(responseEntity);

        // Call the service method
        CompanyResponse companyResponse = companyService.getCompany("123", "us");

        // Verify the service's response
        assertEquals("123", companyResponse.getId());
        assertEquals("Backendify Ltd", companyResponse.getName());
        assertTrue(companyResponse.isActive());
    }

    @Test
    public void whenCompanyV1CloseOnIsGreaterThanCurrentDate_thenParseCorrectly() throws UnexpectedContentTypeException, BackendResponseFormatException, CompanyNotFoundException, BackendServerException, ConnectivityTimeoutException {
        // Simulate V1 backend response
        String v1ResponseBody = "{\"cn\": \"Backendify Ltd\", \"created_on\": \"2022-01-01T00:00:00Z\", \"closed_on\": \"2025-01-01T00:00:00Z\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/x-company-v1"));

        // Mock the RestTemplate to return a V1 response
        ResponseEntity<String> responseEntity = new ResponseEntity<>(v1ResponseBody, headers, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), Mockito.eq(String.class))).thenReturn(responseEntity);

        // Call the service method
        CompanyResponse companyResponse = companyService.getCompany("Backendify", "us");

        // Verify the service's response
        assertEquals("Backendify", companyResponse.getId());
        assertEquals("Backendify Ltd", companyResponse.getName());
        assertTrue(companyResponse.isActive());
        assertEquals("2025-01-01T00:00:00Z", companyResponse.getActiveUntil());

    }

    @Test
    public void whenCompanyV2DissolvedOnIsGreaterThanCurrentDate_thenParseCorrectly() throws UnexpectedContentTypeException, BackendResponseFormatException, CompanyNotFoundException, BackendServerException, ConnectivityTimeoutException {
        // Simulate V2 backend response
        String v2ResponseBody = "{\"company_name\": \"Backendify Ltd\", \"tin\": \"123456\", \"dissolved_on\": \"2025-01-01T00:00:00Z\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/x-company-v2"));

        // Mock the RestTemplate to return a V1 response
        ResponseEntity<String> responseEntity = new ResponseEntity<>(v2ResponseBody, headers, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), Mockito.eq(String.class))).thenReturn(responseEntity);

        // Call the service method
        CompanyResponse companyResponse = companyService.getCompany("123", "us");

        // Verify the service's response
        assertEquals("123", companyResponse.getId());
        assertEquals("Backendify Ltd", companyResponse.getName());
        assertTrue(companyResponse.isActive());
        assertEquals("2025-01-01T00:00:00Z", companyResponse.getActiveUntil());

    }

    @Test
    public void whenMalformedJson_thenThrowAppropriateException() {
        // Simulate unsupported content type response
        String v1ResponseBody = "{\"malformed_json\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/x-company-v1"));

        // Mock the RestTemplate to return an unsupported content type response
        ResponseEntity<String> responseEntity = new ResponseEntity<>(v1ResponseBody, headers, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), Mockito.eq(String.class))).thenReturn(responseEntity);

        // Expect a custom Exception due to unsupported content type
        assertThrows(BackendResponseFormatException.class, () -> {
            companyService.getCompany("123", "us");
        });
    }

    @Test
    public void whenBackendServerError_thenThrowAppropriateException() {
        // Simulate a server error (5xx)
        when(restTemplate.getForEntity(anyString(), Mockito.eq(String.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // Expect a custom Exception
        assertThrows(BackendServerException.class, () -> {
            companyService.getCompany("123", "us");
        });
    }

    @Test
    public void whenBackendTimesOut_thenThrowAppropriateException() {
        // Simulate a timeout (ResourceAccessException)
        when(restTemplate.getForEntity(anyString(), Mockito.eq(String.class)))
                .thenThrow(new ResourceAccessException("Backend timed out"));

        // Expect a custom Exception
        assertThrows(ConnectivityTimeoutException.class, () -> {
            companyService.getCompany("123", "us");
        });
    }

    @Test
    public void whenCompanyNotFound_thenThrowCompanyNotFoundException() {
        // Simulate a 404 Not Found response from the backend
        when(restTemplate.getForEntity(anyString(), Mockito.eq(String.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND,
                        "Company not found",
                        HttpHeaders.EMPTY,
                        null,
                        StandardCharsets.UTF_8
                ));
        assertThrows(CompanyNotFoundException.class, () -> {
            companyService.getCompany("999", "us");
        });
    }
}
