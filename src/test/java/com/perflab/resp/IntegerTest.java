package com.perflab.resp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

public class IntegerTest {

    @Test
    void parsesPositiveInteger() {
        var result = RespParser.parse(":35\r\n");
        assertInstanceOf(RespInteger.class, result.value());
        assertEquals(35L, ((RespInteger) result.value()).value());
    }

    @Test
    void parsesZero() {
        var result = RespParser.parse(":0\r\n");
        assertInstanceOf(RespInteger.class, result.value());
        assertEquals(0L, ((RespInteger) result.value()).value());
    }

    @Test
    void parsesNegativeInteger() {
        var result = RespParser.parse(":-1\r\n");
        assertInstanceOf(RespInteger.class, result.value());
        assertEquals(-1L, ((RespInteger) result.value()).value());
    }

    @Test
    void parsesLargePositiveInteger() {
        var result = RespParser.parse(":999999999999\r\n");
        assertInstanceOf(RespInteger.class, result.value());
        assertEquals(999999999999L, ((RespInteger) result.value()).value());
    }

    @Test
    void parsesLargeNegativeInteger() {
        var result = RespParser.parse(":-999999999999\r\n");
        assertInstanceOf(RespInteger.class, result.value());
        assertEquals(-999999999999L, ((RespInteger) result.value()).value());
    }

    @Test
    void parsesSingleDigit() {
        var result = RespParser.parse(":5\r\n");
        assertInstanceOf(RespInteger.class, result.value());
        assertEquals(5L, ((RespInteger) result.value()).value());
    }

    @Test
    void parsesOne() {
        var result = RespParser.parse(":1\r\n");
        assertInstanceOf(RespInteger.class, result.value());
        assertEquals(1L, ((RespInteger) result.value()).value());
    }

    @Test
    void parsesOneThousand() {
        var result = RespParser.parse(":1000\r\n");
        assertInstanceOf(RespInteger.class, result.value());
        assertEquals(1000L, ((RespInteger) result.value()).value());
    }

    @Test
    void parsesMaxLong() {
        var result = RespParser.parse(":" + Long.MAX_VALUE + "\r\n");
        assertInstanceOf(RespInteger.class, result.value());
        assertEquals(Long.MAX_VALUE, ((RespInteger) result.value()).value());
    }

    @Test
    void parsesMinLong() {
        var result = RespParser.parse(":" + Long.MIN_VALUE + "\r\n");
        assertInstanceOf(RespInteger.class, result.value());
        assertEquals(Long.MIN_VALUE, ((RespInteger) result.value()).value());
    }

    @Test
    void consumedCharsIsCorrect() {
        var result = RespParser.parse(":35\r\n");
        assertEquals(5, result.consumedChars());
    }

    @Test
    void consumedCharsForZero() {
        var result = RespParser.parse(":0\r\n");
        assertEquals(4, result.consumedChars());
    }

    @Test
    void consumedCharsForNegative() {
        var result = RespParser.parse(":-1\r\n");
        assertEquals(5, result.consumedChars());
    }

    @Test
    void consumedCharsForLargeNumber() {
        String input = ":999999999999\r\n";
        var result = RespParser.parse(input);
        assertEquals(input.length(), result.consumedChars());
    }

    @Test
    void ignoresTrailingData() {
        var result = RespParser.parse(":42\r\nextra");
        assertInstanceOf(RespInteger.class, result.value());
        assertEquals(42L, ((RespInteger) result.value()).value());
        assertEquals(5, result.consumedChars());
    }

    // --- encode tests ---

    @Test
    void encodesPositiveInteger() {
        assertEquals(":35\r\n", new RespInteger(35).encode());
    }

    @Test
    void encodesZero() {
        assertEquals(":0\r\n", new RespInteger(0).encode());
    }

    @Test
    void encodesNegativeInteger() {
        assertEquals(":-1\r\n", new RespInteger(-1).encode());
    }

    @Test
    void encodesOne() {
        assertEquals(":1\r\n", new RespInteger(1).encode());
    }

    @Test
    void encodesOneThousand() {
        assertEquals(":1000\r\n", new RespInteger(1000).encode());
    }

    @Test
    void encodesLargePositive() {
        assertEquals(":999999999999\r\n", new RespInteger(999999999999L).encode());
    }

    @Test
    void encodesLargeNegative() {
        assertEquals(":-999999999999\r\n", new RespInteger(-999999999999L).encode());
    }

    @Test
    void encodesMaxLong() {
        assertEquals(":" + Long.MAX_VALUE + "\r\n", new RespInteger(Long.MAX_VALUE).encode());
    }

    @Test
    void encodesMinLong() {
        assertEquals(":" + Long.MIN_VALUE + "\r\n", new RespInteger(Long.MIN_VALUE).encode());
    }

    @Test
    void roundTripPositive() {
        var original = new RespInteger(42);
        var parsed = RespParser.parse(original.encode());
        assertInstanceOf(RespInteger.class, parsed.value());
        assertEquals(42L, ((RespInteger) parsed.value()).value());
    }

    @Test
    void roundTripZero() {
        var original = new RespInteger(0);
        var parsed = RespParser.parse(original.encode());
        assertInstanceOf(RespInteger.class, parsed.value());
        assertEquals(0L, ((RespInteger) parsed.value()).value());
    }

    @Test
    void roundTripNegative() {
        var original = new RespInteger(-100);
        var parsed = RespParser.parse(original.encode());
        assertInstanceOf(RespInteger.class, parsed.value());
        assertEquals(-100L, ((RespInteger) parsed.value()).value());
    }

    @Test
    void roundTripMaxLong() {
        var original = new RespInteger(Long.MAX_VALUE);
        var parsed = RespParser.parse(original.encode());
        assertInstanceOf(RespInteger.class, parsed.value());
        assertEquals(Long.MAX_VALUE, ((RespInteger) parsed.value()).value());
    }

    @Test
    void encodeMatchesParseInput() {
        String wire = ":1000\r\n";
        var parsed = RespParser.parse(wire);
        assertEquals(wire, parsed.value().encode());
    }
}
