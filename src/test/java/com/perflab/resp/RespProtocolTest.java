package com.perflab.resp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Wire-format tests for the RESP protocol. Only covers RESP2 types and inline
 * commands, matching what the server actually accepts on the wire.
 * See https://redis.io/docs/latest/develop/reference/protocol-spec/.
 */
class RespProtocolTest {

    // ---------- encode ----------

    @Test
    void encodesSimpleString() {
        assertEquals("+OK\r\n", new RespSimpleString("OK").encode());
    }

    @Test
    void encodesError() {
        assertEquals("-ERR wrong type\r\n", new RespError("ERR wrong type").encode());
    }

    @Test
    void encodesPositiveInteger() {
        assertEquals(":42\r\n", new RespInteger(42).encode());
    }

    @Test
    void encodesNegativeInteger() {
        assertEquals(":-7\r\n", new RespInteger(-7).encode());
    }

    @Test
    void encodesBulkString() {
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
    void encodesArray() {
        RespArray a = new RespArray(List.of(
                new RespBulkString("foo"),
                new RespBulkString("bar")));
        assertEquals("*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n", a.encode());
    }

    @Test
    void encodesEmptyArray() {
        assertEquals("*0\r\n", new RespArray(List.of()).encode());
    }

    @Test
    void encodesNullArray() {
        assertEquals("*-1\r\n", new RespArray(null).encode());
    }

    // ---------- parse ----------

    @Test
    void parsesSimpleString() {
        assertEquals(new RespSimpleString("OK"), parse("+OK\r\n"));
    }

    @Test
    void parsesError() {
        assertEquals(new RespError("ERR wrong type"), parse("-ERR wrong type\r\n"));
    }

    @Test
    void parsesPositiveInteger() {
        assertEquals(new RespInteger(1234), parse(":1234\r\n"));
    }

    @Test
    void parsesNegativeInteger() {
        assertEquals(new RespInteger(-7), parse(":-7\r\n"));
    }

    @Test
    void parsesBulkString() {
        assertEquals(new RespBulkString("hello"), parse("$5\r\nhello\r\n"));
    }

    @Test
    void parsesEmptyBulkString() {
        assertEquals(new RespBulkString(""), parse("$0\r\n\r\n"));
    }

    @Test
    void parsesNullBulkString() {
        assertEquals(new RespBulkString(null), parse("$-1\r\n"));
    }

    @Test
    void parsesArrayOfBulkStrings() {
        RespValue v = parse("*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n");
        assertEquals(new RespArray(List.of(
                new RespBulkString("foo"),
                new RespBulkString("bar"))), v);
    }

    @Test
    void parsesEmptyArray() {
        assertEquals(new RespArray(List.of()), parse("*0\r\n"));
    }

    @Test
    void parsesNullArray() {
        assertEquals(new RespArray(null), parse("*-1\r\n"));
    }

    @Test
    void parseReturnsNullOnEof() {
        assertNull(RespParser.parse(new ByteArrayInputStream(new byte[0])));
    }

    @Test
    void parsesTwoFramesFromSameStream() {
        InputStream in = stream("+OK\r\n:7\r\n");
        assertEquals(new RespSimpleString("OK"), RespParser.parse(in));
        assertEquals(new RespInteger(7), RespParser.parse(in));
        assertNull(RespParser.parse(in));
    }

    // ---------- inline ----------
    // Redis accepts a plain-text CRLF-terminated line split on whitespace as an
    // alias for an equivalent RESP array of bulk strings. The server is expected
    // to produce the same logical command object either way.

    @Test
    void parsesInlineSingleToken() {
        assertEquals(
                new RespArray(List.of(new RespBulkString("PING"))),
                parse("PING\r\n"));
    }

    @Test
    void parsesInlineMultipleTokens() {
        assertEquals(
                new RespArray(List.of(
                        new RespBulkString("SET"),
                        new RespBulkString("foo"),
                        new RespBulkString("bar"))),
                parse("SET foo bar\r\n"));
    }

    @Test
    void parsesInlineCollapsesRunsOfWhitespace() {
        assertEquals(
                new RespArray(List.of(
                        new RespBulkString("SET"),
                        new RespBulkString("foo"),
                        new RespBulkString("bar"))),
                parse("SET   foo\tbar\r\n"));
    }

    // ---------- helpers ----------

    private static RespValue parse(String wire) {
        return RespParser.parse(stream(wire));
    }

    private static InputStream stream(String wire) {
        return new ByteArrayInputStream(wire.getBytes(StandardCharsets.ISO_8859_1));
    }
}
