package io.cdap.wrangler.parser;

import io.cdap.wrangler.api.parser.TimeDuration;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class TimeDurationTest {

    private static final long NANO = 1L;
    private static final long MICRO = 1000 * NANO;
    private static final long MILLI = 1000 * MICRO;
    private static final long SECOND = 1000 * MILLI;
    private static final long MINUTE = 60 * SECOND;
    private static final long HOUR = 60 * MINUTE;
    private static final long DAY = 24 * HOUR;

    @Test
    public void testTimeDurationParsingNanos() {
        Assert.assertEquals(150 * MILLI, new TimeDuration("150ms").getNanoseconds());
        Assert.assertEquals(150 * MILLI, new TimeDuration("150 MS ").getNanoseconds());
        Assert.assertEquals(150 * MILLI, new TimeDuration("150milli").getNanoseconds());
        Assert.assertEquals(150 * MILLI, new TimeDuration("150milliseconds").getNanoseconds());
        Assert.assertEquals((long)(2.5 * SECOND), new TimeDuration("2.5s").getNanoseconds());
        Assert.assertEquals(5 * SECOND, new TimeDuration("5 sec").getNanoseconds());
        Assert.assertEquals(10 * SECOND, new TimeDuration("10seconds").getNanoseconds());
        Assert.assertEquals(3 * MINUTE, new TimeDuration("3m").getNanoseconds());
        Assert.assertEquals(3 * MINUTE, new TimeDuration("3min").getNanoseconds());
        Assert.assertEquals(3 * MINUTE, new TimeDuration("3minutes").getNanoseconds());
        Assert.assertEquals(1 * HOUR, new TimeDuration("1h").getNanoseconds());
        Assert.assertEquals(1 * HOUR, new TimeDuration("1hr").getNanoseconds());
        Assert.assertEquals(1 * HOUR, new TimeDuration("1 Hour").getNanoseconds());
        Assert.assertEquals(500 * NANO, new TimeDuration("500ns").getNanoseconds());
        Assert.assertEquals(250 * MICRO, new TimeDuration("250us").getNanoseconds());
        Assert.assertEquals(2 * DAY, new TimeDuration("2d").getNanoseconds());
        Assert.assertEquals(2 * DAY, new TimeDuration("2 days").getNanoseconds());
        Assert.assertEquals(100 * MILLI, new TimeDuration("100").getNanoseconds());
        Assert.assertEquals(0, new TimeDuration("0").getNanoseconds());
    }

     @Test
    public void testTimeDurationGetDuration() {
         TimeDuration td = new TimeDuration("1.5s");
         Assert.assertEquals(1.5, td.getDuration(TimeUnit.SECONDS), 0.001);
         Assert.assertEquals(1500.0, td.getDuration(TimeUnit.MILLISECONDS), 0.001);
         Assert.assertEquals(1_500_000_000.0, td.getDuration(TimeUnit.NANOSECONDS), 0.001);
         Assert.assertEquals(0.025, td.getDuration(TimeUnit.MINUTES), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormatSpace() {
        new TimeDuration("150 ms");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormatUnit() {
        new TimeDuration("10years");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormatText() {
        new TimeDuration("fast");
    }

    @Test(expected = NullPointerException.class) // Or IllegalArgumentException depending on constructor check
    public void testNullInput() {
         new TimeDuration(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyInput() {
         new TimeDuration("");
    }
}