grammar Lang;

program
    : importDeclaration* externalDeclaration* EOF;

importDeclaration
    : 'import' ID ('.' ID)*;

externalDeclaration
    : typeDeclaration
    | functionDeclaration
    ;

typeDeclaration
    : 'type' ID '=' (sumType | productType)
    ;

sumType
    : ID ID ('|' ID ID)*
    ;

productType
    : ID ':' ID (',' ID ':' ID)*
    ;

// Functions

functionDeclaration
    : functionName '(' argumentList ')' typeAnnotation?
        statementList ',' expression '.'
    ;

functionName: ID | '(' OPERATOR ')';

argumentList: variableSignature (',' variableSignature)*|;

typeAnnotation: ':' ID;
variableSignature: ID typeAnnotation?;

statementList
    :
    | statement (',' statementList)?
    | blockStatement (';' statementList)?
    ;

statement
    : 'return' expression
    | 'let' ID '=' expression
    ;

blockStatement
    : 'if' expression statementList
    ;

expression
    : expression OPERATOR expression
    | '(' expression ')'
    | ID
    | INT
    | 'true' | 'false'
    ;

// Lexing

ID: LETTER (LETTER | DIGIT)*;
INT: DIGIT+;
OPERATOR: SYMBOL+;

fragment SYMBOL:
    '-' | '+' | '*' | '/' | '.' | '>' | '<'
    '=' | '~' | '!' | '@' | '$' | '%' | '^'
    '&' | '|' | '`';

fragment DIGIT: '0'..'9';
fragment LETTER: [a-zA-Z];

WS: [ \r\t\n]+ -> skip;
