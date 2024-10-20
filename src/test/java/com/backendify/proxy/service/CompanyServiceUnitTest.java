package com.backendify.proxy.service;

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
import org.springframework.web.client.RestTemplate;

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
    public void whenGetCompanyV1_thenReturnCompanyResponse() {
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
}
