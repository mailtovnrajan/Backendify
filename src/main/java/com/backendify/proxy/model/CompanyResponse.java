package com.backendify.proxy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyResponse {

    private String id;
    private String name;
    private boolean active;
    private String activeUntil;  // Optional field, can be null
    // Constructor
    public CompanyResponse(String id, String name, boolean active, String activeUntil) {
        this.id = id;
        this.name = name;
        this.active = active;
        this.activeUntil = activeUntil;
    }

    // Getters and setters for JSON serialization/deserialization
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getActiveUntil() {
        return activeUntil;
    }

    public void setActiveUntil(String activeUntil) {
        this.activeUntil = activeUntil;
    }
}