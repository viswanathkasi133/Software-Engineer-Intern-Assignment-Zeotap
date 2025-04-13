grammar Directives;

import Common;

// Assuming Common.g4 defines basic tokens like COLON, COMMA, etc.
// Add or import other necessary grammar dependencies.

// Define entry point, usually 'recipe' or 'directives'
program : recipe EOF ;

recipe : (directive (EOL|EOF))+ ;

// ... Other existing rules ...

// --- Lexer Rules and Fragments ---
fragment DIGIT : [0-9] ;
fragment NZ_DIGIT : [1-9] ;
fragment DOT : '.' ;
// Adjust NUMBER regex if needed for more complex formats (e.g., scientific notation)
fragment NUMBER : (NZ_DIGIT DIGIT* | '0') (DOT DIGIT+)? ;

fragment BYTE_UNIT : K | M | G | T | P ;
fragment TIME_UNIT : NANO | MICRO | MILLI | SECOND | MINUTE | HOUR | DAY ;

fragment K : ('k'|'K')('b'|'B')? ;
fragment M : ('m'|'M')('b'|'B')? ;
fragment G : ('g'|'G')('b'|'B')? ;
fragment T : ('t'|'T')('b'|'B')? ;
fragment P : ('p'|'P')('b'|'B')? ;

fragment NANO : ('n'|'N')('s'|'S') ;
fragment MICRO : ('u'|'U')('s'|'S') ;
fragment MILLI : ('m'|'M')('s'|'S') ;
fragment SECOND : ('s'|'S') ;
fragment MINUTE : ('m'|'M')('i'|'I')?('n'|'N')? ;
fragment HOUR : ('h'|'H')('r'|'R')? ;
fragment DAY : ('d'|'D') ;

BYTE_SIZE : NUMBER BYTE_UNIT ;
TIME_DURATION : NUMBER TIME_UNIT ;

// --- Assume other basic tokens are defined (e.g., in Common.g4 or here) ---
COLUMN: ID; // Example definition, adjust based on actual grammar
TEXT: ~[\r\n,:]+; // Example definition, adjust based on actual grammar
QUOTED_STRING : '"' (~["\r\n] | '""')* '"' | '\'' (~['\r\n] | '\'\'')* '\'' ; // Example
PROPERTY: ID EQ ( TEXT | NUMBER | QUOTED_STRING ); // Example for key=value properties
ID : [a-zA-Z_] [a-zA-Z0-9_]* ; // Example identifier
EQ : '=' ;
COLON: ':' ;
COMMA: ',' ;
EOL : '\r'? '\n' | '\r' ;
WS : [ \t]+ -> skip ;


// --- Parser Rules ---

// Option 1: Add to existing 'value' rule (Check if appropriate)
// value
//    : TEXT
//    | NUMBER
//    | QUOTED_STRING
//    | boolean_literal // If exists
//    | BYTE_SIZE
//    | TIME_DURATION
//    ;

// Option 2: Define specific argument types
byteSizeArg : BYTE_SIZE ;
timeDurationArg : TIME_DURATION ;

// Modify/define rules needing these arguments
// Example: hypothetical set-size directive
// setSizeDirective : 'set-size' COLON columnName byteSizeArg ;

// Define your new directive's rule structure
aggregateStatsDirective : 'aggregate-stats' COLON
    column // source byte size col
    COMMA column // source time duration col
    COMMA column // target total size col
    COMMA column // target total/avg time col
    (COMMA property)* // Optional arguments like output units/aggregation type
    ;

// Define how columns and properties are matched (examples)
column : COLUMN | QUOTED_STRING | TEXT ; // Adjust based on how columns are identified
property : PROPERTY ; // Use the PROPERTY token defined above


// Add your new directive to the main 'directive' rule
directive :
    // list existing directives here...
    // parseCsvDirective | // Example
    // setColumnDirective | // Example
    aggregateStatsDirective
    ;







/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

grammar Directives;

options {
  language = Java;
}

@lexer::header {
/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
}

/**
 * Parser Grammar for recognizing tokens and constructs of the directives language.
 */
recipe
 : statements EOF
 ;

statements
 :  ( Comment | macro | directive ';' | pragma ';' | ifStatement)*
 ;

directive
 : command
  (   codeblock
    | identifier
    | macro
    | text
    | number
    | bool
    | column
    | colList
    | numberList
    | boolList
    | stringList
    | numberRanges
    | properties
  )*?
  ;

ifStatement
  : ifStat elseIfStat* elseStat? '}'
  ;

ifStat
  : 'if' expression '{' statements
  ;

elseIfStat
  : '}' 'else' 'if' expression '{' statements
  ;

elseStat
  : '}' 'else' '{' statements
  ;

expression
  : '(' (~'(' | expression)* ')'
  ;

forStatement
 : 'for' '(' Identifier '=' expression ';' expression ';' expression ')' '{'  statements '}'
 ;

macro
 : Dollar OBrace (~OBrace | macro | Macro)*? CBrace
 ;

pragma
 : '#pragma' (pragmaLoadDirective | pragmaVersion)
 ;

pragmaLoadDirective
 : 'load-directives' identifierList
 ;

pragmaVersion
 : 'version' Number
 ;

codeblock
 : 'exp' Space* ':' condition
 ;

identifier
 : Identifier
 ;

properties
 : 'prop' ':' OBrace (propertyList)+  CBrace
 | 'prop' ':' OBrace OBrace (propertyList)+ CBrace { notifyErrorListeners("Too many start paranthesis"); }
 | 'prop' ':' OBrace (propertyList)+ CBrace CBrace { notifyErrorListeners("Too many start paranthesis"); }
 | 'prop' ':' (propertyList)+ CBrace { notifyErrorListeners("Missing opening brace"); }
 | 'prop' ':' OBrace (propertyList)+  { notifyErrorListeners("Missing closing brace"); }
 ;

propertyList
 : property (',' property)*
 ;

property
 : Identifier '=' ( text | number | bool )
 ;

numberRanges
 : numberRange ( ',' numberRange)*
 ;

numberRange
 : Number ':' Number '=' value
 ;

value
 : String | Number | Column | Bool
 ;

ecommand
 : '!' Identifier
 ;

config
 : Identifier
 ;

column
 : Column
 ;

text
 : String
 ;

number
 : Number
 ;

bool
 : Bool
 ;

condition
 : OBrace (~CBrace | condition)* CBrace
 ;

command
 : Identifier
 ;

colList
 : Column (','  Column)+
 ;

numberList
 : Number (',' Number)+
 ;

boolList
 : Bool (',' Bool)+
 ;

stringList
 : String (',' String)+
 ;

identifierList
 : Identifier (',' Identifier)*
 ;


/*
 * Following are the Lexer Rules used for tokenizing the recipe.
 */
OBrace   : '{';
CBrace   : '}';
SColon   : ';';
Or       : '||';
And      : '&&';
Equals   : '==';
NEquals  : '!=';
GTEquals : '>=';
LTEquals : '<=';
Match    : '=~';
NotMatch : '!~';
QuestionColon : '?:';
StartsWith : '=^';
NotStartsWith : '!^';
EndsWith : '=$';
NotEndsWith : '!$';
PlusEqual : '+=';
SubEqual : '-=';
MulEqual : '*=';
DivEqual : '/=';
PerEqual : '%=';
AndEqual : '&=';
OrEqual  : '|=';
XOREqual : '^=';
Pow      : '^';
External : '!';
GT       : '>';
LT       : '<';
Add      : '+';
Subtract : '-';
Multiply : '*';
Divide   : '/';
Modulus  : '%';
OBracket : '[';
CBracket : ']';
OParen   : '(';
CParen   : ')';
Assign   : '=';
Comma    : ',';
QMark    : '?';
Colon    : ':';
Dot      : '.';
At       : '@';
Pipe     : '|';
BackSlash: '\\';
Dollar   : '$';
Tilde    : '~';


Bool
 : 'true'
 | 'false'
 ;

Number
 : Int ('.' Digit*)?
 ;

Identifier
 : [a-zA-Z_\-] [a-zA-Z_0-9\-]*
 ;

Macro
 : [a-zA-Z_] [a-zA-Z_0-9]*
 ;

Column
 : ':' [a-zA-Z_\-] [:a-zA-Z_0-9\-]*
 ;

String
 : '\'' ( EscapeSequence | ~('\'') )* '\''
 | '"'  ( EscapeSequence | ~('"') )* '"'
 ;

EscapeSequence
   :   '\\' ('b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\')
   |   UnicodeEscape
   |   OctalEscape
   ;

fragment
OctalEscape
   :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
   |   '\\' ('0'..'7') ('0'..'7')
   |   '\\' ('0'..'7')
   ;

fragment
UnicodeEscape
   :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
   ;

fragment
   HexDigit : ('0'..'9'|'a'..'f'|'A'..'F') ;

Comment
 : ('//' ~[\r\n]* | '/*' .*? '*/' | '--' ~[\r\n]* ) -> skip
 ;

Space
 : [ \t\r\n\u000C]+ -> skip
 ;

fragment Int
 : '-'? [1-9] Digit* [L]*
 | '0'
 ;

fragment Digit
 : [0-9]
 ;
