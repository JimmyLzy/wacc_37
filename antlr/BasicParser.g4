parser grammar BasicParser;

options {
  tokenVocab=BasicLexer;
}

binaryOper : PLUS | MINUS | MULT | DIV | MOD | GREATER | GREATEROREQUAL | SMALLER |
             SMALLEROREQUAL | EQUAL | NOTEQUAL | LOGICALAND | LOGICALOR;

expr: expr binaryOper expr
| INTEGER
| OPEN_PARENTHESES expr CLOSE_PARENTHESES
;

program: BEGIN (func)* stat END;

func: type ident OPEN_PARENTHESES (param-list)? CLOSE_PARENTHESES IS stat END;

// EOF indicates that the program must consume to the end of the input.
prog: (expr)*  EOF ;
