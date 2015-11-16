parser grammar BasicParser;

options {
  tokenVocab=BasicLexer;
}

binary_oper : PLUS | MINUS | MULT | DIV | MOD | GREATER | GREATER_OR_EQUAL | SMALLER |
             SMALLER_OR_EQUAL | EQUAL | NOT_EQUAL | LOGICAL_AND |LOGICAL_OR;

program: BEGIN (func)* stat END EOF;

func: type ident OPEN_PARENTHESES (param_list)? CLOSE_PARENTHESES IS stat END;

param_list: param (COMMA param)*;

param: type ident;

stat: SKIP
| type ident ASSIGN_EQUAL assign_rhs
| assign_lhs ASSIGN_EQUAL assign_rhs
| READ assign_lhs
| FREE expr
| RETURN expr
| EXIT expr
| PRINT expr
| PRINTLN expr
| IF expr THEN stat ELSE stat FI
| WHILE expr DO stat DONE
| BEGIN stat END
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

expr: unary_oper expr
| bool_liter
| char_liter
| str_liter
| pair_liter
| ident
| array_elem
| expr binary_oper expr
| int_liter
| OPEN_PARENTHESES expr CLOSE_PARENTHESES;

unary_oper: LOGICAL_NOT
| MINUS
| LEN
| ORD
| CHR;

ident: IDENT;

array_elem: ident (OPEN_SQUARE_BRACKET expr CLOSE_SQUARE_BRACKET)+ ;

digit: SINGLE_DIGIT ;

int_liter: (int_sign)? INTEGER ;

int_sign: PLUS | MINUS ;

bool_liter: TRUE | FALSE ;

char_liter: CHAR_LITER ;

str_liter: STR_LITER;
//str_liter: DOUBLE_QUOTE STR CLOSESTRING;

array_liter: OPEN_SQUARE_BRACKET (expr (COMMA expr)*)? CLOSE_SQUARE_BRACKET ;

pair_liter: NULL ;


//str_liter : DOUBLE_QUOTE (CHARACTER)* DOUBLE_QUOTE ;
//character: ~('\\' | '\'' | '\"')
//| BACKSLASH ESCAPED_CHAR;


