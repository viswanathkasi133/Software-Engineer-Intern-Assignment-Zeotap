package io.cdap.wrangler.parser;

import io.cdap.wrangler.api.parser.ByteSize;
import org.junit.Assert;
import org.junit.Test;

public class ByteSizeTest {

    private static final long K = 1024L;
    private static final long M = K * 1024L;
    private static final long G = M * 1024L;

    @Test
    public void testByteSizeParsing() {
        Assert.assertEquals(10 * K, new ByteSize("10k").getBytes());
        Assert.assertEquals(10 * K, new ByteSize("10K").getBytes());
        Assert.assertEquals(10 * K, new ByteSize("10kb").getBytes());
        Assert.assertEquals(10 * K, new ByteSize("10KB").getBytes());
        Assert.assertEquals(10 * K, new ByteSize(" 10 KB ").getBytes());
        Assert.assertEquals(1536, new ByteSize("1.5k").getBytes());
        Assert.assertEquals((long)(1.5 * M), new ByteSize("1.5MB").getBytes());
        Assert.assertEquals(5 * M, new ByteSize("5M").getBytes());
        Assert.assertEquals(2 * G, new ByteSize("2g").getBytes());
        Assert.assertEquals(0, new ByteSize("0GB").getBytes());
        Assert.assertEquals(1024, new ByteSize("1024").getBytes());
        Assert.assertEquals(0, new ByteSize("0").getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormatSpace() {
        new ByteSize("10 K");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormatUnit() {
        new ByteSize("10XB");
    }

     @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormatText() {
        new ByteSize("abc");
    }

    @Test(expected = NullPointerException.class) // Or IllegalArgumentException depending on constructor check
    public void testNullInput() {
        new ByteSize(null);
    }

     @Test(expected = IllegalArgumentException.class)
    public void testEmptyInput() {
        new ByteSize("");
    }
}