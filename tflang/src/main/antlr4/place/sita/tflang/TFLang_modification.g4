grammar TFLang_modification;
import TFLang_lexing;


changeManyExpression
    : changeExpression (',' changeExpression)*
    ;

changeExpression
    : addManyExpression
    | removeComplexExpression
    | transformManyExpression
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

transformManyExpression
    : transformManyExpressionCalculated
    | transformManyExpressionSpecial
    ;

transformManyExpressionCalculated
    : 'REPLACE' 'WITH' matchedOrStringTupleExpression (',' matchedOrStringTupleExpression)*
    | 'REPLACE' 'WITH' matchedOrStringTupleExpression (',' 'REPLACE' 'WITH' matchedOrStringTupleExpression)*
    ;

transformManyExpressionSpecial
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
