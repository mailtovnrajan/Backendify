package com.backendify.proxy.service;

import com.backendify.proxy.exception.*;
import com.backendify.proxy.model.CompanyResponse;
import com.backendify.proxy.model.CompanyV1Response;
import com.backendify.proxy.model.CompanyV2Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Service
public class CompanyService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private Map<String, String> backendMappings;
    private final MetricsService metricsService;

    // Constructor injection for RestTemplate
    @Autowired
    public CompanyService(RestTemplate restTemplate, ObjectMapper objectMapper, MetricsService metricsService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.metricsService = metricsService;
    }

    public void setBackendMappings(Map<String, String> backendMappings){
        this.backendMappings = backendMappings;
    }

    @Cacheable(value = "companyCache", key = "#id.concat('-').concat(#countryIso)", unless = "#result == null")
    public CompanyResponse getCompany(String id, String countryIso) throws UnexpectedContentTypeException, BackendResponseFormatException, CompanyNotFoundException, CountryNotFoundException, BackendServerException, ConnectivityTimeoutException {
        metricsService.incrementRequestCount();  // Count total requests

        try {
            // Return the URL based on the country ISO code
            String backendUrl = getBackendUrl(countryIso);
            // Call the backend service using RestTemplate
            ResponseEntity<String> response = restTemplate.getForEntity(backendUrl + "/companies/" + id, String.class);

            String body = response.getBody();
            HttpHeaders responseHeaders = response.getHeaders();
            if (responseHeaders.getContentType() != null) {
                String contentType = responseHeaders.getContentType().toString();

                if ("application/x-company-v1".equals(contentType)) {
                    metricsService.incrementCompanyV1ResponseCount();
                    return parseV1Response(id, body);
                } else if ("application/x-company-v2".equals(contentType)) {
                    metricsService.incrementCompanyV2ResponseCount();
                    return parseV2Response(id, body);
                } else {
                    metricsService.incrementUnexpectedContentTypeCount();
                    throw new UnexpectedContentTypeException("Unsupported backend response type");
                }
            }
                throw new IllegalStateException("Content Type is null");
        } catch (HttpClientErrorException.NotFound e) {
            throw new CompanyNotFoundException("Company not found");
        } catch (HttpServerErrorException e) {
            metricsService.incrementBackendErrorCount();
            throw new BackendServerException("Backend server error: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            metricsService.incrementBackendErrorCount();
            throw new ConnectivityTimeoutException("Timeout or connectivity issue with backend: " + e.getMessage(), e);
        }
    }

    private String getBackendUrl(String countryCode) throws CountryNotFoundException {
        if (!backendMappings.containsKey(countryCode))
            throw new CountryNotFoundException("No backend configured for country code: " + countryCode);

        return backendMappings.get(countryCode);
    }

    private CompanyResponse parseV1Response(String id, String body) throws BackendResponseFormatException {
        // Logic for parsing V1 backend response
        try {
            CompanyV1Response v1Response = objectMapper.readValue(body, CompanyV1Response.class);
            // Map fields from the V1 object to CompanyResponse
            String name = v1Response.getCompanyName();
            String closedOn = v1Response.getClosedOn();
            boolean active = isActive(closedOn);
            metricsService.incrementCompanyV1ResponseCount();
            return new CompanyResponse(id, name, active, closedOn);
        } catch(JsonProcessingException | DateTimeParseException e) {
            throw new BackendResponseFormatException(e);
        }
    }

    private CompanyResponse parseV2Response(String id, String body) throws BackendResponseFormatException {
        // Logic for parsing V2 backend response
        try {
            CompanyV2Response v2Response = objectMapper.readValue(body, CompanyV2Response.class);
            // Map fields from the V2 object to CompanyResponse
            String name = v2Response.getCompanyName();
            String dissolvedOn = v2Response.getDissolvedOn();
            boolean active = isActive(dissolvedOn);
            metricsService.incrementCompanyV2ResponseCount();
            return new CompanyResponse(id, name, active, dissolvedOn);
        } catch(JsonProcessingException | DateTimeParseException e) {
            throw new BackendResponseFormatException(e);
        }
    }

    private boolean isActive(String closedOn) {
        if (closedOn == null) return true;

        // Parse the closedOn string using RFC 3339 format
        OffsetDateTime closedOnDateTime = OffsetDateTime.parse(closedOn, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        // Compare the current date-time in UTC with closed_on date-time
        return OffsetDateTime.now(ZoneOffset.UTC).isBefore(closedOnDateTime);
    }


}
