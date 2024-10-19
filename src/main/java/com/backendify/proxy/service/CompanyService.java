package com.backendify.proxy.service;

import com.backendify.proxy.model.CompanyResponse;
import org.springframework.stereotype.Service;


@Service
public class CompanyService {

    public CompanyResponse getCompany(String id, String countryIso) {
        return new CompanyResponse(null, null, true, null);
    }
}
