package com.perflab.resp;

public record RespError(String value) implements RespValue {

    @Override
    public String encode() {
        return "-" + value + "\r\n";
    }
}
