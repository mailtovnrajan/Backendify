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
    public ResponseEntity<?> getCompany(@RequestParam String id, @RequestParam String country_iso) {
        try {
            CompanyResponse companyResponse = companyService.getCompany(id, country_iso);
            return ResponseEntity.ok(companyResponse);
        } catch (CompanyNotFoundException | CountryNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());  // 404 Not Found
        } catch (BackendServerException e) {
            return ResponseEntity.status(500).body(e.getMessage()); // 500 Internal Server Error
        } catch (ConnectivityTimeoutException e) {
            return ResponseEntity.status(504).body(e.getMessage());  // 504 Gateway Timeout
        } catch (UnexpectedContentTypeException e) {
            return ResponseEntity.status(415).body(e.getMessage());  // 415 Unsupported Media Type
        } catch (BackendResponseFormatException e) {
            return ResponseEntity.status(502).body(e.getMessage());  // 502 Bad Gateway
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());  // 500 Internal Server Error for unexpected errors
        }
    }
}
