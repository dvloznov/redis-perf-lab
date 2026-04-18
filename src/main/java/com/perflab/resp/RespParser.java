package com.perflab.resp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public final class RespParser {

    private static final Map<Character, Function<InputStream, RespValue>> parsers = Map.of(
            '+', RespParser::parseSimpleString,
            '-', RespParser::parseError,
            ':', RespParser::parseInteger,
            '#', RespParser::parseBoolean,
            '$', RespParser::parseBulkString,
            '*', RespParser::parseArray);

    public static RespValue parse(InputStream is) {
        int b = getNextInt(is);
        if (b == -1) {
            return null;
        }

        var parser = parsers.get((char) b);
        if (parser == null) {
            return parseInlineCommand(b, is);
        }
        return parser.apply(is);

    }

    private static RespValue parseInlineCommand(int firstByte, InputStream is) {
        String line = (char) firstByte + extractLine(is);
        return new RespArray(
                Stream.of(line.split("\\s+")).filter(s -> !s.isBlank()).map(s -> (RespValue) new RespBulkString(s))
                        .toList());
    }

    private static RespValue parseSimpleString(InputStream is) {
        return new RespSimpleString(extractLine(is));
    }

    private static RespValue parseError(InputStream is) {
        return new RespError(extractLine(is));
    }

    private static RespValue parseInteger(InputStream is) {
        var intAsString = extractLine(is);
        long valueAsLong;
        try {
            valueAsLong = Long.parseLong(intAsString);
        } catch (NumberFormatException e) {
            throw new RespParseException("Invalid integer value: " + intAsString, e);
        }

        return new RespInteger(valueAsLong);
    }

    private static RespValue parseBoolean(InputStream is) {
        return new RespBoolean(extractLine(is));
    }

    private static RespValue parseBulkString(InputStream is) {
        var lengthAsString = extractLine(is);
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
            return new RespBulkString(null);
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int nextByte = getNextInt(is);
            if (nextByte == -1) {
                throw new RespParseException("Unexpected end of input stream");
            }
            sb.append((char) nextByte);
        }

        if ((char) getNextInt(is) != '\r') {
            throw new RespParseException("Unexpected end of input stream");
        }
        if ((char) getNextInt(is) != '\n') {
            throw new RespParseException("Unexpected end of input stream");
        }

        return new RespBulkString(sb.toString());
    }

    private static RespValue parseArray(InputStream is) {
        var lengthAsString = extractLine(is);
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
            return new RespArray(null);
        }
        var result = new ArrayList<RespValue>();
        for (int i = 0; i < arrayLength; i++) {
            var parsingResult = RespParser.parse(is);
            result.add(parsingResult);
        }

        return new RespArray(List.copyOf(result));
    }

    private static String extractLine(InputStream is) {
        StringBuilder sb = new StringBuilder();
        int b = -1;
        while ((b = getNextInt(is)) != -1) {
            char c = (char) b;
            if (c == '\r') {
                int nextB = getNextInt(is);
                if (nextB == -1) {
                    throw new RespParseException("Unexpected EOF after CR while parsing line");
                }
                if ((char) nextB != '\n') {
                    sb.append(c);
                    sb.append((char) nextB);
                    continue;
                }
                return sb.toString();
            }
            sb.append(c);
        }
        throw new RespParseException("Unexpected EOF while parsing the request");
    }

    private static int getNextInt(InputStream is) {
        try {
            return is.read();
        } catch (IOException ex) {
            throw new RespParseException("Exception while reading input stream: ", ex);
        }
    }

    private RespParser() {
    }
}
