grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

INT
    : '0'
    |[1-9][0-9]*
    ;

ID : [a-zA-Z_$][a-zA-Z_0-9$]* ;

WS : [ \t\n\r\f]+ -> skip ;

program
    : (importDeclaration)* classDeclaration EOF
    ;

importDeclaration
    : 'import' ID( '.' ID )* ';'
    ;

classDeclaration
    : 'class' ID ( 'extends' ID )? '{' ( varDeclaration )* ( methodDeclaration )*'}'
    ;

varDeclaration
    : type ID ';'
    ;

methodDeclaration
    : ('public')? type ID '(' ( type ID ( ',' type ID )* )? ')' '{' ( varDeclaration )* ( statement )* 'return' expression ';' '}'
    | ('public')? 'static' 'void' 'main' '(' 'String' '[' ']' ID ')' '{' ( varDeclaration )* ( statement )* '}'
    ;

type
    : 'int' '[' ']'  #IntArray
    | 'boolean'  #Boolean
    | 'int'  #Int
    | 'String'  #String
    | ID  #Class
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
    : '!' expression  #NotExpression
    | expression ('*' | '/') expression  #MultDivOp
    | expression ('&&' | '>' | '||' | '<' | '+' | '-') expression  #BinaryOp
    | expression '[' expression ']' #ArrayAcessOp
    | expression '.' 'length'  #ArrayLengthOp
    | expression '.' ID '(' ( expression ( ',' expression )* )? ')'  #MethodCallOp
    | 'new' 'int' '[' expression ']'  #NewIntArrayOp
    | 'new' ID '(' ')'  #NewObjectOp
    | '(' expression ')'  #ParenOp
    | INT  #IntLiteral
    | 'true'  #TrueLiteral
    | 'false'  #FalseLiteral
    | ID  #IdOp
    | 'this'  #ThisOp
    ;