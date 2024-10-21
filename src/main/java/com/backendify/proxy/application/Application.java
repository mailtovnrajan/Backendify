package com.backendify.proxy.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication  // This annotation sets up Spring Boot's auto-configuration
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);  // Run the Spring Boot application
    }
}
