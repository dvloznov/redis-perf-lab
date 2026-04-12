package com.perflab.resp;

public record RespBulkString(String value) implements RespValue {

    @Override
    public String encode() {
        if (value == null) {
            return "$-1\r\n";
        }
        return "$" + value.length() + "\r\n" + value + "\r\n";
    }
}
