package com.backendify.proxy.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CompanyV2Response {

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("tin")
    private String tin;

    @JsonProperty("dissolved_on")
    private String dissolvedOn;  // Optional (nullable)

    // Getters and setters
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getTin() {
        return tin;
    }

    public void setTin(String tin) {
        this.tin = tin;
    }

    public String getDissolvedOn() {
        return dissolvedOn;
    }

    public void setDissolvedOn(String dissolvedOn) {
        this.dissolvedOn = dissolvedOn;
    }
}