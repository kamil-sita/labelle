grammar TFLang_modification;
import TFLang_lexing;


changeManyExpressionParse
    : changeManyExpression EOF
    ;

changeManyExpression
    : changeExpression (',' changeExpression)*
    ;

changeExpression
    : addManyExpression
    | removeComplexExpression
    | transformExpression
    | changeInEntityExpression
    ;

changeInEntityExpression
    : 'IN' NAME 'DO' '(' changeManyExpression ')'
    ;

addManyExpression
    : addManyExpressionCalculated
    | addManyExpressionSpecial
    ;


addManyExpressionCalculated
    : 'ADD' matchedOrStringTupleExpression (',' matchedOrStringTupleExpression)*
    | 'ADD' matchedOrStringTupleExpression (',' 'ADD' matchedOrStringTupleExpression)*
    ;

addManyExpressionSpecial
    : 'ADD' 'USING' NAME
    ;

removeComplexExpression
    : justRemoveExpression
    | removeManyExpression
    ;

justRemoveExpression
    : 'REMOVE'
    ;

removeManyExpression
    : removeManyExpressionCalculated
    | removeManyExpressionSpecial
    ;

removeManyExpressionCalculated
    : 'REMOVE' matchedOrStringTupleExpression (',' matchedOrStringTupleExpression)*
    | 'REMOVE' matchedOrStringTupleExpression (',' 'REMOVE' matchedOrStringTupleExpression)*
    ;

removeManyExpressionSpecial
    : 'REMOVE' 'USING' NAME
    ;

transformExpression
    : transformExpressionCalculated
    | transformExpressionSpecial
    ;

transformExpressionCalculated
    : 'REPLACE' 'WITH' matchedOrStringTupleExpression
    ;

transformExpressionSpecial
    : 'REPLACE' 'USING' NAME
    ;


matchedOrStringTupleExpression
    : matchedOrStringTuple
    | matchedOrString
    ;

matchedOrStringTuple
    : '(' matchedOrString ( ',' matchedOrString )* ')'
    ;

matchedOrString
    : 'MATCHED' #copy
    | StringLiteral #string
    ;
