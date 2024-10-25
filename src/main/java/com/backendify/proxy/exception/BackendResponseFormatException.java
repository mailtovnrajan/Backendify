package com.backendify.proxy.exception;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.format.DateTimeParseException;

public class BackendResponseFormatException extends Throwable {

    public BackendResponseFormatException(String msg){ super(msg); }

    public BackendResponseFormatException(Exception e) {
        super(e);
    }
}
