package com.perflab.resp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class ArrayTest {

    @Test
    void parsesArrayOfTwoBulkStrings() {
        var result = RespParser.parse("*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n");
        assertInstanceOf(RespArray.class, result.value());
        List<RespValue> elements = ((RespArray) result.value()).values();
        assertEquals(2, elements.size());
        assertInstanceOf(RespBulkString.class, elements.get(0));
        assertEquals("foo", ((RespBulkString) elements.get(0)).value());
        assertInstanceOf(RespBulkString.class, elements.get(1));
        assertEquals("bar", ((RespBulkString) elements.get(1)).value());
    }

    @Test
    void parsesEmptyArray() {
        var result = RespParser.parse("*0\r\n");
        assertInstanceOf(RespArray.class, result.value());
        List<RespValue> elements = ((RespArray) result.value()).values();
        assertTrue(elements.isEmpty());
    }

    @Test
    void parsesNullArray() {
        var result = RespParser.parse("*-1\r\n");
        assertInstanceOf(RespArray.class, result.value());
        assertNull(((RespArray) result.value()).values());
    }

    @Test
    void parsesSingleElementArray() {
        var result = RespParser.parse("*1\r\n+OK\r\n");
        assertInstanceOf(RespArray.class, result.value());
        List<RespValue> elements = ((RespArray) result.value()).values();
        assertEquals(1, elements.size());
        assertInstanceOf(RespSimpleString.class, elements.get(0));
        assertEquals("OK", ((RespSimpleString) elements.get(0)).value());
    }

    @Test
    void parsesArrayOfIntegers() {
        var result = RespParser.parse("*3\r\n:1\r\n:2\r\n:3\r\n");
        assertInstanceOf(RespArray.class, result.value());
        List<RespValue> elements = ((RespArray) result.value()).values();
        assertEquals(3, elements.size());
        assertEquals(1L, ((RespInteger) elements.get(0)).value());
        assertEquals(2L, ((RespInteger) elements.get(1)).value());
        assertEquals(3L, ((RespInteger) elements.get(2)).value());
    }

    @Test
    void parsesArrayWithMixedTypes() {
        var result = RespParser.parse("*4\r\n:1\r\n$3\r\nfoo\r\n+OK\r\n-ERR\r\n");
        assertInstanceOf(RespArray.class, result.value());
        List<RespValue> elements = ((RespArray) result.value()).values();
        assertEquals(4, elements.size());
        assertInstanceOf(RespInteger.class, elements.get(0));
        assertEquals(1L, ((RespInteger) elements.get(0)).value());
        assertInstanceOf(RespBulkString.class, elements.get(1));
        assertEquals("foo", ((RespBulkString) elements.get(1)).value());
        assertInstanceOf(RespSimpleString.class, elements.get(2));
        assertEquals("OK", ((RespSimpleString) elements.get(2)).value());
        assertInstanceOf(RespError.class, elements.get(3));
        assertEquals("ERR", ((RespError) elements.get(3)).value());
    }

    @Test
    void parsesEchoCommand() {
        var result = RespParser.parse("*2\r\n$4\r\necho\r\n$11\r\nhello world\r\n");
        assertInstanceOf(RespArray.class, result.value());
        List<RespValue> elements = ((RespArray) result.value()).values();
        assertEquals(2, elements.size());
        assertEquals("echo", ((RespBulkString) elements.get(0)).value());
        assertEquals("hello world", ((RespBulkString) elements.get(1)).value());
    }

    @Test
    void parsesPingCommand() {
        var result = RespParser.parse("*1\r\n$4\r\nping\r\n");
        assertInstanceOf(RespArray.class, result.value());
        List<RespValue> elements = ((RespArray) result.value()).values();
        assertEquals(1, elements.size());
        assertEquals("ping", ((RespBulkString) elements.get(0)).value());
    }

    @Test
    void parsesSetCommand() {
        var result = RespParser.parse("*3\r\n$3\r\nset\r\n$3\r\nkey\r\n$5\r\nvalue\r\n");
        assertInstanceOf(RespArray.class, result.value());
        List<RespValue> elements = ((RespArray) result.value()).values();
        assertEquals(3, elements.size());
        assertEquals("set", ((RespBulkString) elements.get(0)).value());
        assertEquals("key", ((RespBulkString) elements.get(1)).value());
        assertEquals("value", ((RespBulkString) elements.get(2)).value());
    }

    @Test
    void parsesNestedArrays() {
        var result = RespParser.parse("*2\r\n*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n*1\r\n:42\r\n");
        assertInstanceOf(RespArray.class, result.value());
        List<RespValue> outer = ((RespArray) result.value()).values();
        assertEquals(2, outer.size());

        assertInstanceOf(RespArray.class, outer.get(0));
        List<RespValue> inner1 = ((RespArray) outer.get(0)).values();
        assertEquals(2, inner1.size());
        assertEquals("foo", ((RespBulkString) inner1.get(0)).value());
        assertEquals("bar", ((RespBulkString) inner1.get(1)).value());

        assertInstanceOf(RespArray.class, outer.get(1));
        List<RespValue> inner2 = ((RespArray) outer.get(1)).values();
        assertEquals(1, inner2.size());
        assertEquals(42L, ((RespInteger) inner2.get(0)).value());
    }

    @Test
    void parsesArrayOfSimpleStrings() {
        var result = RespParser.parse("*2\r\n+OK\r\n+PONG\r\n");
        assertInstanceOf(RespArray.class, result.value());
        List<RespValue> elements = ((RespArray) result.value()).values();
        assertEquals(2, elements.size());
        assertEquals("OK", ((RespSimpleString) elements.get(0)).value());
        assertEquals("PONG", ((RespSimpleString) elements.get(1)).value());
    }

    @Test
    void consumedCharsForTwoBulkStrings() {
        // "*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n" = 4 + 9 + 9 = 22
        var result = RespParser.parse("*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n");
        assertEquals(22, result.consumedChars());
    }

    @Test
    void consumedCharsForEmptyArray() {
        // "*0\r\n" = 4
        var result = RespParser.parse("*0\r\n");
        assertEquals(4, result.consumedChars());
    }

    @Test
    void consumedCharsForNullArray() {
        // "*-1\r\n" = 5
        var result = RespParser.parse("*-1\r\n");
        assertEquals(5, result.consumedChars());
    }

    @Test
    void consumedCharsForEchoCommand() {
        String input = "*2\r\n$4\r\necho\r\n$11\r\nhello world\r\n";
        var result = RespParser.parse(input);
        assertEquals(input.length(), result.consumedChars());
    }

    @Test
    void ignoresTrailingData() {
        var result = RespParser.parse("*1\r\n+OK\r\ngarbage");
        assertInstanceOf(RespArray.class, result.value());
        List<RespValue> elements = ((RespArray) result.value()).values();
        assertEquals(1, elements.size());
        assertEquals("OK", ((RespSimpleString) elements.get(0)).value());
        assertEquals(9, result.consumedChars());
    }

    @Test
    void parsesArrayWithBulkStringContainingCrlf() {
        var result = RespParser.parse("*1\r\n$5\r\nhe\r\nlo\r\n");
        assertInstanceOf(RespArray.class, result.value());
        List<RespValue> elements = ((RespArray) result.value()).values();
        assertEquals(1, elements.size());
        assertEquals("he\r\nl", ((RespBulkString) elements.get(0)).value());
    }

    @Test
    void parsesLargeArray() {
        StringBuilder sb = new StringBuilder();
        int count = 10;
        sb.append("*").append(count).append("\r\n");
        for (int i = 0; i < count; i++) {
            sb.append(":").append(i).append("\r\n");
        }
        var result = RespParser.parse(sb.toString());
        assertInstanceOf(RespArray.class, result.value());
        List<RespValue> elements = ((RespArray) result.value()).values();
        assertEquals(count, elements.size());
        for (int i = 0; i < count; i++) {
            assertEquals((long) i, ((RespInteger) elements.get(i)).value());
        }
    }

    // --- encode tests ---

    @Test
    void encodesArrayOfTwoBulkStrings() {
        var array = new RespArray(List.of(
                new RespBulkString("foo"),
                new RespBulkString("bar")));
        assertEquals("*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n", array.encode());
    }

    @Test
    void encodesEmptyArray() {
        var array = new RespArray(List.of());
        assertEquals("*0\r\n", array.encode());
    }

    @Test
    void encodesNullArray() {
        var array = new RespArray(null);
        assertEquals("*-1\r\n", array.encode());
    }

    @Test
    void encodesSingleElementArray() {
        var array = new RespArray(List.of(new RespSimpleString("OK")));
        assertEquals("*1\r\n+OK\r\n", array.encode());
    }

    @Test
    void encodesArrayOfIntegers() {
        var array = new RespArray(List.of(
                new RespInteger(1),
                new RespInteger(2),
                new RespInteger(3)));
        assertEquals("*3\r\n:1\r\n:2\r\n:3\r\n", array.encode());
    }

    @Test
    void encodesArrayWithMixedTypes() {
        var array = new RespArray(List.of(
                new RespInteger(1),
                new RespBulkString("foo"),
                new RespSimpleString("OK"),
                new RespError("ERR")));
        assertEquals("*4\r\n:1\r\n$3\r\nfoo\r\n+OK\r\n-ERR\r\n", array.encode());
    }

    @Test
    void encodesEchoCommand() {
        var array = new RespArray(List.of(
                new RespBulkString("echo"),
                new RespBulkString("hello world")));
        assertEquals("*2\r\n$4\r\necho\r\n$11\r\nhello world\r\n", array.encode());
    }

    @Test
    void encodesPingCommand() {
        var array = new RespArray(List.of(new RespBulkString("ping")));
        assertEquals("*1\r\n$4\r\nping\r\n", array.encode());
    }

    @Test
    void encodesSetCommand() {
        var array = new RespArray(List.of(
                new RespBulkString("set"),
                new RespBulkString("key"),
                new RespBulkString("value")));
        assertEquals("*3\r\n$3\r\nset\r\n$3\r\nkey\r\n$5\r\nvalue\r\n", array.encode());
    }

    @Test
    void encodesNestedArrays() {
        var inner1 = new RespArray(List.of(
                new RespBulkString("foo"),
                new RespBulkString("bar")));
        var inner2 = new RespArray(List.of(new RespInteger(42)));
        var outer = new RespArray(List.of(inner1, inner2));
        assertEquals("*2\r\n*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n*1\r\n:42\r\n", outer.encode());
    }

    @Test
    void encodesArrayOfSimpleStrings() {
        var array = new RespArray(List.of(
                new RespSimpleString("OK"),
                new RespSimpleString("PONG")));
        assertEquals("*2\r\n+OK\r\n+PONG\r\n", array.encode());
    }

    @Test
    void encodesArrayOfErrors() {
        var array = new RespArray(List.of(
                new RespError("ERR first"),
                new RespError("ERR second")));
        assertEquals("*2\r\n-ERR first\r\n-ERR second\r\n", array.encode());
    }

    @Test
    void encodesArrayWithNullBulkString() {
        var array = new RespArray(List.of(
                new RespBulkString("hello"),
                new RespBulkString(null),
                new RespBulkString("world")));
        assertEquals("*3\r\n$5\r\nhello\r\n$-1\r\n$5\r\nworld\r\n", array.encode());
    }

    @Test
    void roundTripTwoBulkStrings() {
        var original = new RespArray(List.of(
                new RespBulkString("foo"),
                new RespBulkString("bar")));
        var parsed = RespParser.parse(original.encode());
        assertInstanceOf(RespArray.class, parsed.value());
        List<RespValue> elements = ((RespArray) parsed.value()).values();
        assertEquals(2, elements.size());
        assertEquals("foo", ((RespBulkString) elements.get(0)).value());
        assertEquals("bar", ((RespBulkString) elements.get(1)).value());
    }

    @Test
    void roundTripEmptyArray() {
        var original = new RespArray(List.of());
        var parsed = RespParser.parse(original.encode());
        assertInstanceOf(RespArray.class, parsed.value());
        assertTrue(((RespArray) parsed.value()).values().isEmpty());
    }

    @Test
    void roundTripNullArray() {
        var original = new RespArray(null);
        var parsed = RespParser.parse(original.encode());
        assertInstanceOf(RespArray.class, parsed.value());
        assertNull(((RespArray) parsed.value()).values());
    }

    @Test
    void roundTripMixedTypes() {
        var original = new RespArray(List.of(
                new RespInteger(42),
                new RespBulkString("hello"),
                new RespSimpleString("OK"),
                new RespError("ERR")));
        var parsed = RespParser.parse(original.encode());
        assertInstanceOf(RespArray.class, parsed.value());
        List<RespValue> elements = ((RespArray) parsed.value()).values();
        assertEquals(4, elements.size());
        assertEquals(42L, ((RespInteger) elements.get(0)).value());
        assertEquals("hello", ((RespBulkString) elements.get(1)).value());
        assertEquals("OK", ((RespSimpleString) elements.get(2)).value());
        assertEquals("ERR", ((RespError) elements.get(3)).value());
    }

    @Test
    void roundTripNestedArrays() {
        var inner = new RespArray(List.of(new RespInteger(1), new RespInteger(2)));
        var outer = new RespArray(List.of(inner, new RespBulkString("end")));
        var parsed = RespParser.parse(outer.encode());
        assertInstanceOf(RespArray.class, parsed.value());
        List<RespValue> outerElements = ((RespArray) parsed.value()).values();
        assertEquals(2, outerElements.size());
        assertInstanceOf(RespArray.class, outerElements.get(0));
        List<RespValue> innerElements = ((RespArray) outerElements.get(0)).values();
        assertEquals(2, innerElements.size());
        assertEquals(1L, ((RespInteger) innerElements.get(0)).value());
        assertEquals(2L, ((RespInteger) innerElements.get(1)).value());
        assertEquals("end", ((RespBulkString) outerElements.get(1)).value());
    }

    @Test
    void roundTripSetCommand() {
        var original = new RespArray(List.of(
                new RespBulkString("set"),
                new RespBulkString("mykey"),
                new RespBulkString("myvalue")));
        var parsed = RespParser.parse(original.encode());
        assertInstanceOf(RespArray.class, parsed.value());
        List<RespValue> elements = ((RespArray) parsed.value()).values();
        assertEquals(3, elements.size());
        assertEquals("set", ((RespBulkString) elements.get(0)).value());
        assertEquals("mykey", ((RespBulkString) elements.get(1)).value());
        assertEquals("myvalue", ((RespBulkString) elements.get(2)).value());
    }

    @Test
    void encodeMatchesParseInput() {
        String wire = "*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n";
        var parsed = RespParser.parse(wire);
        assertEquals(wire, parsed.value().encode());
    }

    @Test
    void encodeMatchesParseInputForNullArray() {
        String wire = "*-1\r\n";
        var parsed = RespParser.parse(wire);
        assertEquals(wire, parsed.value().encode());
    }

    @Test
    void encodeMatchesParseInputForNestedArrays() {
        String wire = "*2\r\n*2\r\n$3\r\nfoo\r\n$3\r\nbar\r\n*1\r\n:42\r\n";
        var parsed = RespParser.parse(wire);
        assertEquals(wire, parsed.value().encode());
    }

    @Test
    void roundTripLargeArray() {
        var elements = new java.util.ArrayList<RespValue>();
        for (int i = 0; i < 10; i++) {
            elements.add(new RespInteger(i));
        }
        var original = new RespArray(elements);
        var parsed = RespParser.parse(original.encode());
        assertInstanceOf(RespArray.class, parsed.value());
        List<RespValue> parsedElements = ((RespArray) parsed.value()).values();
        assertEquals(10, parsedElements.size());
        for (int i = 0; i < 10; i++) {
            assertEquals((long) i, ((RespInteger) parsedElements.get(i)).value());
        }
    }
}
