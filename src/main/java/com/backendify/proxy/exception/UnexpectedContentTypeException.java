package com.backendify.proxy.exception;
//Created custom exception for case where the received content type is unexpected
public class UnexpectedContentTypeException extends Exception {
    public UnexpectedContentTypeException(String msg) {
        super(msg);
    }
}
