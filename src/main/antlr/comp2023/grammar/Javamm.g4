grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

INT
    : [0]
    |[1-9][0-9]*
    ;

ID : [a-zA-Z_$][a-zA-Z_$0-9]* ;

WS : [ \t\n\r\f]+ -> skip ;

COMMENT : '/*' .*? '*/' -> skip ;

LINE_COMMENT : '//' ~[\r\n]* -> skip ;

program
    : (importDeclaration)* classDeclaration EOF
    ;

importDeclaration
    : 'import' name += ID( '.' name += ID )* ';'
    ;

classDeclaration
    : 'class' name = ID ( 'extends' sName = ID )? '{' ( varDeclaration )* ( methodDeclaration )*'}'
    ;

varDeclaration
    : type ';'
    ;

methodDeclaration
    : ('public')? type '(' ( type ( ',' type )* )? ')' '{' ( varDeclaration )* ( statement )* 'return' expression ';' '}'  #RegularMethod
    | ('public')? 'static' 'void' name = 'main' '(' 'String' '[' ']' ID ')' '{' ( varDeclaration )* ( statement )* '}'  #MainMethod
    ;

type locals [boolean isArray = false]
    : name='int' ('['']'{$isArray = true;})? varname=ID
    | name='boolean' varname=ID
    | name='int' varname=ID
    | name='String' varname=ID
    | name=ID varname=ID
    ;

statement
    : '{' ( statement )* '}'  #BlockStat
    | 'if' '(' expression ')' statement 'else' statement #IfElseStat
    | 'while' '(' expression ')' statement  #WhileStat
    | expression ';'  #ExpressionStat
    | variable = ID '=' expression ';' #AssignmentStat
    | arrayVar = ID '[' expression ']' '=' expression ';'  #ArrayAssignmentStat
    ;

expression
    : '(' expression ')'  #ParenOp
    | expression '[' expression ']' #ArrayAcessOp
    | expression '.' 'length'  #ArrayLengthOp
    | expression '.' name = ID '(' ( expression ( ',' expression )* )? ')'  #MethodCallOp
    | '!' expression  #NotExpression
    | expression ('++' | '--') #CrementOp
    | expression ('*' | '/') expression  #BinaryOperator
    | expression ('+' | '-') expression  #BinaryOperator
    | expression ('<' | '<=' | '>' | '>=' ) expression  #RelationalExpression
    | expression ('==' | '!=') expression #RelationalExpression
    | expression '&&' expression #AndExpression
    | expression '||' expression #BinaryOp
    | 'new' 'int' '[' expression ']'  #NewIntArrayOp
    | 'new' name = ID '(' ')'  #NewObjectOp
    | val=INT  #IntLiteral
    | val='true'  #BooleanLiteral
    | val='false'  #BooleanLiteral
    | val=ID  #IdOp
    | val='this'  #ThisOp
    ;