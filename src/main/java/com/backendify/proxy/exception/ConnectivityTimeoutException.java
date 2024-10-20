package com.backendify.proxy.exception;

import org.springframework.web.client.ResourceAccessException;

public class ConnectivityTimeoutException extends Throwable {
    public ConnectivityTimeoutException(String msg, ResourceAccessException cause) {
        super(msg, cause);
    }
}
