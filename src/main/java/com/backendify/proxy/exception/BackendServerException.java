package com.backendify.proxy.exception;

import org.springframework.web.client.HttpServerErrorException;

public class BackendServerException extends Throwable {
    public BackendServerException(String msg, HttpServerErrorException cause) {
        super(msg, cause);
    }
}
