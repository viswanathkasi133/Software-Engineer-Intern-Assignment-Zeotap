package io.cdap.wrangler.directive;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Optional;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.TransientStore;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Plugin(type = Directive.Type)
@Name(AggregateStats.NAME)
@Description("Aggregates byte size and time duration columns, outputting total/average values.")
public class AggregateStats implements Directive, Serializable {
    private static final long serialVersionUID = 1L; // Use appropriate serialVersionUID
    public static final String NAME = "aggregate-stats";

    private String sizeCol;
    private String timeCol;
    private String targetSizeCol;
    private String targetTimeCol;
    private String sizeOutputUnit = "BYTES";
    private String timeOutputUnit = "NANOS";
    private String timeAggregationMode = "TOTAL";

    private static final String TOTAL_BYTES_KEY = "aggstats.total.bytes";
    private static final String TOTAL_NANOS_KEY = "aggstats.total.nanos";
    private static final String ROW_COUNT_KEY = "aggstats.row.count";

    private static final Set<String> VALID_SIZE_UNITS = new HashSet<>(Arrays.asList("BYTES", "KB", "MB", "GB", "TB", "PB"));
    private static final Set<String> VALID_TIME_UNITS = new HashSet<>(Arrays.asList("NANOS", "MICROS", "MS", "S", "SECONDS", "MINUTES", "HOURS", "DAYS"));
    private static final Set<String> VALID_TIME_MODES = new HashSet<>(Arrays.asList("TOTAL", "AVERAGE"));


    @Override
    public UsageDefinition define() {
        return UsageDefinition.builder(NAME)
                .define("size_column", TokenType.COLUMN)
                .define("time_column", TokenType.COLUMN)
                .define("target_size_column", TokenType.COLUMN)
                .define("target_time_column", TokenType.COLUMN)
                .define("size_unit", TokenType.TEXT, Optional.TRUE)
                .define("time_unit", TokenType.TEXT, Optional.TRUE)
                .define("time_mode", TokenType.TEXT, Optional.TRUE)
                .build();
    }

    @Override
    public void initialize(Arguments args) throws DirectiveParseException {
        this.sizeCol = ((ColumnName) args.value("size_column")).value();
        this.timeCol = ((ColumnName) args.value("time_column")).value();
        this.targetSizeCol = ((ColumnName) args.value("target_size_column")).value();
        this.targetTimeCol = ((ColumnName) args.value("target_time_column")).value();

        if (args.contains("size_unit")) {
            String unit = ((Text) args.value("size_unit")).value().toUpperCase();
            if (!VALID_SIZE_UNITS.contains(unit)) {
                 throw new DirectiveParseException(NAME, "Invalid 'size_unit'. Supported: " + VALID_SIZE_UNITS);
            }
            this.sizeOutputUnit = unit;
        }
         if (args.contains("time_unit")) {
            String unit = ((Text) args.value("time_unit")).value().toUpperCase();
             if (!VALID_TIME_UNITS.contains(unit)) {
                 throw new DirectiveParseException(NAME, "Invalid 'time_unit'. Supported: " + VALID_TIME_UNITS);
             }
             this.timeOutputUnit = "SECONDS".equals(unit) ? "S" : unit; // Normalize
        }
         if (args.contains("time_mode")) {
             String mode = ((Text) args.value("time_mode")).value().toUpperCase();
             if (!VALID_TIME_MODES.contains(mode)) {
                  throw new DirectiveParseException(NAME, "Invalid 'time_mode'. Supported: " + VALID_TIME_MODES);
             }
             this.timeAggregationMode = mode;
         }
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
        TransientStore store = context.getTransientStore();

        long currentTotalBytes = store.computeIfAbsent(TOTAL_BYTES_KEY, k -> 0L);
        long currentTotalNanos = store.computeIfAbsent(TOTAL_NANOS_KEY, k -> 0L);
        long currentRowCount = store.computeIfAbsent(ROW_COUNT_KEY, k -> 0L);

        for (Row row : rows) {
            Object sizeValue = row.getValue(sizeCol);
            if (sizeValue != null) {
                try {
                    currentTotalBytes += parseSizeValue(sizeValue);
                } catch (IllegalArgumentException e) {
                     // Log or count error using context.getMetrics() if needed
                }
            }

            Object timeValue = row.getValue(timeCol);
             if (timeValue != null) {
                try {
                     currentTotalNanos += parseTimeValue(timeValue);
                } catch (IllegalArgumentException e) {
                    // Log or count error using context.getMetrics() if needed
                }
            }
            currentRowCount++;
        }

        store.set(TOTAL_BYTES_KEY, currentTotalBytes);
        store.set(TOTAL_NANOS_KEY, currentTotalNanos);
        store.set(ROW_COUNT_KEY, currentRowCount);

        return new ArrayList<>();
    }

     private long parseSizeValue(Object value) throws IllegalArgumentException {
         if (value instanceof ByteSize) {
             return ((ByteSize) value).getBytes();
         } else if (value instanceof String) {
             return new ByteSize((String) value).getBytes();
         } else if (value instanceof Number) {
              return ((Number) value).longValue();
         }
         throw new IllegalArgumentException("Unsupported type for byte size column: " + value.getClass().getName());
     }

     private long parseTimeValue(Object value) throws IllegalArgumentException {
         if (value instanceof TimeDuration) {
             return ((TimeDuration) value).getNanoseconds();
         } else if (value instanceof String) {
             return new TimeDuration((String) value).getNanoseconds();
         } else if (value instanceof Number) {
             double ms = ((Number) value).doubleValue();
             double nanos = ms * 1_000_000.0;
              if (nanos > Long.MAX_VALUE || nanos < Long.MIN_VALUE) {
                 throw new IllegalArgumentException("Numeric time value out of range when converted to nanoseconds.");
              }
             return (long) nanos;
         }
          throw new IllegalArgumentException("Unsupported type for time duration column: " + value.getClass().getName());
     }


    @Override
    public List<Row> finish(ExecutorContext context) throws DirectiveExecutionException {
        TransientStore store = context.getTransientStore();

        long totalBytes = store.get(TOTAL_BYTES_KEY);
        long totalNanos = store.get(TOTAL_NANOS_KEY);
        long rowCount = store.get(ROW_COUNT_KEY);

        if (rowCount == 0) {
             return new ArrayList<>();
        }

        Object finalSizeValue = convertBytesToOutputUnit(totalBytes, sizeOutputUnit);
        Object finalTimeValue = convertNanosToOutputUnit(totalNanos, timeOutputUnit, timeAggregationMode, rowCount);

        Row resultRow = new Row();
        resultRow.add(targetSizeCol, finalSizeValue);
        resultRow.add(targetTimeCol, finalTimeValue);

        return List.of(resultRow);
    }

     private Object convertBytesToOutputUnit(long bytes, String unit) {
        double value = (double) bytes;
        long divisor = 1L;
        long K = 1024L;
        long M = K * 1024L;
        long G = M * 1024L;
        long T = G * 1024L;
        long P = T * 1024L;

        switch (unit) {
            case "KB": divisor = K; break;
            case "MB": divisor = M; break;
            case "GB": divisor = G; break;
            case "TB": divisor = T; break;
            case "PB": divisor = P; break;
            case "BYTES":
            default: return bytes;
        }
        return BigDecimal.valueOf(value / divisor).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

     private Object convertNanosToOutputUnit(long nanos, String unit, String mode, long count) {
         double valueToConvert = (double) nanos;

         if ("AVERAGE".equals(mode) && count > 0) {
            valueToConvert = valueToConvert / count;
         }

         TimeUnit targetTimeUnit;
          switch (unit) {
            case "MICROS": targetTimeUnit = TimeUnit.MICROSECONDS; break;
            case "MS": targetTimeUnit = TimeUnit.MILLISECONDS; break;
            case "S": targetTimeUnit = TimeUnit.SECONDS; break;
            case "MINUTES": targetTimeUnit = TimeUnit.MINUTES; break;
            case "HOURS": targetTimeUnit = TimeUnit.HOURS; break;
            case "DAYS": targetTimeUnit = TimeUnit.DAYS; break;
            case "NANOS":
            default: return (long) valueToConvert;
          }

          double result = valueToConvert / TimeUnit.NANOSECONDS.convert(1, targetTimeUnit);
          return BigDecimal.valueOf(result).setScale(3, RoundingMode.HALF_UP).doubleValue();
     }

    @Override
    public void destroy() {
        // No-op
    }
}