package io.cdap.wrangler.api.parser;

import io.cdap.wrangler.api.annotations.Public;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Public
public class TimeDuration extends Token implements Serializable {
    private static final long serialVersionUID = -9876543210L; // Use a generated value

    private final long nanoseconds;
    private final String originalValue;

    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(\\d+(\\.\\d+)?)\\s*(ns|nanos?|us|micros?|ms|millis?|s|sec(?:onds?)?|m|min(?:utes?)?|h|hr|hours?|d|days?)",
            Pattern.CASE_INSENSITIVE);

    public TimeDuration(String text) {
        super(TokenType.TIME_DURATION, text);
        this.originalValue = Objects.requireNonNull(text, "Input text cannot be null");
        this.nanoseconds = parseDurationToNanos(text);
    }

    private long parseDurationToNanos(String text) {
        String trimmedText = text.trim();
        if (trimmedText.isEmpty()) {
            throw new IllegalArgumentException("Time duration string cannot be empty.");
        }

        Matcher matcher = TIME_PATTERN.matcher(trimmedText);
        if (!matcher.matches()) {
            try {
                double numValue = Double.parseDouble(trimmedText);
                return convertToNanos(numValue, TimeUnit.MILLISECONDS); // Default unit: milliseconds
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid time duration format: '" + text + "'. Expected format like '150ms', '2.5s', or a plain number for milliseconds.");
            }
        }

        String numStr = matcher.group(1);
        String unitStr = matcher.group(3).toLowerCase();
        double numValue = Double.parseDouble(numStr);
        TimeUnit unit;

        switch (unitStr) {
            case "ns": case "nano": case "nanos": unit = TimeUnit.NANOSECONDS; break;
            case "us": case "micro": case "micros": unit = TimeUnit.MICROSECONDS; break;
            case "ms": case "milli": case "millis": unit = TimeUnit.MILLISECONDS; break;
            case "s": case "sec": case "second": case "seconds": unit = TimeUnit.SECONDS; break;
            case "m": case "min": case "minute": case "minutes": unit = TimeUnit.MINUTES; break;
            case "h": case "hr": case "hour": case "hours": unit = TimeUnit.HOURS; break;
            case "d": case "day": case "days": unit = TimeUnit.DAYS; break;
            default: throw new IllegalArgumentException("Unknown time unit in: '" + text + "'");
        }

        return convertToNanos(numValue, unit);
    }

    private long convertToNanos(double value, TimeUnit unit) {
        double nanos = value * unit.toNanos(1);
         if (nanos > Long.MAX_VALUE || nanos < Long.MIN_VALUE) {
             throw new IllegalArgumentException("Time duration value out of range: cannot fit in Long nanoseconds.");
         }
        return (long) nanos;
    }

    public long getNanoseconds() {
        return nanoseconds;
    }

    public double getDuration(TimeUnit targetUnit) {
        Objects.requireNonNull(targetUnit, "Target TimeUnit cannot be null");
        if (targetUnit == TimeUnit.NANOSECONDS) {
            return (double) nanoseconds;
        }
        return (double) nanoseconds / TimeUnit.NANOSECONDS.convert(1, targetUnit);
    }

    public String getOriginalValue() {
        return originalValue;
    }

    @Override
    public String toString() {
        return "TimeDuration{" +
                "nanoseconds=" + nanoseconds +
                ", originalValue='" + originalValue + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeDuration that = (TimeDuration) o;
        return nanoseconds == that.nanoseconds && Objects.equals(originalValue, that.originalValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nanoseconds, originalValue);
    }
}