package com.backendify.proxy.service;

import com.backendify.proxy.model.CompanyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CompanyService {

    private final RestTemplate restTemplate;

    // Constructor injection for RestTemplate
    @Autowired
    public CompanyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CompanyResponse getCompany(String id, String countryIso) {
        // Return the URL based on the country ISO code
        String backendUrl = "http://localhost:8080/companies/" + id;

        // Call the backend service using RestTemplate
        ResponseEntity<String> response = restTemplate.getForEntity(backendUrl, String.class);
        String body = response.getBody();
        HttpHeaders headers = response.getHeaders();

        String contentType = headers.getContentType().toString();

        if ("application/x-company-v1".equals(contentType)) {
            return parseV1Response(id, body);
        } else if ("application/x-company-v2".equals(contentType)) {
            return parseV2Response(id, body);
        } else {
            throw new RuntimeException("Unsupported backend response type");
        }
    }

    private CompanyResponse parseV1Response(String id, String body) {
        // Logic for parsing V1 backend response
        return new CompanyResponse(id, "Company V1", true, null);
    }

    private CompanyResponse parseV2Response(String id, String body) {
        // Logic for parsing V2 backend response
        return new CompanyResponse(id, "Company V2", true, null);
    }

}
