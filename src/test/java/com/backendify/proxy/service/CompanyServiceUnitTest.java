package com.backendify.proxy.service;

import com.backendify.proxy.application.Application;
import com.backendify.proxy.exception.*;
import com.backendify.proxy.model.CompanyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@ComponentScan(basePackages = "com.backendify.proxy")
@EnableCaching
public class CompanyServiceUnitTest {

    @Autowired
    private CompanyService companyService;
    @MockBean
    private RestTemplate restTemplate;
    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    public void setUp() {
        // Setup the backend mappings for tests
        Map<String, String> backendMappings = Map.of(
                "us", "http://localhost:9001",
                "ru", "http://localhost:9002"
        );
        companyService.setBackendMappings(backendMappings);
        // Clear cache before each test
        if (cacheManager.getCache("companyCache") != null)
            cacheManager.getCache("companyCache").clear();
    }

    @Test
    public void whenGetCompanyV1_thenReturnCompanyResponse() throws UnexpectedContentTypeException, BackendResponseFormatException, CompanyNotFoundException, BackendServerException, ConnectivityTimeoutException, CountryNotFoundException {
        // Simulate V1 backend response
        String v1ResponseBody = "{\"cn\": \"Company V1\", \"created_on\": \"2022-01-01T00:00:00Z\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/x-company-v1"));
        
        // Mock the RestTemplate to return a V1 response
        ResponseEntity<String> responseEntity = new ResponseEntity<>(v1ResponseBody, headers, HttpStatus.OK);
        when(restTemplate.getForEntity(Mockito.eq("http://localhost:9001/companies/123"), Mockito.eq(String.class))).thenReturn(responseEntity);

        // Call the service method
        CompanyResponse companyResponse = companyService.getCompany("123", "us");

        // Verify the service's response
        assertEquals("123", companyResponse.getId());
        assertEquals("Company V1", companyResponse.getName());
        assertTrue(companyResponse.isActive());
    }

    @Test
    public void whenGetCompanyV2_thenReturnCompanyResponse() throws UnexpectedContentTypeException, BackendResponseFormatException, CompanyNotFoundException, BackendServerException, ConnectivityTimeoutException, CountryNotFoundException {
        // Simulate V2 backend response
        String v2ResponseBody = "{\"company_name\": \"Company V2\", \"tin\": \"2022-01-01T00:00:00Z\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/x-company-v2"));

        // Mock the RestTemplate to return a V2 response
        ResponseEntity<String> responseEntity = new ResponseEntity<>(v2ResponseBody, headers, HttpStatus.OK);
        when(restTemplate.getForEntity(Mockito.eq("http://localhost:9001/companies/123"), Mockito.eq(String.class))).thenReturn(responseEntity);

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
    public void whenCompanyV1IsInactive_thenParseCorrectly() throws UnexpectedContentTypeException, BackendResponseFormatException, CompanyNotFoundException, BackendServerException, ConnectivityTimeoutException, CountryNotFoundException {
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
    public void whenCompanyV2IsInactive_thenParseCorrectly() throws UnexpectedContentTypeException, BackendResponseFormatException, CompanyNotFoundException, BackendServerException, ConnectivityTimeoutException, CountryNotFoundException {
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
    public void whenCompanyV1IsActive_thenParseCorrectly() throws UnexpectedContentTypeException, BackendResponseFormatException, CompanyNotFoundException, BackendServerException, ConnectivityTimeoutException, CountryNotFoundException {
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
    public void whenCompanyV2IsActive_thenParseCorrectly() throws UnexpectedContentTypeException, BackendResponseFormatException, CompanyNotFoundException, BackendServerException, ConnectivityTimeoutException, CountryNotFoundException {
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

//    @Test
//    public void whenCompanyV1CloseOnIsGreaterThanCurrentDate_thenParseCorrectly() throws UnexpectedContentTypeException, BackendResponseFormatException, CompanyNotFoundException, BackendServerException, ConnectivityTimeoutException, CountryNotFoundException {
//        // Simulate V1 backend response
//        String v1ResponseBody = "{\"cn\": \"Backendify Ltd\", \"created_on\": \"2022-01-01T00:00:00Z\", \"closed_on\": \"2025-01-01T00:00:00Z\"}";
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.valueOf("application/x-company-v1"));
//
//        // Mock the RestTemplate to return a V1 response
//        ResponseEntity<String> responseEntity = new ResponseEntity<>(v1ResponseBody, headers, HttpStatus.OK);
//        when(restTemplate.getForEntity(anyString(), Mockito.eq(String.class))).thenReturn(responseEntity);
//
//        // Call the service method
//        CompanyResponse companyResponse = companyService.getCompany("Backendify", "us");
//
//        // Verify the service's response
//        assertEquals("Backendify", companyResponse.getId());
//        assertEquals("Backendify Ltd", companyResponse.getName());
//        assertTrue(companyResponse.isActive());
//        assertEquals("2025-01-01T00:00:00Z", companyResponse.getActiveUntil());
//
//    }
//
//    @Test
//    public void whenCompanyV2DissolvedOnIsGreaterThanCurrentDate_thenParseCorrectly() throws UnexpectedContentTypeException, BackendResponseFormatException, CompanyNotFoundException, BackendServerException, ConnectivityTimeoutException, CountryNotFoundException {
//        // Simulate V2 backend response
//        String v2ResponseBody = "{\"company_name\": \"Backendify Ltd\", \"tin\": \"123456\", \"dissolved_on\": \"2025-01-01T00:00:00Z\"}";
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.valueOf("application/x-company-v2"));
//
//        // Mock the RestTemplate to return a V1 response
//        ResponseEntity<String> responseEntity = new ResponseEntity<>(v2ResponseBody, headers, HttpStatus.OK);
//        when(restTemplate.getForEntity(anyString(), Mockito.eq(String.class))).thenReturn(responseEntity);
//
//        // Call the service method
//        CompanyResponse companyResponse = companyService.getCompany("123", "us");
//
//        // Verify the service's response
//        assertEquals("123", companyResponse.getId());
//        assertEquals("Backendify Ltd", companyResponse.getName());
//        assertTrue(companyResponse.isActive());
//        assertEquals("2025-01-01T00:00:00Z", companyResponse.getActiveUntil());
//
//    }

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

    @Test
    public void whenCountryCodeNotFound_thenThrowCountryNotFoundException() {
        // Call the public method getCompany which internally calls getBackendUrl
        // Simulate that no backend is configured for the "fr" country code
        assertThrows(CountryNotFoundException.class, () -> {
            companyService.getCompany("123", "fr");
        });
    }

    @Test
    public void whenGetCompany_thenCacheResponse() throws Exception, BackendResponseFormatException, CompanyNotFoundException, CountryNotFoundException, BackendServerException, ConnectivityTimeoutException {
        // Set up a valid backend response
        String validResponse = "{\"cn\": \"Test Company\", \"created_on\": \"2022-01-01T00:00:00Z\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.valueOf("application/x-company-v1"));
        ResponseEntity<String> responseEntity = new ResponseEntity<>(validResponse, headers, HttpStatus.OK);

        // Mock the backend call to return the valid response
        when(restTemplate.getForEntity(Mockito.eq("http://localhost:9001/companies/123"), Mockito.eq(String.class))).thenReturn(responseEntity);

        // First call (should make the backend request)
        CompanyResponse response = companyService.getCompany("123", "us");

        // Verify that the response is as expected
        assertNotNull(response);
        assertEquals("123", response.getId());
        assertEquals("Test Company", response.getName());

        // Check if the response is cached
        CompanyResponse cachedResponse = (CompanyResponse) cacheManager.getCache("companyCache").get("123-us").get();
        assertNotNull(cachedResponse);
        assertEquals("Test Company", cachedResponse.getName());
    }

    @Test
    public void whenBackendFails_thenReturnCachedResponse() throws Exception, BackendResponseFormatException, CompanyNotFoundException, CountryNotFoundException, BackendServerException, ConnectivityTimeoutException {
        // Set up a valid backend response to be cached initially
        String validResponse = "{\"cn\": \"Test Company\", \"created_on\": \"2022-01-01T00:00:00Z\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.valueOf("application/x-company-v1"));
        ResponseEntity<String> responseEntity = new ResponseEntity<>(validResponse, headers, HttpStatus.OK);

        // Mock the backend call to return the valid response
        when(restTemplate.getForEntity(Mockito.eq("http://localhost:9001/companies/123"), Mockito.eq(String.class))).thenReturn(responseEntity);

        // First call (should make the backend request and cache the response)
        CompanyResponse response = companyService.getCompany("123", "us");

        // Verify the response and that it's cached
        assertNotNull(response);
        assertEquals("Test Company", response.getName());

        // Simulate backend failure with an unchecked exception (like ResourceAccessException)
        Mockito.reset(restTemplate);  // Reset the previous mock setup
        when(restTemplate.getForEntity(anyString(), Mockito.eq(String.class))).thenThrow(new ResourceAccessException("Backend timed out"));

        // Second call (should return cached response, even though the backend fails)
        CompanyResponse cachedResponse = companyService.getCompany("123", "us");

        // Verify the cached response is returned
        assertNotNull(cachedResponse);
        assertEquals("Test Company", cachedResponse.getName());
    }

    @Test
    public void whenCacheExpires_thenBackendIsCalledAgain() throws Exception, BackendResponseFormatException, CompanyNotFoundException, CountryNotFoundException, BackendServerException, ConnectivityTimeoutException {
        // Mock a valid backend response
        String validResponse = "{\"cn\": \"Test Company\", \"created_on\": \"2022-01-01T00:00:00Z\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.valueOf("application/x-company-v1"));
        ResponseEntity<String> responseEntity = new ResponseEntity<>(validResponse, headers, HttpStatus.OK);

        // Mock the backend call
        when(restTemplate.getForEntity(Mockito.eq("http://localhost:9001/companies/123"), Mockito.eq(String.class))).thenReturn(responseEntity);

        // First call should cache the response
        companyService.getCompany("123", "us");

        // Simulate cache expiration manually (for test purposes)
        cacheManager.getCache("companyCache").evict("123-us");

        // Make the backend call again (should hit the backend because cache is cleared)
        Mockito.reset(restTemplate);
        when(restTemplate.getForEntity(anyString(), Mockito.eq(String.class))).thenReturn(responseEntity);

        CompanyResponse responseAfterCacheEvict = companyService.getCompany("123", "us");
        assertNotNull(responseAfterCacheEvict);
        assertEquals("Test Company", responseAfterCacheEvict.getName());
    }

    @Test
    public void testFormatToRFC3339_withInvalidDate() {
        String invalidDate = "2022-01-01 00:00:00";

        assertThrows(DateTimeParseException.class, () -> {
            companyService.formatToRFC3339(invalidDate);
        });
    }

    @Test
    public void testFormatToRFC3339_withValidDate() {
        String validDate = "2022-01-01T00:00:00Z";
        String formattedDate = companyService.formatToRFC3339(validDate);

        assertEquals("2022-01-01T00:00:00Z", formattedDate);
    }

    @Test
    public void testFormatToRFC3339_withNonUTCDate() {
        String nonUTCDate = "2022-01-01T00:00:00+02:00";
        String formattedDate = companyService.formatToRFC3339(nonUTCDate);

        assertEquals("2021-12-31T22:00:00Z", formattedDate);  // Converts to UTC
    }

}



