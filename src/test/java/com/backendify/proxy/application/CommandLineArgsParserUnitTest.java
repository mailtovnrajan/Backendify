package com.backendify.proxy.application;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CommandLineArgsParserUnitTest {

    @Test
    public void whenValidArgsProvided_thenParseCorrectly() {
        String[] args = {"ru=http://localhost:9001", "us=http://localhost:9002"};
        Map<String, String> result = CommandLineArgsParser.parseArgs(args);

        // Verify the mappings
        assertEquals(2, result.size());
        assertEquals("http://localhost:9001", result.get("ru"));
        assertEquals("http://localhost:9002", result.get("us"));
    }

    @Test
    public void whenInvalidArgsProvided_thenThrowsIllegalArgumentException() {
        String[] args = {"invalidArg"};
        
        // Expect an exception due to the invalid format
        assertThrows(IllegalArgumentException.class, () -> CommandLineArgsParser.parseArgs(args));
    }

    @Test
    public void whenNoArgsProvided_thenEmptyMap() {
        String[] args = {};
        Map<String, String> result = CommandLineArgsParser.parseArgs(args);

        // Verify that an empty map is returned
        assertTrue(result.isEmpty());
    }

    @Test
    public void whenMalformedUrlProvided_thenThrowsIllegalArgumentException() {
        String[] args = {"ru=localhost:9001"}; // Malformed URL (missing protocol like http://)

        // Expect an exception due to malformed URL
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            CommandLineArgsParser.parseArgs(args);
        });

        assertEquals("Invalid URL format for country code: ru", exception.getMessage());
    }
}
