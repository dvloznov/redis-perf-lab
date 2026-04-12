package com.perflab.resp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

public class SimpleStringTest {

    @Test
    void parsesOk() {
        var result = RespParser.parse("+OK\r\n");
        assertInstanceOf(RespSimpleString.class, result.value());
        assertEquals("OK", ((RespSimpleString) result.value()).value());
    }

    @Test
    void parsesEmptyString() {
        var result = RespParser.parse("+\r\n");
        assertInstanceOf(RespSimpleString.class, result.value());
        assertEquals("", ((RespSimpleString) result.value()).value());
    }

    @Test
    void parsesStringWithSpaces() {
        var result = RespParser.parse("+hello world\r\n");
        assertInstanceOf(RespSimpleString.class, result.value());
        assertEquals("hello world", ((RespSimpleString) result.value()).value());
    }

    @Test
    void parsesStringWithPlusPrefix() {
        var result = RespParser.parse("++a\r\n");
        assertInstanceOf(RespSimpleString.class, result.value());
        assertEquals("+a", ((RespSimpleString) result.value()).value());
    }

    @Test
    void parsesSingleCharacter() {
        var result = RespParser.parse("+a\r\n");
        assertInstanceOf(RespSimpleString.class, result.value());
        assertEquals("a", ((RespSimpleString) result.value()).value());
    }

    @Test
    void parsesPong() {
        var result = RespParser.parse("+PONG\r\n");
        assertInstanceOf(RespSimpleString.class, result.value());
        assertEquals("PONG", ((RespSimpleString) result.value()).value());
    }

    @Test
    void parsesNullWord() {
        var result = RespParser.parse("+null\r\n");
        assertInstanceOf(RespSimpleString.class, result.value());
        assertEquals("null", ((RespSimpleString) result.value()).value());
    }

    @Test
    void parsesStringWithSpecialCharacters() {
        var result = RespParser.parse("+hello!@#$%^&*()\r\n");
        assertInstanceOf(RespSimpleString.class, result.value());
        assertEquals("hello!@#$%^&*()", ((RespSimpleString) result.value()).value());
    }

    @Test
    void parsesStringWithNumbers() {
        var result = RespParser.parse("+12345\r\n");
        assertInstanceOf(RespSimpleString.class, result.value());
        assertEquals("12345", ((RespSimpleString) result.value()).value());
    }

    @Test
    void consumedCharsIsCorrect() {
        var result = RespParser.parse("+OK\r\n");
        assertEquals(5, result.consumedChars());
    }

    @Test
    void consumedCharsForEmptyString() {
        var result = RespParser.parse("+\r\n");
        assertEquals(3, result.consumedChars());
    }

    @Test
    void consumedCharsForLongerString() {
        var result = RespParser.parse("+hello world\r\n");
        assertEquals(14, result.consumedChars());
    }

    @Test
    void ignoresTrailingData() {
        var result = RespParser.parse("+OK\r\nextra data");
        assertInstanceOf(RespSimpleString.class, result.value());
        assertEquals("OK", ((RespSimpleString) result.value()).value());
        assertEquals(5, result.consumedChars());
    }

    // --- encode tests ---

    @Test
    void encodesOk() {
        assertEquals("+OK\r\n", new RespSimpleString("OK").encode());
    }

    @Test
    void encodesEmptyString() {
        assertEquals("+\r\n", new RespSimpleString("").encode());
    }

    @Test
    void encodesPong() {
        assertEquals("+PONG\r\n", new RespSimpleString("PONG").encode());
    }

    @Test
    void encodesStringWithSpaces() {
        assertEquals("+hello world\r\n", new RespSimpleString("hello world").encode());
    }

    @Test
    void encodesStringWithSpecialCharacters() {
        assertEquals("+hello!@#$%^&*()\r\n", new RespSimpleString("hello!@#$%^&*()").encode());
    }

    @Test
    void encodesStringWithPlusPrefix() {
        assertEquals("++a\r\n", new RespSimpleString("+a").encode());
    }

    @Test
    void encodesSingleCharacter() {
        assertEquals("+a\r\n", new RespSimpleString("a").encode());
    }

    @Test
    void encodesNullWord() {
        assertEquals("+null\r\n", new RespSimpleString("null").encode());
    }

    @Test
    void encodesNumbers() {
        assertEquals("+12345\r\n", new RespSimpleString("12345").encode());
    }

    @Test
    void roundTripOk() {
        var original = new RespSimpleString("OK");
        var parsed = RespParser.parse(original.encode());
        assertInstanceOf(RespSimpleString.class, parsed.value());
        assertEquals("OK", ((RespSimpleString) parsed.value()).value());
    }

    @Test
    void roundTripEmpty() {
        var original = new RespSimpleString("");
        var parsed = RespParser.parse(original.encode());
        assertInstanceOf(RespSimpleString.class, parsed.value());
        assertEquals("", ((RespSimpleString) parsed.value()).value());
    }

    @Test
    void roundTripWithSpaces() {
        var original = new RespSimpleString("hello world");
        var parsed = RespParser.parse(original.encode());
        assertInstanceOf(RespSimpleString.class, parsed.value());
        assertEquals("hello world", ((RespSimpleString) parsed.value()).value());
    }

    @Test
    void encodeMatchesParseInput() {
        String wire = "+OK\r\n";
        var parsed = RespParser.parse(wire);
        assertEquals(wire, parsed.value().encode());
    }
}
