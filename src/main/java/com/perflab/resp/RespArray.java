package com.perflab.resp;

import java.util.List;
import java.util.stream.Collectors;

public record RespArray(List<RespValue> values) implements RespValue {

    public RespArray {
        values = values == null ? null : List.copyOf(values);
    }

    @Override
    public String encode() {
        if (values == null) {
            return "*-1\r\n";
        }
        return "*" + values.size() + "\r\n"
                + values.stream().map(RespValue::encode).collect(Collectors.joining());
    }
}