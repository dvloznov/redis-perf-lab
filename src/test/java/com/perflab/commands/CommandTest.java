package com.perflab.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.perflab.resp.RespParser;
import com.perflab.resp.RespValue;
import com.perflab.storage.KVStorage;

/**
 * API-level tests: bytes from a client go in, bytes back to the client come out.
 * The test fixture goes through the same parse -> dispatch -> encode pipeline
 * that the server uses, exercised via the public API of each layer.
 *
 * Scope mirrors the commands actually implemented:
 *   PING, ECHO, GET, SET, CONFIG
 * and the two wire formats the server accepts for each: RESP arrays and inline.
 *
 * Spec reference: https://redis.io/commands/
 */
class CommandTest {

    @BeforeEach
    void resetStorage() {
        KVStorage.STORAGE.clear();
    }

    // ---------- PING ----------
    // PING with no argument returns the simple string "PONG".
    // https://redis.io/commands/ping/

    @Test
    void pingRespReturnsPong() {
        assertEquals("+PONG\r\n", exchange("*1\r\n$4\r\nPING\r\n"));
    }

    @Test
    void pingInlineReturnsPong() {
        assertEquals("+PONG\r\n", exchange("PING\r\n"));
    }

    @Test
    void pingIsCaseInsensitive() {
        assertEquals("+PONG\r\n", exchange("*1\r\n$4\r\nping\r\n"));
        assertEquals("+PONG\r\n", exchange("*1\r\n$4\r\nPiNg\r\n"));
    }

    // ---------- ECHO ----------
    // ECHO returns its argument as a bulk string.
    // https://redis.io/commands/echo/

    @Test
    void echoReturnsArgumentAsBulkString() {
        assertEquals(
                "$11\r\nhello world\r\n",
                exchange("*2\r\n$4\r\nECHO\r\n$11\r\nhello world\r\n"));
    }

    @Test
    void echoPreservesEmptyPayload() {
        assertEquals(
                "$0\r\n\r\n",
                exchange("*2\r\n$4\r\nECHO\r\n$0\r\n\r\n"));
    }

    // ---------- SET ----------
    // SET key value returns the simple string "OK".
    // https://redis.io/commands/set/

    @Test
    void setReturnsOk() {
        assertEquals("+OK\r\n", exchange("*3\r\n$3\r\nSET\r\n$3\r\nfoo\r\n$3\r\nbar\r\n"));
    }

    @Test
    void setStoresValueVisibleToGet() {
        exchange("*3\r\n$3\r\nSET\r\n$3\r\nfoo\r\n$3\r\nbar\r\n");
        assertEquals("$3\r\nbar\r\n", exchange("*2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n"));
    }

    @Test
    void setOverwritesExistingValue() {
        exchange("*3\r\n$3\r\nSET\r\n$3\r\nfoo\r\n$3\r\nbar\r\n");
        exchange("*3\r\n$3\r\nSET\r\n$3\r\nfoo\r\n$3\r\nbaz\r\n");
        assertEquals("$3\r\nbaz\r\n", exchange("*2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n"));
    }

    // ---------- GET ----------
    // GET returns the value at key as a bulk string, or a nil bulk string if
    // the key does not exist.
    // https://redis.io/commands/get/

    @Test
    void getReturnsNilBulkStringForMissingKey() {
        assertEquals("$-1\r\n", exchange("*2\r\n$3\r\nGET\r\n$7\r\nabsentk\r\n"));
    }

    @Test
    void getIsCaseInsensitiveCommandName() {
        exchange("*3\r\n$3\r\nSET\r\n$1\r\nk\r\n$1\r\nv\r\n");
        assertEquals("$1\r\nv\r\n", exchange("*2\r\n$3\r\nget\r\n$1\r\nk\r\n"));
    }

    // ---------- inline SET / GET ----------
    // Inline commands are equivalent to the corresponding RESP array: the
    // server must accept both interchangeably.

    @Test
    void inlineSetThenInlineGet() {
        assertEquals("+OK\r\n", exchange("SET foo bar\r\n"));
        assertEquals("$3\r\nbar\r\n", exchange("GET foo\r\n"));
    }

    @Test
    void inlineAndRespInteroperate() {
        assertEquals("+OK\r\n", exchange("SET foo bar\r\n"));
        assertEquals("$3\r\nbar\r\n", exchange("*2\r\n$3\r\nGET\r\n$3\r\nfoo\r\n"));
    }

    // ---------- CONFIG ----------
    // CONFIG GET returns an array of alternating name/value bulk strings for
    // parameters matching the pattern. The current server has no configuration
    // keys, so it returns an empty array for any query.
    // https://redis.io/commands/config-get/

    @Test
    void configGetReturnsArray() {
        String response = exchange("*3\r\n$6\r\nCONFIG\r\n$3\r\nGET\r\n$9\r\nmaxmemory\r\n");
        assertTrue(response.startsWith("*"),
                "CONFIG GET must return a RESP array, got: " + response);
    }

    // ---------- unknown command ----------
    // Any command the server does not implement must be answered with a RESP
    // error reply (starting with '-'), not silently with a nil.

    @Test
    void unknownCommandReturnsRespError() {
        String response = exchange("*1\r\n$7\r\nUNKNOWN\r\n");
        assertTrue(response.startsWith("-"),
                "Unknown command must return a RESP error, got: " + response);
    }

    // ---------- helpers ----------

    /**
     * Send a chunk of client-side bytes through the full server pipeline
     * (parse -> dispatch -> encode) and return the response bytes as a String.
     * Uses ISO-8859-1 so every byte round-trips as a single char.
     */
    private static String exchange(String clientBytes) {
        var in = new ByteArrayInputStream(clientBytes.getBytes(StandardCharsets.ISO_8859_1));
        RespValue request = RespParser.parse(in);
        RespValue response = CommandDispatcher.handle(request);
        return response.encode();
    }
}
