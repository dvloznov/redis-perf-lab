package com.perflab.resp;

public record RespSimpleString(String value) implements RespValue {

    @Override
    public String encode() {
        if (value != null && value.contains("\r\n")) {
            throw new IllegalArgumentException("Simple strings must not contain CRLF");
        }
        return "+" + value + "\r\n";
    }
}
