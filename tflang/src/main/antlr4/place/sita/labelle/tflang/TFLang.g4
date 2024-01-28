grammar TFLang;

parse
    : expression EOF
    ;

expression
    : expression binaryOp expression
    | unaryOp expression
    | '(' expression ')'
    | singleExpression
    ;

binaryOp
    : 'AND' #and
    | 'OR' #or
    ;

unaryOp
    : 'NOT'
    ;

// code to OddBuild mentions a workaround needed if labels used - I'm not sure if that's the case, but let's not
// risk it yet. Can be fixed later TODO
singleExpression
    :   singleExpressionRaw
    ;

singleExpressionRaw
    : NAME '=' StringLiteral #eqComparison
    | NAME 'startsWith' StringLiteral #startsWithComparison
    | NAME 'endsWith' StringLiteral #endsWithComparison
    | NAME 'like' StringLiteral #likeComparison
    | NAME 'in' '(' StringLiteral (',' StringLiteral)* ')' #inComparison
    | nameTuple '=' valueTuple #tupleEqComparison
    | nameTuple 'in' '(' valueTuple (',' valueTuple)* ')' #inEqComparison
    ;

nameTuple
    : '(' NAME ( ',' NAME )* ')'
    ;

valueTuple
    : '(' StringLiteral ( ',' StringLiteral )* ')'
    ;

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