lexer grammar BasicLexer;

//tokens{
//    OPENSTRING,
//    CLOSESTRING
//}

COMMENT: SHARP ~('\n')* '\n' -> skip;

SKIP : 'skip' ;
READ : 'read' ;
FREE: 'free' ;
RETURN: 'return' ;
EXIT: 'exit' ;
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

LEN : 'len' ;
ORD : 'ord' ;
CHR : 'chr' ;

//types
INT : 'int' ;
BOOL : 'bool' ;
CHAR : 'char' ;
STRING : 'string' ;

//unknown
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
fragment DIGIT : '0'..'9' ;

INTEGER: DIGIT+ ;

SINGLE_DIGIT: DIGIT ;

fragment SINGLE_IDENT: (UNDERSCORE | LOWER_CASE_ALPHABET | UPPER_CASE_ALPHABET | SINGLE_DIGIT);

IDENT:  (UNDERSCORE | LOWER_CASE_ALPHABET | UPPER_CASE_ALPHABET) SINGLE_IDENT*;

UNDERSCORE : '_' ;


//letters
LOWER_CASE_ALPHABET : 'a'..'z' ;
UPPER_CASE_ALPHABET : 'A'..'Z' ;


//DOUBLE_QUOTE : '"' -> pushMode(STRINGMODE) ;

WS: (' ' | '\t' | '\r' | '\n') -> skip;
NEW_LINE: '\n' -> skip;

CHAR_LITER: '\'' CHARACTER '\'' ;

CHARACTER: ~('\\' | '\'' | '\"')
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


STR_LITER: '"' (CHARACTER)* '"';
//mode STRINGMODE;



//SCHARACTER: ~('\\' | '\'' | '"')
//| '\\' ESCAPED_CHAR ;

//STR: SCHARACTER+ ;

//CLOSESTRING: '"' -> popMode ;
























//BACKSLASH: '\\' -> Mode(SLASHMODE);
//mode SLASHMODE;
//
//fragment SLASH: '\\' ;
//
//SINGLESLASH: SLASH -> skip;
//
//ESCAPEDCHAR : ESCAPED_CHAR -> popMode ;

