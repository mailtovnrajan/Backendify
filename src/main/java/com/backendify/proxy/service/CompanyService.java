package com.backendify.proxy.service;

import com.backendify.proxy.exception.*;
import com.backendify.proxy.model.CompanyResponse;
import com.backendify.proxy.model.CompanyV1Response;
import com.backendify.proxy.model.CompanyV2Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Service
public class CompanyService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private Map<String, String> backendMappings;
    private final StatsDClient statsDClient;

    // Constructor injection for RestTemplate
    @Autowired
    public CompanyService(RestTemplate restTemplate, ObjectMapper objectMapper, StatsDClient statsDClient) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.statsDClient = statsDClient;
    }

    public void setBackendMappings(Map<String, String> backendMappings){
        this.backendMappings = backendMappings;
    }

    @Cacheable(value = "companyCache", key = "#id.concat('-').concat(#countryIso)", unless = "#result == null")
    public CompanyResponse getCompany(String id, String countryIso) throws UnexpectedContentTypeException, BackendResponseFormatException, CompanyNotFoundException, CountryNotFoundException, BackendServerException, ConnectivityTimeoutException {
        statsDClient.incrementCounter("metric.1");  // Count total requests
        try {
            // Return the URL based on the country ISO code
            String backendUrl = getBackendUrl(countryIso);

            // Call the backend service using RestTemplate
            ResponseEntity<String> response = restTemplate.getForEntity(backendUrl + "/companies/" + id, String.class);
            String body = response.getBody();
            HttpHeaders headers = response.getHeaders();

            if (headers.getContentType() != null) {
                String contentType = headers.getContentType().toString();

                if ("application/x-company-v1".equals(contentType)) {
                    return parseV1Response(id, body);
                } else if ("application/x-company-v2".equals(contentType)) {
                    return parseV2Response(id, body);
                } else {
                    statsDClient.incrementCounter("metric.4");
                    throw new UnexpectedContentTypeException("Unsupported backend response type");
                }
            }
            throw new IllegalStateException("Content Type is null");
        } catch (HttpClientErrorException.NotFound e) {
            throw new CompanyNotFoundException("Company not found");
        } catch (HttpServerErrorException e) {
            statsDClient.incrementCounter("metric.5");
            throw new BackendServerException("Backend server error: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            statsDClient.incrementCounter("metric.5");
            throw new ConnectivityTimeoutException("Timeout or connectivity issue with backend: " + e.getMessage(), e);
        }

    }

    private String getBackendUrl(String countryCode) throws CountryNotFoundException {
        if (!backendMappings.containsKey(countryCode))
            throw new CountryNotFoundException("No backend configured for country code: " + countryCode);

        return backendMappings.get(countryCode);
    }

    private CompanyResponse parseV1Response(String id, String body) throws BackendResponseFormatException {
        try {
            CompanyV1Response v1Response = objectMapper.readValue(body, CompanyV1Response.class);
            ;
            // Map fields from the V1 object to CompanyResponse
            String name = v1Response.getCompanyName();
            String closedOn = formatToRFC3339(v1Response.getClosedOn());
            boolean active = isActive(closedOn);
            statsDClient.incrementCounter("metric.2");
            return new CompanyResponse(id, name, active, closedOn);
        } catch(JsonProcessingException | DateTimeParseException e) {
            throw new BackendResponseFormatException(e);
        }
    }

    private boolean isActive(String closedOn) {
        if (closedOn == null) return true;

        LocalDateTime closedOnDateTime = LocalDateTime.parse(closedOn, DateTimeFormatter.ISO_DATE_TIME);

        // Compare the current date with closed_on date
        return LocalDateTime.now().isBefore(closedOnDateTime);
    }

    private CompanyResponse parseV2Response(String id, String body) throws BackendResponseFormatException {
        // Logic for parsing V2 backend response
        try {
            CompanyV2Response v2Response = objectMapper.readValue(body, CompanyV2Response.class);
            ;
            // Map fields from the V1 object to CompanyResponse
            String name = v2Response.getCompanyName();
            String closedOn = formatToRFC3339(v2Response.getDissolvedOn());
            boolean active = isActive(closedOn);
            statsDClient.incrementCounter("metric.3");
            return new CompanyResponse(id, name, active, closedOn);
        } catch(JsonProcessingException | DateTimeParseException e) {
            throw new BackendResponseFormatException(e);
        }
    }

    String formatToRFC3339(String dateStr) throws DateTimeParseException {
        if (dateStr == null) return null;

        ZonedDateTime date = ZonedDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC));
        return date.format(DateTimeFormatter.ISO_INSTANT);  // Ensure RFC3339 UTC format with 'Z' offset
    }

}
