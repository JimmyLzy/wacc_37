lexer grammar BasicLexer;

//operators
PLUS: '+' ;
MINUS: '-' ;
MULT: '*' ;
DIV: '/' ;
MOD: '%' ;
GREATER: '>' ;
GREATEROREQUAL: '>=' ;
SMALLER: '<' ;
SMALLEROREQUAL: '<=' ;
EQUAL : '==' ;
NOTEQUAL: '!=' ;
LOGICALAND: '&&' ;
LOGICALOR: '||' ;

//stat
SKIP: 'skip' ;
READ: 'read' ;
FREE: 'free' ;
RETURN: 'return' ;
EXIT: 'exit' ;
PRINT: 'print' ;
PRINTLN: 'println' ;
IF: 'if' ;
THEN: 'then' ;
ELSE: 'else' ;
FI: 'fi' ;
WHILE: 'while' ;
DO: 'do' ;
DONE: 'done' ;
BEGIN: 'begin' ;
END: 'end' ;
IS: 'is' ;

//brackets
OPEN_PARENTHESES : '(' ;
CLOSE_PARENTHESES : ')' ;

//numbers
fragment DIGIT : '0'..'9' ; 

INTEGER: DIGIT+ ;





