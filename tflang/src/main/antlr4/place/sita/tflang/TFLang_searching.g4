grammar TFLang_searching;
import TFLang_lexing;

parseMatchExpression
    : matchExpression EOF
    ;

matchExpression
    : matchExpression binaryOp matchExpression #binaryOpMatchExpression
    | unaryOp matchExpression #unaryOpMatchExpression
    | '(' matchExpression ')' #parenthesesMatchExpression
    | singleMatchExpression #singleMatchMatchExpression
    | anyExpresion #anyMatchExpression
    | matchInnerExpression #innerMatchExpression
    ;

matchInnerExpression
    : 'IN' NAME 'EXISTS' '(' matchExpression ')'
    ;

anyExpresion
    : 'ANY' #any
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
    | NAME 'in' stringList #inComparison
    | nameTuple '=' valueTuple #tupleEqComparison
    | nameTuple 'in' '(' valueTuple (',' valueTuple)* ')' #inEqComparison
    ;

nameTuple
    : '(' NAME ( ',' NAME )* ')'
    ;

valueTuple
    : stringList
    ;

stringList
    : '(' StringLiteral ( ',' StringLiteral )* ')'
    ;
