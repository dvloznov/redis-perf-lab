package com.perflab.resp;

public record RespBoolean(String value) implements RespValue {

    @Override
    public String encode() {
        return "#" + value + "\r\n";
    }
}
