package com.backendify.proxy.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CompanyV1Response {

    @JsonProperty("cn")
    private String companyName;

    @JsonProperty("created_on")
    private String createdOn;

    @JsonProperty("closed_on")
    private String closedOn;  // This field is optional (nullable)

    // Getters and Setters
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getClosedOn() {
        return closedOn;
    }

    public void setClosedOn(String closedOn) {
        this.closedOn = closedOn;
    }
}
