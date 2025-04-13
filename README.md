# Software-Engineer-Intern-Assignment-Zeotap

🚀 CDAP Wrangler Enhancements: Byte Size and Time Duration Support
📝 Overview
Added native support for parsing, handling, and aggregating:

Byte sizes (e.g., 10KB, 1.5MB)

Time durations (e.g., 250ms, 3.5s)

Simplifies data preparation involving:

File sizes

Transfer volumes

Latency measurements

🧩 Core Components Added
🔹 ByteSize Parser
Recognizes units: KB, MB, GB, TB, PB (case-insensitive, optional B)

Plain numbers = bytes

Canonical format: long getBytes()

Uses 1024 multiplier (1 KB = 1024 Bytes)

🔹 TimeDuration Parser
Recognizes units: ns, us, ms, s, sec, m, min, h, hr, d (case-insensitive)

Plain numbers = milliseconds

Canonical format: long getNanoseconds()

Supports conversion to Java TimeUnit

🔹 Grammar Updates
Updated ANTLR grammar (Directives.g4)

Added BYTE_SIZE and TIME_DURATION tokens

🔹 New Directive: aggregate-stats
Aggregates across rows for byte size and time duration columns

Emits a single output row with totals or averages

🧪 New Directive: aggregate-stats
📌 Syntax
wrangler
Copy
Edit
aggregate-stats <size-column> <time-column> <target-size-column> <target-time-column> [size_unit:<unit>] [time_unit:<unit>] [time_mode:<mode>]
📌 Arguments
<size-column>: Column with byte size values (e.g., "10KB", "2097152")

<time-column>: Column with time duration values (e.g., "150ms", "2.5s")

<target-size-column>: Output column for total size

<target-time-column>: Output column for total or average time

📌 Optional Options (key:value)
size_unit:<unit>

Units: BYTES, KB, MB, GB, TB, PB

Default: BYTES

Output: long for BYTES, double for others (rounded to 3 decimals)

time_unit:<unit>

Units: NANOS, MICROS, MS, S, SECONDS, MINUTES, HOURS, DAYS

Default: NANOS

Output: long for NANOS, double for others (rounded to 3 decimals)

time_mode:<mode>

Modes: TOTAL, AVERAGE

Default: TOTAL

⚙️ Behavior
Processes all input rows

Aggregates:

Total size in bytes

Total time in nanoseconds

Tracks row count for average calculations

Skips rows with unparseable values (counts them for averaging if partially valid)

Emits one final row with calculated values

Outputs nothing if no rows are processed

📌 Example Usage
➤ Parse CSV Data
wrangler
Copy
Edit
parse-as-csv :body ',' true;
➤ Aggregate Total Download Size (MB) and Average Response Time (s)
wrangler
Copy
Edit
aggregate-stats :download_size_col :latency_col :total_dl_mb :avg_response_s size_unit:MB time_unit:s time_mode:average
➤ Keep Only Result Columns
wrangler
Copy
Edit
keep :total_dl_mb :avg_response_s
