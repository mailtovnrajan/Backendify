package com.backendify.proxy;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController  // Marks this class as a REST controller
public class StatusController {

    @GetMapping("/status")  // Maps HTTP GET requests to /status
    public ResponseEntity<String> getStatus() {
        // Returns a 200 OK response with the message "Service is running"
        return ResponseEntity.ok("Service is running");
    }
}