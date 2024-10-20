package com.backendify.proxy.service;

import com.backendify.proxy.model.CompanyResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
        return parseV1Response(body);
    }

    private CompanyResponse parseV1Response(String body) {
        // Logic for parsing V1 backend response
        return new CompanyResponse("123", "Company V1", true, null);
    }

}
