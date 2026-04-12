package com.perflab.resp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class BulkStringTest {

    @Test
    void parsesSimpleBulkString() {
        var result = RespParser.parse("$5\r\nhello\r\n");
        assertInstanceOf(RespBulkString.class, result.value());
        assertEquals("hello", ((RespBulkString) result.value()).value());
    }

    @Test
    void parsesEmptyBulkString() {
        var result = RespParser.parse("$0\r\n\r\n");
        assertInstanceOf(RespBulkString.class, result.value());
        assertEquals("", ((RespBulkString) result.value()).value());
    }

    @Test
    void parsesBulkStringContainingCrlf() {
        var result = RespParser.parse("$5\r\nAAA\r\n\r\n");
        assertInstanceOf(RespBulkString.class, result.value());
        assertEquals("AAA\r\n", ((RespBulkString) result.value()).value());
    }

    @Test
    void parsesBulkStringWithSpaces() {
        var result = RespParser.parse("$11\r\nhello world\r\n");
        assertInstanceOf(RespBulkString.class, result.value());
        assertEquals("hello world", ((RespBulkString) result.value()).value());
    }

    @Test
    void parsesSingleCharBulkString() {
        var result = RespParser.parse("$1\r\na\r\n");
        assertInstanceOf(RespBulkString.class, result.value());
        assertEquals("a", ((RespBulkString) result.value()).value());
    }

    @Test
    void parsesNullBulkString() {
        var result = RespParser.parse("$-1\r\n");
        assertInstanceOf(RespBulkString.class, result.value());
        assertNull(((RespBulkString) result.value()).value());
    }

    @Test
    void parsesLongBulkString() {
        String payload = "a".repeat(100);
        var result = RespParser.parse("$100\r\n" + payload + "\r\n");
        assertInstanceOf(RespBulkString.class, result.value());
        assertEquals(payload, ((RespBulkString) result.value()).value());
    }

    @Test
    void parsesBulkStringWithSpecialCharacters() {
        String payload = "!@#$%^&*()";
        var result = RespParser.parse("$10\r\n" + payload + "\r\n");
        assertInstanceOf(RespBulkString.class, result.value());
        assertEquals(payload, ((RespBulkString) result.value()).value());
    }

    @Test
    void parsesBulkStringWithNewlines() {
        var result = RespParser.parse("$7\r\nfoo\r\nba\r\n");
        assertInstanceOf(RespBulkString.class, result.value());
        assertEquals("foo\r\nba", ((RespBulkString) result.value()).value());
    }

    @Test
    void parsesBulkStringContainingRespPrefix() {
        var result = RespParser.parse("$4\r\n+OK\n\r\n");
        assertInstanceOf(RespBulkString.class, result.value());
        assertEquals("+OK\n", ((RespBulkString) result.value()).value());
    }

    @Test
    void consumedCharsForSimpleString() {
        // "$5\r\nhello\r\n" = 1 + 1 + 2 + 5 + 2 = 11
        var result = RespParser.parse("$5\r\nhello\r\n");
        assertEquals(11, result.consumedChars());
    }

    @Test
    void consumedCharsForEmptyString() {
        // "$0\r\n\r\n" = 1 + 1 + 2 + 0 + 2 = 6
        var result = RespParser.parse("$0\r\n\r\n");
        assertEquals(6, result.consumedChars());
    }

    @Test
    void consumedCharsForStringWithCrlf() {
        // "$5\r\nAAA\r\n\r\n" = 1 + 1 + 2 + 5 + 2 = 11
        var result = RespParser.parse("$5\r\nAAA\r\n\r\n");
        assertEquals(11, result.consumedChars());
    }

    @Test
    void consumedCharsForNullBulkString() {
        // "$-1\r\n" = 1 + 2 + 2 = 5
        var result = RespParser.parse("$-1\r\n");
        assertEquals(5, result.consumedChars());
    }

    @Test
    void ignoresTrailingData() {
        var result = RespParser.parse("$5\r\nhello\r\ngarbage");
        assertInstanceOf(RespBulkString.class, result.value());
        assertEquals("hello", ((RespBulkString) result.value()).value());
        assertEquals(11, result.consumedChars());
    }

    @Test
    void parsesDoubleDigitLength() {
        String payload = "abcdefghij";
        var result = RespParser.parse("$10\r\n" + payload + "\r\n");
        assertInstanceOf(RespBulkString.class, result.value());
        assertEquals(payload, ((RespBulkString) result.value()).value());
    }

    // --- encode tests ---

    @Test
    void encodesSimpleBulkString() {
        assertEquals("$5\r\nhello\r\n", new RespBulkString("hello").encode());
    }

    @Test
    void encodesEmptyBulkString() {
        assertEquals("$0\r\n\r\n", new RespBulkString("").encode());
    }

    @Test
    void encodesNullBulkString() {
        assertEquals("$-1\r\n", new RespBulkString(null).encode());
    }

    @Test
    void encodesSingleChar() {
        assertEquals("$1\r\na\r\n", new RespBulkString("a").encode());
    }

    @Test
    void encodesStringWithSpaces() {
        assertEquals("$11\r\nhello world\r\n", new RespBulkString("hello world").encode());
    }

    @Test
    void encodesStringContainingCrlf() {
        assertEquals("$5\r\nAAA\r\n\r\n", new RespBulkString("AAA\r\n").encode());
    }

    @Test
    void encodesStringWithSpecialCharacters() {
        String payload = "!@#$%^&*()";
        assertEquals("$10\r\n" + payload + "\r\n", new RespBulkString(payload).encode());
    }

    @Test
    void encodesLongString() {
        String payload = "a".repeat(100);
        assertEquals("$100\r\n" + payload + "\r\n", new RespBulkString(payload).encode());
    }

    @Test
    void encodesStringContainingRespPrefix() {
        assertEquals("$4\r\n+OK\n\r\n", new RespBulkString("+OK\n").encode());
    }

    @Test
    void encodesDoubleDigitLength() {
        String payload = "abcdefghij";
        assertEquals("$10\r\n" + payload + "\r\n", new RespBulkString(payload).encode());
    }

    @Test
    void encodesStringWithMultipleCrlf() {
        String payload = "foo\r\nbar\r\nbaz";
        assertEquals("$" + payload.length() + "\r\n" + payload + "\r\n",
                new RespBulkString(payload).encode());
    }

    @Test
    void roundTripSimple() {
        var original = new RespBulkString("hello");
        var parsed = RespParser.parse(original.encode());
        assertInstanceOf(RespBulkString.class, parsed.value());
        assertEquals("hello", ((RespBulkString) parsed.value()).value());
    }

    @Test
    void roundTripEmpty() {
        var original = new RespBulkString("");
        var parsed = RespParser.parse(original.encode());
        assertInstanceOf(RespBulkString.class, parsed.value());
        assertEquals("", ((RespBulkString) parsed.value()).value());
    }

    @Test
    void roundTripNull() {
        var original = new RespBulkString(null);
        var parsed = RespParser.parse(original.encode());
        assertInstanceOf(RespBulkString.class, parsed.value());
        assertNull(((RespBulkString) parsed.value()).value());
    }

    @Test
    void roundTripWithCrlf() {
        var original = new RespBulkString("AAA\r\n");
        var parsed = RespParser.parse(original.encode());
        assertInstanceOf(RespBulkString.class, parsed.value());
        assertEquals("AAA\r\n", ((RespBulkString) parsed.value()).value());
    }

    @Test
    void roundTripWithSpaces() {
        var original = new RespBulkString("hello world");
        var parsed = RespParser.parse(original.encode());
        assertInstanceOf(RespBulkString.class, parsed.value());
        assertEquals("hello world", ((RespBulkString) parsed.value()).value());
    }

    @Test
    void roundTripLongString() {
        String payload = "x".repeat(500);
        var original = new RespBulkString(payload);
        var parsed = RespParser.parse(original.encode());
        assertInstanceOf(RespBulkString.class, parsed.value());
        assertEquals(payload, ((RespBulkString) parsed.value()).value());
    }

    @Test
    void encodeMatchesParseInput() {
        String wire = "$5\r\nhello\r\n";
        var parsed = RespParser.parse(wire);
        assertEquals(wire, parsed.value().encode());
    }

    @Test
    void encodeMatchesParseInputForNull() {
        String wire = "$-1\r\n";
        var parsed = RespParser.parse(wire);
        assertEquals(wire, parsed.value().encode());
    }
}
