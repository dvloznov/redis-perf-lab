package com.perflab.resp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

public class ErrorTest {

    @Test
    void parsesSimpleError() {
        var result = RespParser.parse("-ERR\r\n");
        assertInstanceOf(RespError.class, result.value());
        assertEquals("ERR", ((RespError) result.value()).value());
    }

    @Test
    void parsesErrorWithMessage() {
        var result = RespParser.parse("-ERR unknown command\r\n");
        assertInstanceOf(RespError.class, result.value());
        assertEquals("ERR unknown command", ((RespError) result.value()).value());
    }

    @Test
    void parsesErrorWithDetailedMessage() {
        var result = RespParser.parse("-ERR unknown command 'foobar'\r\n");
        assertInstanceOf(RespError.class, result.value());
        assertEquals("ERR unknown command 'foobar'", ((RespError) result.value()).value());
    }

    @Test
    void parsesWrongtypeError() {
        var result = RespParser.parse("-WRONGTYPE Operation against a key holding the wrong kind of value\r\n");
        assertInstanceOf(RespError.class, result.value());
        assertEquals("WRONGTYPE Operation against a key holding the wrong kind of value",
                ((RespError) result.value()).value());
    }

    @Test
    void parsesEmptyError() {
        var result = RespParser.parse("-\r\n");
        assertInstanceOf(RespError.class, result.value());
        assertEquals("", ((RespError) result.value()).value());
    }

    @Test
    void parsesSingleWordError() {
        var result = RespParser.parse("-LOADING\r\n");
        assertInstanceOf(RespError.class, result.value());
        assertEquals("LOADING", ((RespError) result.value()).value());
    }

    @Test
    void parsesMovedError() {
        var result = RespParser.parse("-MOVED 3999 127.0.0.1:6381\r\n");
        assertInstanceOf(RespError.class, result.value());
        assertEquals("MOVED 3999 127.0.0.1:6381", ((RespError) result.value()).value());
    }

    @Test
    void consumedCharsIsCorrect() {
        var result = RespParser.parse("-ERR\r\n");
        assertEquals(6, result.consumedChars());
    }

    @Test
    void consumedCharsForEmptyError() {
        var result = RespParser.parse("-\r\n");
        assertEquals(3, result.consumedChars());
    }

    @Test
    void consumedCharsForLongError() {
        var result = RespParser.parse("-ERR unknown command\r\n");
        assertEquals(22, result.consumedChars());
    }

    @Test
    void ignoresTrailingData() {
        var result = RespParser.parse("-ERR\r\nmore stuff");
        assertInstanceOf(RespError.class, result.value());
        assertEquals("ERR", ((RespError) result.value()).value());
        assertEquals(6, result.consumedChars());
    }

    // --- encode tests ---

    @Test
    void encodesSimpleError() {
        assertEquals("-ERR\r\n", new RespError("ERR").encode());
    }

    @Test
    void encodesEmptyError() {
        assertEquals("-\r\n", new RespError("").encode());
    }

    @Test
    void encodesErrorWithMessage() {
        assertEquals("-ERR unknown command\r\n", new RespError("ERR unknown command").encode());
    }

    @Test
    void encodesErrorWithDetailedMessage() {
        assertEquals("-ERR unknown command 'foobar'\r\n",
                new RespError("ERR unknown command 'foobar'").encode());
    }

    @Test
    void encodesWrongtypeError() {
        assertEquals("-WRONGTYPE Operation against a key holding the wrong kind of value\r\n",
                new RespError("WRONGTYPE Operation against a key holding the wrong kind of value").encode());
    }

    @Test
    void encodesMovedError() {
        assertEquals("-MOVED 3999 127.0.0.1:6381\r\n",
                new RespError("MOVED 3999 127.0.0.1:6381").encode());
    }

    @Test
    void encodesLoadingError() {
        assertEquals("-LOADING\r\n", new RespError("LOADING").encode());
    }

    @Test
    void roundTripSimpleError() {
        var original = new RespError("ERR");
        var parsed = RespParser.parse(original.encode());
        assertInstanceOf(RespError.class, parsed.value());
        assertEquals("ERR", ((RespError) parsed.value()).value());
    }

    @Test
    void roundTripErrorWithMessage() {
        var original = new RespError("ERR unknown command 'foobar'");
        var parsed = RespParser.parse(original.encode());
        assertInstanceOf(RespError.class, parsed.value());
        assertEquals("ERR unknown command 'foobar'", ((RespError) parsed.value()).value());
    }

    @Test
    void roundTripEmptyError() {
        var original = new RespError("");
        var parsed = RespParser.parse(original.encode());
        assertInstanceOf(RespError.class, parsed.value());
        assertEquals("", ((RespError) parsed.value()).value());
    }

    @Test
    void encodeMatchesParseInput() {
        String wire = "-ERR unknown command\r\n";
        var parsed = RespParser.parse(wire);
        assertEquals(wire, parsed.value().encode());
    }
}
