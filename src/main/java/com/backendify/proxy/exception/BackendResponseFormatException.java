package com.backendify.proxy.exception;

public class BackendResponseFormatException extends Throwable {

    public BackendResponseFormatException(String msg){ super(msg); }

    public BackendResponseFormatException(Exception e) {
        super(e);
    }
}
