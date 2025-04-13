package io.cdap.wrangler.api.parser;

import io.cdap.wrangler.api.annotations.Public;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Public
public class ByteSize extends Token implements Serializable {
    private static final long serialVersionUID = 6789012345L; 

    private final long bytes;
    private final String originalValue;

    private static final Pattern BYTE_PATTERN = Pattern.compile("(\\d+(\\.\\d+)?)\\s*([kKmMgGtTpP])([bB])?", Pattern.CASE_INSENSITIVE);
    private static final long KILO = 1024L;
    private static final long MEGA = KILO * 1024L;
    private static final long GIGA = MEGA * 1024L;
    private static final long TERA = GIGA * 1024L;
    private static final long PETA = TERA * 1024L;

    public ByteSize(String text) {
        super(TokenType.BYTE_SIZE, text);
        this.originalValue = Objects.requireNonNull(text, "Input text cannot be null");
        this.bytes = parseByteSize(text);
    }

    private long parseByteSize(String text) {
        String trimmedText = text.trim();
        if (trimmedText.isEmpty()) {
            throw new IllegalArgumentException("Byte size string cannot be empty.");
        }

        Matcher matcher = BYTE_PATTERN.matcher(trimmedText);
        if (!matcher.matches()) {
            try {
                return Long.parseLong(trimmedText);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid byte size format: '" + text + "'. Expected format like '10KB', '1.5MB', or a plain number for bytes.");
            }
        }

        String numStr = matcher.group(1);
        String unitStr = matcher.group(3).toUpperCase();

        double numValue = Double.parseDouble(numStr);
        long multiplier = 1L;

        switch (unitStr) {
            case "K": multiplier = KILO; break;
            case "M": multiplier = MEGA; break;
            case "G": multiplier = GIGA; break;
            case "T": multiplier = TERA; break;
            case "P": multiplier = PETA; break;
        }

        double byteValue = numValue * multiplier;
        if (byteValue > Long.MAX_VALUE || byteValue < Long.MIN_VALUE) {
             throw new IllegalArgumentException("Byte size value out of range: '" + text + "'");
        }
        return (long) byteValue;
    }

    public long getBytes() {
        return bytes;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    @Override
    public String toString() {
        return "ByteSize{" +
                "bytes=" + bytes +
                ", originalValue='" + originalValue + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ByteSize byteSize = (ByteSize) o;
        return bytes == byteSize.bytes && Objects.equals(originalValue, byteSize.originalValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bytes, originalValue);
    }
}