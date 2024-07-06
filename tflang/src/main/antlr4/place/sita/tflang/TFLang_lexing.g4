lexer grammar TFLang_lexing;

NAME
    : [a-zA-Z_][a-zA-Z_0-9]*
    ;

SPACE
    : [ \t\r\n] -> skip
    ;

StringLiteral
	:	'"' StringCharacters? '"'
	;

fragment
StringCharacters
	:	StringCharacter+
	;
fragment
StringCharacter
	:	~["\\\r\n]
	|	EscapeSequence
	;

fragment
EscapeSequence
	:	'\\' [btnfr"'\\]
	;

Comment
  :  '//' ~( '\r' | '\n' )* -> skip
  ;
