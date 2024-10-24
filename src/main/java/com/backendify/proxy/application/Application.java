package com.backendify.proxy.application;

import com.backendify.proxy.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.Map;

@SpringBootApplication  // This annotation sets up Spring Boot's auto-configuration
@ComponentScan(basePackages = {"com.backendify.proxy.service", "com.backendify.proxy.application"})
public class Application implements CommandLineRunner {

    private final CompanyService companyService;

    @Autowired
    public Application(CompanyService companyService) {
        this.companyService = companyService;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);  // Run the Spring Boot application
    }

    @Override
    public void run(String... args) throws Exception {
        // Parse command-line arguments
        Map<String, String> backendMappings = CommandLineArgsParser.parseArgs(args);
        companyService.setBackendMappings(backendMappings);
    }

}
