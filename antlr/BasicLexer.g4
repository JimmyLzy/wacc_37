lexer grammar BasicLexer;

//binary operators
PLUS : '+' ;
MINUS : '-' ;
MULT : '*' ;
DIV : '/' ;
MOD : '%' ;
GREATER : '>' ;
GREATER_OR_EQUAL : '>=' ;
SMALLER : '<' ;
SMALLER_OR_EQUAL : '<=' ;
EQUAL : '==' ;
ASSIGN_EQUAL : '=' ;
NOT_EQUAL : '!=' ;
LOGICAL_AND : '&&' ;
LOGICAL_OR : '||' ;

//unary operator
LOGICAL_NOT : '!' ;
NEGATE : '-' ;
LEN : 'len' ;
ORD : 'ord' ;
CHR : 'chr' ;

//unnamed keywords(from stat)
SKIP : 'skip' ;
READ : 'read' ;
FREE : 'free' ;
RETURN : 'return' ;
EXIT : 'exit' ;
PRINT : 'print' ;
PRINTLN : 'println' ;
IF : 'if' ;
THEN : 'then' ;
ELSE : 'else' ;
FI : 'fi' ;
WHILE : 'while' ;
DO : 'do' ;
DONE : 'done' ;
BEGIN : 'begin' ;
END : 'end' ;
IS : 'is' ;

CALL : 'call' ;
NEWPAIR : 'newpair' ;
PAIR : 'pair' ;

FST : 'fst' ;
SND : 'snd' ;

//escaped char

//might be wrong
NULL_TERMINATOR : '0' ;
BACKSPACE : 'b' ;
HORIZONTAL_TAB : 't' ;
LINE_FEED : 'n' ;
FORM_FEED : 'f' ;
CARRIAGE_RETURN : 'r' ;
SINGLE_QUOTE : '\'' ;
DOUBLE_QUOTE : '\"' ;
BACKSLASH : '\\' ;

//unknown
UNDERSCORE : '_' ;
NULL: 'null' ;
SHARP: '#' ;
TRUE: 'true' ;
FALSE: 'false' ;
//wrong
EOL: LINE_FEED ;

//types
INT : 'int' ;
BOOL : 'bool' ;
CHAR : 'char' ;
STRING : 'string' ;

//brackets
OPEN_PARENTHESES : '(' ;
CLOSE_PARENTHESES : ')' ;
OPEN_SQUARE_BRACKET : '[' ;
CLOSE_SQUARE_BRACKET : ']' ;

//separators
COMMA : ',';
SEMICOLON : ';';

//numbers
//removed fragment
DIGIT : '0'..'9' ;

INTEGER: DIGIT+ ;

//letters
LOWER_CASE_ALPHABET : 'a'..'z' ;
UPPER_CASE_ALPHABET : 'A'..'Z' ;

COMMENT: SHARP ~('\n')* '\n' -> skip;
WS: (' ' | '\t') -> skip;
ESCAPED_CHAR: BACKSLASH (HORIZONTAL_TAB | LINE_FEED | CARRIAGE_RETURN) -> skip;

//NULL_TERMINATOR : '\\0';
//BACKSPACE : '\b' ;
//HORIZONTAL_TAB : '\t' -> skip ;
//LINE_FEED : '\n' -> skip;
//FORM_FEED : '\f' ;
//CARRIAGE_RETURN : '\r' -> skip;
//SINGLE_QUOTE : '\'' ;
//DOUBLE_QUOTE : '\"' ;

//KEYWORD: PLUS
//| MINUS
//|

STR_LITER : DOUBLE_QUOTE (CHARACTER)* DOUBLE_QUOTE ;
CHARACTER: ~(BACKSLASH | SINGLE_QUOTE | DOUBLE_QUOTE)
| BACKSLASH escaped_char;