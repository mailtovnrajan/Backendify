package com.backendify.proxy.service;

import com.backendify.proxy.exception.UnexpectedContentTypeException;
import com.backendify.proxy.model.CompanyResponse;
import com.backendify.proxy.model.CompanyV1Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CompanyService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Constructor injection for RestTemplate
    @Autowired
    public CompanyService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public CompanyResponse getCompany(String id, String countryIso) throws UnexpectedContentTypeException {
        // Return the URL based on the country ISO code
        String backendUrl = "http://localhost:8080/companies/" + id;

        // Call the backend service using RestTemplate
        ResponseEntity<String> response = restTemplate.getForEntity(backendUrl, String.class);
        String body = response.getBody();
        HttpHeaders headers = response.getHeaders();

        if (headers.getContentType() != null) {
            String contentType = headers.getContentType().toString();

            if ("application/x-company-v1".equals(contentType)) {
                return parseV1Response(id, body);
            } else if ("application/x-company-v2".equals(contentType)) {
                return parseV2Response(id, body);
            } else {
                throw new UnexpectedContentTypeException("Unsupported backend response type");
            }
        }
        throw new IllegalStateException("Content Type is null");
    }

    private CompanyResponse parseV1Response(String id, String body) {

        try {
            CompanyV1Response v1Response =  objectMapper.readValue(body, CompanyV1Response.class);;
            // Map fields from the V1 object to CompanyResponse
            String name = v1Response.getCompanyName();
            String activeUntil = v1Response.getClosedOn();

            return new CompanyResponse(id, name, false, activeUntil);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private CompanyResponse parseV2Response(String id, String body) {
        // Logic for parsing V2 backend response
        return new CompanyResponse(id, "Company V2", true, null);
    }

}
