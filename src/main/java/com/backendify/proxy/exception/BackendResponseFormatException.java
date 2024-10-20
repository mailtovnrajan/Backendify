package com.backendify.proxy.exception;

import com.fasterxml.jackson.core.JsonProcessingException;

public class BackendResponseFormatException extends Throwable {
    public BackendResponseFormatException(JsonProcessingException cause) {
        super(cause);
    }
}
