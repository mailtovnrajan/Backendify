package com.backendify.proxy.controller;

import com.backendify.proxy.exception.*;
import com.backendify.proxy.model.CompanyResponse;
import com.backendify.proxy.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CompanyController {

    private final CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping ("/company")
    public ResponseEntity<CompanyResponse> getCompany(@RequestParam String id, @RequestParam String country_iso) {
        try {
            CompanyResponse companyResponse = companyService.getCompany(id, country_iso);
            return ResponseEntity.ok(companyResponse);
        } catch (CompanyNotFoundException | CountryNotFoundException e) {
            return ResponseEntity.status(404).build();  // 404 Not Found
        } catch (BackendServerException e) {
            return ResponseEntity.status(500).build();  // 500 Internal Server Error
        } catch (ConnectivityTimeoutException e) {
            return ResponseEntity.status(504).build();  // 504 Gateway Timeout
        } catch (UnexpectedContentTypeException e) {
            return ResponseEntity.status(415).build();  // 415 Unsupported Media Type
        } catch (BackendResponseFormatException e) {
            return ResponseEntity.status(502).build();  // 502 Bad Gateway
        } catch (Exception e) {
            return ResponseEntity.status(400).build();  // 400 Bad Request for unexpected errors
        }
    }
}
