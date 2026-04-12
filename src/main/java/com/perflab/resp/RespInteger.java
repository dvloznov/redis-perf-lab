package com.perflab.resp;

public record RespInteger(long value) implements RespValue {

    @Override
    public String encode() {
        return ":" + value + "\r\n";
    }
}
