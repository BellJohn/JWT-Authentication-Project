package com.bellj.resourceserver.exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message){
        super(message);
    }

    public UnauthorizedException(String message, Exception cause){
        super(message, cause);
    }
}
