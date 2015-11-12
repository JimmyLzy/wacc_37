lexer grammar BasicLexer;


COMMENT: SHARP ~('\n')* '\n' -> skip;

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

//unnamed keywords(from stat)

CALL : 'call' ;
NEWPAIR : 'newpair' ;
PAIR : 'pair' ;

FST : 'fst' ;
SND : 'snd' ;

//binary operators
LOGICAL_AND : '&&' ;
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
LOGICAL_OR : '||' ;

//unary operator
LOGICAL_NOT : '!' ;
NEGATE : '-' ;
LEN : 'len' ;
ORD : 'ord' ;
CHR : 'chr' ;

//types
INT : 'int' ;
BOOL : 'bool' ;
CHAR : 'char' ;
STRING : 'string' ;

//unknown
UNDERSCORE : '_' ;
NULL: 'null' ;
SHARP: '#' ;
TRUE: 'true' ;
FALSE: 'false' ;


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


DOUBLE_QUOTE : '"' ;

//OPENSTRING:  DOUBLE_QUOTE -> more, pushMode(STRINGMODE) ;
WS: (' ' | '\t' | '\r' | '\n') -> skip;
NEW_LINE: '\n' -> skip;

//mode STRINGMODE;
//SPECIAL: '\\"' -> more;
//TEXT: CHARACTER -> more;
//ESCAPE: ('\t' | '\r' | '\n') -> popMode;
//CLOSESTRING: DOUBLE_QUOTE -> popMode ;



STR_LITER: '"' (CHARACTER)* '"';

//ESCAPED_CHAR: NULL_TERMINATOR | BACKSPACE | FORM_FEED | SINGLE_QUOTE | DOUBLE_QUOTE
 //            | HORIZONTAL_TAB | LINE_FEED | CARRIAGE_RETURN | '\\';

/*NULL_TERMINATOR : '\\0';
BACKSPACE : '\b' ;
HORIZONTAL_TAB : '\t'  ;
LINE_FEED : '\n' ;
FORM_FEED : '\f' ;
CARRIAGE_RETURN : '\r' ;
SINGLE_QUOTE : '\'' ;*/



//KEYWORD: PLUS
//| MINUS
//|

//STR_LITER : DOUBLE_QUOTE (CHARACTER)* DOUBLE_QUOTE ;
//CHARACTER: ~('\\' | '\'' | '\"')
//| BACKSLASH ESCAPED_CHAR;

CHAR_LITER: '\'' CHARACTER '\'' ;

CHARACTER: ~('\\' | '\'' | '"')
| '\\' ESCAPED_CHAR;

ESCAPED_CHAR : '0'
| 'b'
| 't'
| 'n'
| 'f'
| 'r'
| '"'
| '\''
| '\\' ;