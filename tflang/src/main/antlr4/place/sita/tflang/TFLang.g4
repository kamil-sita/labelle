grammar TFLang;
import TFLang_searching, TFLang_modification;

parseConditionalChangeExpression
    : conditionalChangeExpression EOF
    ;

conditionalChangeExpression
    : 'IF' matchExpression 'THEN' changeManyExpression
    | changeManyExpression
    ;


parseConditionalChangeExpressionMany
    : conditionalChangeExpressionMany EOF
    ;

conditionalChangeExpressionMany
    : conditionalChangeExpression (';'+ conditionalChangeExpression)* (';')*
    ;
