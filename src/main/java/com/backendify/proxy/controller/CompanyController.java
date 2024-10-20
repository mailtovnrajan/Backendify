package com.backendify.proxy.controller;

import com.backendify.proxy.exception.BackendResponseFormatException;
import com.backendify.proxy.exception.CompanyNotFoundException;
import com.backendify.proxy.exception.UnexpectedContentTypeException;
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
    public ResponseEntity<CompanyResponse> getCompany(@RequestParam String id, @RequestParam String country_iso) throws UnexpectedContentTypeException, BackendResponseFormatException, CompanyNotFoundException {
        CompanyResponse companyResponse = companyService.getCompany(id, country_iso);
        return ResponseEntity.ok(companyResponse);
    }
}
