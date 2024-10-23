package com.backendify.proxy.exception;

import com.fasterxml.jackson.core.JsonProcessingException;

public class BackendResponseFormatException extends Throwable {

    public BackendResponseFormatException(String msg){ super(msg); }

    public BackendResponseFormatException(JsonProcessingException cause) {
        super(cause);
    }
}
