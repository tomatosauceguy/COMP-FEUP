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
    : (importDeclaration)* classDeclaration EOF  #ProgramDec
    ;

importDeclaration
    : 'import' ID( '.' ID )* ';'  #ImportDec
    ;

classDeclaration
    : 'class' ID ( 'extends' ID )? '{' ( varDeclaration )* ( methodDeclaration )*'}'  #ClassDec
    ;

varDeclaration
    : type ';'  #VarDec
    ;

methodDeclaration
    : ('public')? type'(' ( type ( ',' type )* )? ')' '{' ( varDeclaration )* ( statement )* 'return' expression ';' '}'  #FunctionDeclaration
    | ('public')? 'static' 'void' 'main' '(' 'String' '[' ']' ID ')' '{' ( varDeclaration )* ( statement )* '}'  #MainDeclaration
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
    | '!' expression  #NotExpression
    | expression ('&&' | '>' | '||' | '<') expression  #BinaryOp
    | expression ('*' | '/') expression  #MultDivOp
    | expression ('+' | '-') expression  #BinaryOp
    | expression '[' expression ']' #ArrayAcessOp
    | expression '.' 'length'  #ArrayLengthOp
    | expression '.' ID '(' ( expression ( ',' expression )* )? ')'  #MethodCallOp
    | 'new' 'int' '[' expression ']'  #NewIntArrayOp
    | 'new' ID '(' ')'  #NewObjectOp
    | INT  #IntLiteral
    | 'true'  #TrueLiteral
    | 'false'  #FalseLiteral
    | ID  #IdOp
    | 'this'  #ThisOp
    ;