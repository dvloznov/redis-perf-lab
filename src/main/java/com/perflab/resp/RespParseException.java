package com.perflab.resp;

public class RespParseException extends RuntimeException {
    public RespParseException(String message) {
        super(message);
    }

    public RespParseException(String message, Throwable ex) {
        super(message, ex);
    }
}
