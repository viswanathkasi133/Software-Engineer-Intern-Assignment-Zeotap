CDAP Wrangler Enhancements: Byte Size and Time Duration Support
Overview
This enhancement integrates native support for parsing, handling, and aggregating data representing byte sizes (like "10KB" or "1.5MB") and time durations (like "250ms" or "3.5s") directly within Wrangler recipes. This simplifies data preparation tasks involving file sizes, transfer volumes, latency measurements, and other similar metrics.

Core Components Added
ByteSize Parser:

Recognizes common byte units (KB, MB, GB, TB, PB, case-insensitive, optional 'B').

Plain numbers are treated as bytes.

Provides canonical values in bytes using the long getBytes() method. (1 KB = 1024 Bytes).

TimeDuration Parser:

Recognizes common time units (ns, us, ms, s, sec, m, min, h, hr, d, case-insensitive).

Plain numbers are treated as milliseconds.

Provides canonical values in nanoseconds using the long getNanoseconds() method.

Allows conversion to other Java TimeUnits.

Grammar Updates:

The core ANTLR grammar (Directives.g4) is updated with BYTE_SIZE and TIME_DURATION tokens.

New Directive: aggregate-stats:

Performs aggregation across rows for specified byte size and time duration columns.

Outputs a single row with the results.

New Directive: aggregate-stats
This directive processes the entire dataset (or current batch in a pipeline context) to calculate aggregate statistics for size and time columns, outputting a single row with the results.

Syntax
plaintext
Copy
Edit
wrangler
aggregate-stats <size-column> <time-column> <target-size-column> <target-time-column> [size_unit:<unit>] [time_unit:<unit>] [time_mode:<mode>]
Arguments
<size-column> (ColumnName):

The source column containing byte size values (e.g., "10KB", "2097152").

Accepts strings matching the ByteSize format or plain numbers (interpreted as bytes).

<time-column> (ColumnName):

The source column containing time duration values (e.g., "150ms", "2.5s").

Accepts strings matching the TimeDuration format or plain numbers (interpreted as milliseconds).

<target-size-column> (ColumnName):

Name for the new output column that will contain the calculated total size.

<target-time-column> (ColumnName):

Name for the new output column that will contain the calculated total or average time.

Optional Options (Specified as key:value):
size_unit:<unit> (Text):

Specifies the unit for the output <target-size-column>.

Supported units: BYTES, KB, MB, GB, TB, PB.

Default: BYTES.

Output type is long for BYTES and double for others (rounded to 3 decimal places).

time_unit:<unit> (Text):

Specifies the unit for the output <target-time-column>.

Supported units: NANOS, MICROS, MS, S (or SECONDS), MINUTES, HOURS, DAYS.

Default: NANOS.

Output type is long for NANOS (when mode is TOTAL or AVERAGE on whole nanoseconds) and double for others (rounded to 3 decimal places).

time_mode:<mode> (Text):

Specifies the aggregation calculation for the time column.

Supported modes: TOTAL, AVERAGE.

Default: TOTAL.

Behavior
The directive processes all input rows.

It accumulates the total size (in bytes) and total time (in nanoseconds) from the specified source columns.

It tracks the number of rows processed for calculating averages.

Rows with null or unparseable values in the source columns are skipped for that specific calculation (the other value in the row might still be aggregated), but the row is counted for the AVERAGE mode denominator.

In the finish phase (or at the end of processing), it calculates the final values based on the specified size_unit, time_unit, and time_mode.

It emits a single row containing the final calculated values for both the size and time columns.

If no rows are processed, it outputs an empty result (0 rows).

Example Usage
Parse CSV data

plaintext
Copy
Edit
parse-as-csv :body ',' true;
Calculate total download size in Megabytes (MB) and average response time in seconds (s)

plaintext
Copy
Edit
aggregate-stats :download_size_col :latency_col :total_dl_mb :avg_response_s size_unit:MB time_unit:s time_mode:average
Keep only the aggregate results (optional, useful after aggregation)

plaintext
Copy
Edit
keep :total_dl_mb :avg_response_s
