package com.backendify.proxy.application;

import com.backendify.proxy.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;
import java.util.Map;

@SpringBootApplication  // This annotation sets up Spring Boot's auto-configuration
@ComponentScan(basePackages = {"com.backendify.proxy"})
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
        // Filter out Spring Boot-specific arguments (like --server.port)
        String[] customArgs = Arrays.stream(args)
                .filter(arg -> !arg.startsWith("--server.port"))
                .toArray(String[]::new);

        // Parse command-line arguments
        Map<String, String> backendMappings = CommandLineArgsParser.parseArgs(customArgs);
        companyService.setBackendMappings(backendMappings);
    }

}
