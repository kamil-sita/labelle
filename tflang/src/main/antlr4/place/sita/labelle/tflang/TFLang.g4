grammar TFLang;

parseMatchExpression
    : matchExpression EOF
    ;

matchExpression
    : matchExpression binaryOp matchExpression
    | unaryOp matchExpression
    | '(' matchExpression ')'
    | singleMatchExpression
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
singleMatchExpression
    :   singleMatchExpressionRaw
    ;

singleMatchExpressionRaw
    : NAME '=' StringLiteral #eqComparison
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