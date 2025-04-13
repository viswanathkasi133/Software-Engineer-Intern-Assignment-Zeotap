# Format Date

The FORMAT-DATE directive allows custom patterns for date-time formatting.


## Syntax
```
format-date <column> <format>
```

## Usage Notes

Date and time formats are specified by date and time pattern strings. Within pattern
strings, unquoted letters from 'A' to 'Z' and from 'a' to 'z' are interpreted as pattern
letters representing the components of a date or time string. Text can be quoted using
single quotes (`'`) to avoid interpretation. Two single quotes `''` represents a single
quote. All other characters are not interpreted; they're simply copied into the output
string during formatting or matched against the input string during parsing.

These pattern letters are defined (all other characters from 'A' to 'Z' and from 'a' to 'z' are reserved):

| Letter | Date or Time Component                      | Presentation       | Examples                              |
| ------ | ------------------------------------------- | ------------------ | ------------------------------------- |
| G      | Era designator                              | Text               | AD                                    |
| y      | Year                                        | Year               | 1996; 96                              |
| Y      | Week year                                   | Year               | 2009; 09                              |
| M      | Month in year                               | Month              | July; Jul; 07                         |
| w      | Week in year                                | Number             | 27                                    |
| W      | Week in month                               | Number             | 2                                     |
| D      | Day in year                                 | Number             | 189                                   |
| d      | Day in month                                | Number             | 10                                    |
| F      | Day of week in month                        | Number             | 2                                     |
| E      | Day name in week                            | Text               | Tuesday; Tue                          |
| u      | Day number of week (1=Monday,..., 7=Sunday) | Number             | 1                                     |
| a      | AM/PM marker                                | Text               | PM                                    |
| H      | Hour in day (0-23)                          | Number             | 0                                     |
| k      | Hour in day (1-24)                          | Number             | 24                                    |
| K      | Hour in am/pm (0-11)                        | Number             | 0                                     |
| h      | Hour in am/pm (1-12)                        | Number             | 12                                    |
| m      | Minute in hour                              | Number             | 30                                    |
| s      | Second in minute                            | Number             | 55                                    |
| S      | Millisecond                                 | Number             | 978                                   |
| z      | Time zone                                   | General Time Zone  | Pacific Standard Time; PST; GMT-08:00 |
| Z      | Time zone                                   | RFC 822 Time Zone  | -0800                                 |
| X      | Time zone                                   | ISO 8601 Time Zone | -08; -0800; -08:00                    |


## Examples

Prior to using these patterns, the directive [PARSE-AS-DATE](parse-as-date.md) should be applied
to correctly create a complete date-time string.

These examples show how date and time patterns are interpreted in the U.S. locale. If the
given date and time is `2001-07-04 12:08:56`, and is a local time in the U.S. Pacific Time
Zone, then applying different patterns results in these strings:

| Date and Time Pattern          | Date String                          |
| ------------------------------ | ------------------------------------ |
| `yyyy.MM.dd G 'at' HH:mm:ss z` | 2001.07.04 AD at 12:08:56 PDT        |
| `EEE, MMM d, ''yy`             | Wed, Jul 4, '01                      |
| `h:mm a`                       | 12:08 PM                             |
| `hh 'o''clock' a, zzzz`        | 12 o'clock PM, Pacific Daylight Time |
| `K:mm a, z`                    | 0:08 PM, PDT                         |
| `yyyy.MMMMM.dd GGG hh:mm aaa`  | 2001.July.04 AD 12:08 PM             |
| `EEE, d MMM yyyy HH:mm:ss Z`   | Wed, 4 Jul 2001 12:08:56 -0700       |
| `yyMMddHHmmssZ`                | 010704120856-0700                    |
| `yyyy-MM-dd'T'HH:mm:ss.SSSZ`   | 2001-07-04T12:08:56.235-0700         |
| `yyyy-MM-dd'T'HH:mm:ss.SSSXXX` | 2001-07-04T12:08:56.235-07:00        |
| `MM/dd/yyyy HH:mm`             | 07/04/2001 12:09                     |
| `yyyy.MM.dd`                   | 2001-07-04                           |
