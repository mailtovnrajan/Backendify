package com.backendify.proxy.controller;

import com.backendify.proxy.model.CompanyResponse;
import com.backendify.proxy.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping ("/company")
    public ResponseEntity<CompanyResponse> getCompany(@RequestParam String id, @RequestParam String country_iso) {
        CompanyResponse companyResponse = companyService.getCompany(id, country_iso);
        return ResponseEntity.ok(companyResponse);
    }
}
