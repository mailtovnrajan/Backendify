package com.backendify.proxy.application;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CommandLineArgsParser {

    public static Map<String, String> parseArgs(String[] args) {
        Map<String, String> backendMappings = new HashMap<>();

        for (String arg : args) {
            if (!arg.contains("=")) {
                throw new IllegalArgumentException("Invalid argument format. Expected format: countryCode=backendUrl");
            }

            String[] parts = arg.split("=");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid argument format. Expected format: countryCode=backendUrl");
            }

            String countryCode = parts[0].trim();
            String backendUrl = parts[1].trim();

            // Validate the URL format
            if (!isValidUrl(backendUrl)) {
                throw new IllegalArgumentException("Invalid URL format for country code: " + countryCode);
            }

            backendMappings.put(countryCode, backendUrl);
        }

        return backendMappings;
    }

    // Helper method to validate URL
    private static boolean isValidUrl(String urlString) {
        try {
            new URL(urlString); // This checks if the URL is valid
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}