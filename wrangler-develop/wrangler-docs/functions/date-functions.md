# Date Manipulation Functions

These are functions for detecting the type of data. These functions can be used in the
directives `filter-row-if-false`, `filter-row-if-true`, `filter-row-on`, or
`send-to-error`.


## Pre-requisite

These can be used only in the `filter-*` or `send-to-error` directives.


## Namespace

All type-related functions are in the namespace `type`.


## Example Data

Upload to the workspace `body` an input record such as:

```
{
    "name": {
        "fname": "Joltie",
        "lname": "Root",
        "mname": null
    },
    "date": "12/17/2019",
    "time": "10:45 PM",
    "boolean": "true",
    "coordinates": [
        12.56,
        45.789
    ],
    "numbers": [
        1,
        2.1,
        3,
        null,
        4,
        5,
        6,
        null
    ],
    "moves": [
        { "a": 1, "b": "X", "c": 2.8},
        { "a": 2, "b": "Y", "c": 232342.8},
        { "a": 3, "b": "Z", "c": null},
        { "a": 4, "b": "U"}
    ],
    "integer": "1",
    "double": "2.8",
    "empty": "",
    "float": 45.6,
    "aliases": [
        "root",
        "joltie",
        "bunny",
        null
    ]
}
```

Once such a record is loaded, apply these directives before applying any of the functions
listed here:
```
  parse-as-json body
  columns-replace s/body_//g
```

## List of Type Functions

Each function returns `true` if the condition is met, `false` otherwise.

| Function            | Condition                                 | Example                                  |
| ------------------- | ----------------------------------------- | ---------------------------------------- |
| `isDate(string)`    | Tests if string value is a date field     | `filter-row-if-true type:isDate(date)`   |
| `isTime(string)`    | Tests if string value is a date time      | `filter-row-if-true type:isTime(time)`   |
| `isBoolean(string)` | Tests if string value is a booelan field  | `send-to-error !type:isBoolean(boolean)` |
| `isNumber(string)`  | Tests if string value is a number field   | `send-to-error !type:isNumber(integer)`  |
| `isEmpty(string)`   | Tests if string value is empty            | `send-to-error !type:isEmpty(empty)`     |
| `isDouble(string)`  | Tests if string value is a double field   | `send-to-error !type:isDouble(double)`   |
| `isInteger(string)` | Tests if string value is an integer field | `send-to-error !type:isInteger(integer)` |
