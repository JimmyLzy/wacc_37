parser grammar BasicParser;

options {
  tokenVocab=BasicLexer;
}

binary_oper : PLUS | MINUS | MULT | DIV | MOD | GREATER | GREATER_OR_EQUAL | SMALLER |
             SMALLER_OR_EQUAL | EQUAL | NOT_EQUAL | LOGICAL_AND | LOGICAL_OR;

//expr: expr binaryOper expr
//| INTEGER
//| OPEN_PARENTHESES expr CLOSE_PARENTHESES;

program: BEGIN (func)* stat END;

func: type ident OPEN_PARENTHESES (param_list)? CLOSE_PARENTHESES IS stat END;

param_list: param (COMMA param)*;

param: type ident;

stat: SKIP
| type ident EQUAL assign_rhs
| assign_lhs EQUAL assign_rhs
| READ assign_lhs
| FREE expr
| RETURN expr
| EXIT expr
| PRINT expr
| PRINTLN expr
| IF expr THEN expr ELSE expr FI
| WHILE expr DO expr DONE
| BEGIN expr END
| stat SEMICOLON stat;

assign_lhs: ident
| array_elem
| pair_elem;

assign_rhs: expr
| array_liter
| NEWPAIR OPEN_PARENTHESES expr COMMA expr CLOSE_PARENTHESES
| pair_elem
| CALL ident OPEN_PARENTHESES (arg_list)?  CLOSE_PARENTHESES;

arg_list: expr (COMMA expr)*;

pair_elem: FST expr
| SND expr;

type: base_type
| type OPEN_SQUARE_BRACKET CLOSE_SQUARE_BRACKET
| pair_type;

base_type: INT
| BOOL
| CHAR
| STRING;

array_type: type OPEN_SQUARE_BRACKET CLOSE_SQUARE_BRACKET;

pair_type: PAIR OPEN_PARENTHESES pair_elem_type COMMA pair_elem_type CLOSE_PARENTHESES;

pair_elem_type: base_type
| array_type
| PAIR;

expr: int_liter
| bool_liter
| char_liter
| str_liter
| pair_liter
| ident
| array_elem
| unary_oper expr
| expr binary_oper expr
| INTEGER
| OPEN_PARENTHESES expr CLOSE_PARENTHESES;

unary_oper: LOGICAL_NOT
| NEGATE
| LEN
| ORD
| CHR;

ident: (UNDERSCORE | LOWER_CASE_ALPHABET | UPPER_CASE_ALPHABET)
(UNDERSCORE | LOWER_CASE_ALPHABET | UPPER_CASE_ALPHABET | DIGIT)* ;

array_elem: ident (OPEN_SQUARE_BRACKET expr CLOSE_SQUARE_BRACKET)+ ;

int_liter: (int_sign)? (digit)+ ;

digit: DIGIT ;

int_sign: PLUS | MINUS ;

bool_liter: TRUE | FALSE ;

char_liter: SINGLE_QUOTE character SINGLE_QUOTE ;

str_liter: DOUBLE_QUOTE character DOUBLE_QUOTE ;

character: ~(BACKSLASH | SINGLE_QUOTE | DOUBLE_QUOTE)*
| BACKSLASH escaped_char;

escaped_char: NULL_TERMINATOR
| BACKSPACE
| HORIZONTAL_TAB
| LINE_FEED
| FORM_FEED
| CARRIAGE_RETURN
| SINGLE_QUOTE
| DOUBLE_QUOTE
| BACKSLASH;

array_liter: OPEN_SQUARE_BRACKET (expr (COMMA expr)*)? CLOSE_SQUARE_BRACKET ;

pair_liter: NULL ;

comment: SHARP ~(EOL)* EOL ;



// EOF indicates that the program must consume to the end of the input.
prog: (expr)*  EOF ;
