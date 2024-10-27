package com.backendify.proxy.service;

import com.backendify.proxy.exception.*;
import com.backendify.proxy.model.CompanyResponse;
import com.backendify.proxy.model.CompanyV1Response;
import com.backendify.proxy.model.CompanyV2Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
    private final MetricsService metricsService;
    private final CacheManager cacheManager;

    // Constructor injection for RestTemplate
    @Autowired
    public CompanyService(RestTemplate restTemplate, ObjectMapper objectMapper, MetricsService metricsService, CacheManager cacheManager) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.metricsService = metricsService;
        this.cacheManager = cacheManager;
    }

    public void setBackendMappings(Map<String, String> backendMappings){
        this.backendMappings = backendMappings;
    }

//    @Cacheable(value = "companyCache", key = "#id.concat('-').concat(#countryIso)", unless = "#result == null")
    public CompanyResponse getCompany(String id, String countryIso) throws UnexpectedContentTypeException, BackendResponseFormatException, CompanyNotFoundException, CountryNotFoundException, BackendServerException, ConnectivityTimeoutException {
        metricsService.incrementRequestCount();  // Count total requests
        // Create a cache key
        String cacheKey = id.concat("-").concat(countryIso);
        CompanyResponse cachedResponse = (CompanyResponse) cacheManager.getCache("companyCache").get(cacheKey, CompanyResponse.class);

        // If we have a cached response, try to validate it using conditional requests
        HttpHeaders requestHeaders = new HttpHeaders();
        if (cachedResponse != null) {
            cachedResponse.setActiveUntil(formatToRFC3339(cachedResponse.getActiveUntil()));
            // Add ETag or Last-Modified headers to check if data has changed
            if (cachedResponse.getETag() != null) {
                requestHeaders.setIfNoneMatch(cachedResponse.getETag());
            }
            if (cachedResponse.getLastModified() != null) {
                requestHeaders.setIfModifiedSince(cachedResponse.getLastModified());
            }
        }

        try {
            // Return the URL based on the country ISO code
            String backendUrl = getBackendUrl(countryIso);
            // Make a conditional request to the backend
            ResponseEntity<String> response = restTemplate.getForEntity(backendUrl + "/companies/" + id, String.class, requestHeaders);
            if (response.getStatusCode() == HttpStatus.NOT_MODIFIED && cachedResponse != null) {
                // The data hasn't changed, use the cached response
                return cachedResponse;
            } else if (response.getStatusCode() == HttpStatus.OK) {
                // Update the cache with the new response data
                // Call the backend service using RestTemplate
                String body = response.getBody();
                HttpHeaders responseHeaders = response.getHeaders();

                if (responseHeaders.getContentType() != null) {
                    String contentType = responseHeaders.getContentType().toString();

                    if ("application/x-company-v1".equals(contentType)) {
                        metricsService.incrementCompanyV1ResponseCount();
                        CompanyResponse companyResponse = parseV1Response(id, body);
                        cacheManager.getCache("companyCache").put(cacheKey, companyResponse);
                        // Set ETag and Last-Modified headers
                        companyResponse.setETag(responseHeaders.getETag());
                        companyResponse.setLastModified(responseHeaders.getLastModified());
                        return companyResponse;
                    } else if ("application/x-company-v2".equals(contentType)) {
                        metricsService.incrementCompanyV2ResponseCount();
                        CompanyResponse companyResponse = parseV2Response(id, body);
                        cacheManager.getCache("companyCache").put(cacheKey, companyResponse);
                        // Set ETag and Last-Modified headers
                        companyResponse.setETag(responseHeaders.getETag());
                        companyResponse.setLastModified(responseHeaders.getLastModified());
                        return companyResponse;
                    } else {
                        metricsService.incrementUnexpectedContentTypeCount();
                        throw new UnexpectedContentTypeException("Unsupported backend response type");
                    }
                }
                throw new IllegalStateException("Content Type is null");
            } else{
                throw new UnexpectedContentTypeException("Unexpected response status: " + response.getStatusCode());
            }
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
        try {
            CompanyV1Response v1Response = objectMapper.readValue(body, CompanyV1Response.class);
            ;
            // Map fields from the V1 object to CompanyResponse
            String name = v1Response.getCompanyName();
            String closedOn = formatToRFC3339(v1Response.getClosedOn());
            boolean active = isActive(closedOn);
            metricsService.incrementCompanyV1ResponseCount();
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
            metricsService.incrementCompanyV2ResponseCount();
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
