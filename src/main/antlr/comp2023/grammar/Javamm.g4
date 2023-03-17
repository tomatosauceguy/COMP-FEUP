grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

INT
    : '0'
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
    : ('public')? type'(' ( type ( ',' type )* )? ')' '{' ( varDeclaration )* ( statement )* 'return' expression ';' '}'  #MethodName
    | ('public')? 'static' 'void' 'main' '(' 'String' '[' ']' ID ')' '{' ( varDeclaration )* ( statement )* '}'  #MethodName
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
    | ID '=' expression ';' #AssignmentStat
    | ID '[' expression ']' '=' expression ';'  #ArrayAssigmentStat
    ;

expression
    : '(' expression ')'  #ParenOp
    | expression '[' expression ']' #ArrayAcessOp
    | expression '.' 'length'  #ArrayLengthOp
    | expression '.' ID '(' ( expression ( ',' expression )* )? ')'  #MethodCallOp
    | '!' expression  #NotExpression
    | expression ('*' | '/') expression  #MultDivOp
    | expression ('+' | '-') expression  #BinaryOp
    | expression '<' expression  #BinaryOp
    | expression '&&' expression #BinaryOp
    | 'new' 'int' '[' expression ']'  #NewIntArrayOp
    | 'new' ID '(' ')'  #NewObjectOp
    | INT  #IntLiteral
    | 'true'  #TrueLiteral
    | 'false'  #FalseLiteral
    | ID  #IdOp
    | 'this'  #ThisOp
    ;