package com.perflab.resp;

public record ParseResult(RespValue value, int consumedChars) {
}
