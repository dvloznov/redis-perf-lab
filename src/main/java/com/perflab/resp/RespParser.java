package com.perflab.resp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class RespParser {

    private static final int PREFIX_LEN = 1;
    private static final int CRLF_LEN = 2;

    private static final Map<Character, Function<String, ParseResult>> parsers = Map.of(
            '+', RespParser::parseSimpleString,
            '-', RespParser::parseError,
            ':', RespParser::parseInteger,
            '$', RespParser::parseBulkString,
            '*', RespParser::parseArray);

    public static ParseResult parse(String command) {
        if (command == null || command.isEmpty() || !parsers.containsKey(command.charAt(0))) {
            throw new RespParseException("Invalid command");
        }
        return parsers.get(command.charAt(0)).apply(command.substring(PREFIX_LEN));

    }

    private static ParseResult parseSimpleString(String data) {
        var simpleString = extractLine(data);
        return new ParseResult(new RespSimpleString(simpleString), simpleString.length() + PREFIX_LEN + CRLF_LEN);
    }

    private static ParseResult parseError(String data) {
        var error = extractLine(data);
        return new ParseResult(new RespError(error), error.length() + PREFIX_LEN + CRLF_LEN);
    }

    private static ParseResult parseInteger(String data) {
        var intAsString = extractLine(data);
        long valueAsLong;
        try {
            valueAsLong = Long.parseLong(intAsString);
        } catch (NumberFormatException e) {
            throw new RespParseException("Invalid integer value: " + intAsString, e);
        }

        return new ParseResult(new RespInteger(valueAsLong), intAsString.length() + PREFIX_LEN + CRLF_LEN);
    }

    private static ParseResult parseBulkString(String data) {
        var lengthAsString = extractLine(data);
        int length;
        try {
            length = Integer.parseInt(lengthAsString);
        } catch (NumberFormatException e) {
            throw new RespParseException("Invalid integer value: " + lengthAsString, e);
        }
        if (length < -1) {
            throw new RespParseException("Invalid bulk string length: " + length);
        }

        if (length == -1) {
            return new ParseResult(new RespBulkString(null), PREFIX_LEN + lengthAsString.length() + CRLF_LEN);
        }

        String rest = data.substring(lengthAsString.length() + CRLF_LEN);
        if (rest.length() < length + CRLF_LEN) {
            throw new RespParseException(
                    "Truncated bulk string: declared length " + length + " but only " + rest.length()
                            + " bytes available");
        }

        String value = rest.substring(0, length);

        return new ParseResult(new RespBulkString(value),
                PREFIX_LEN + lengthAsString.length() + CRLF_LEN + length + CRLF_LEN);
    }

    private static ParseResult parseArray(String data) {
        var lengthAsString = extractLine(data);
        String rest = data.substring(lengthAsString.length() + CRLF_LEN);
        int arrayLength;
        try {
            arrayLength = Integer.parseInt(lengthAsString);
        } catch (NumberFormatException e) {
            throw new RespParseException("Invalid integer value: " + lengthAsString, e);
        }
        if (arrayLength < -1) {
            throw new RespParseException("Invalid array length: " + arrayLength);
        }
        if (arrayLength == -1) {
            return new ParseResult(new RespArray(null), PREFIX_LEN + lengthAsString.length() + CRLF_LEN);
        }
        var result = new ArrayList<RespValue>();
        var nextString = rest;
        var consumedTotal = 0;
        for (int i = 0; i < arrayLength; i++) {
            var parsingResult = RespParser.parse(nextString);
            result.add(parsingResult.value());
            nextString = nextString.substring(parsingResult.consumedChars());
            consumedTotal += parsingResult.consumedChars();
        }

        return new ParseResult(new RespArray(List.copyOf(result)),
                consumedTotal + PREFIX_LEN + CRLF_LEN + lengthAsString.length());
    }

    private static String extractLine(String data) {
        int idx = data.indexOf("\r\n");
        if (idx == -1) {
            throw new RespParseException("Missing CRLF terminator");
        }
        return data.substring(0, idx);
    }

    private RespParser() {
    }
}
