// Upper case = terminal symbol or lexer rule
// Lower case = nonterminal symbol or parser rule
// All capitals = simple token / symbol / keyword
grammar FXSL;

// ** Parser rules **

typeKeyValue : Identifier COLON typeRef;
exprKeyValue : Identifier COLON expr;
optTypeKeyValue : Identifier
                | typeKeyValue;

// Type declarations and references
// FIXME add wildcard types

typeRef : baseTypeRef DECLARE typeRef
        | LPAREN baseTypeRef (COMMA typeRef)* RPAREN DECLARE typeRef
        | baseTypeRef LBRACK Integer RBRACK
        | baseTypeRef;

baseTypeRef : LBRACE typeKeyValue (COMMA typeKeyValue)* RBRACE
            | Identifier
            | LPAREN typeRef RPAREN;

typeConstr : Boolean
           | Integer
           | Float
           | LBRACK expr (COMMA expr)* RBRACK
           | LBRACE exprKeyValue (COMMA exprKeyValue)* RBRACE
           | optTypeKeyValue DECLARE expr
           | LPAREN optTypeKeyValue (COMMA optTypeKeyValue)* RPAREN DECLARE expr;

// Definitions

baseDef : optTypeKeyValue ASSIGN expr;
typeDef : TYPE Identifier ASSIGN typeRef;
varDef : DEF baseDef;

// Expressions

binaryOpLeft : GT
             | LT
             | GE
             | LE
             | EQUAL
             | NOTEQUAL
             | AND
             | OR
             | BITAND
             | BITOR
             | TILDE // FIXME does glsl support bit ops and if so, do they include bit inverse?
             | ADD
             | SUB
             | MUL
             | DIV
             | MOD;
expr : expr binaryOpLeft expr
     | <assoc=right>expr CARET expr
     | BANG expr
     | SUB expr
     | expr LPAREN expr (COMMA expr)* RPAREN // functions always have at least one parameter
     | expr LBRACK expr RBRACK
     | LET declList IN expr
     | typeConstr
     | Identifier
     | LPAREN expr RPAREN;

declList : baseDef (SEMI baseDef)*;

// Shader unit

stm : expr
    | typeDef
    | varDef;
stmList : (stm SEMI)+;

// ** Lexer Rules **

// Symbols

LPAREN          : '(';
RPAREN          : ')';
LBRACE          : '{';
RBRACE          : '}';
LBRACK          : '[';
RBRACK          : ']';
SEMI            : ';';
COMMA           : ',';
DOT             : '.';

ASSIGN          : '=';
GT              : '>';
LT              : '<';
BANG            : '!';
TILDE           : '~';
QUESTION        : '?';
COLON           : ':';
EQUAL           : '==';
LE              : '<=';
GE              : '>=';
NOTEQUAL        : '!=';
AND             : '&&';
OR              : '||';
ADD             : '+';
SUB             : '-';
MUL             : '*';
DIV             : '/';
BITAND          : '&';
BITOR           : '|';
CARET           : '^';
MOD             : '%';
DECLARE         : '->';

// Keywords

TYPE : 'type';
LET : 'let';
IN : 'in';
DEF : 'def';

// Primitive types

Boolean :   'true'
        |   'false';

fragment Digit : [0-9];

fragment Digits : Digit+;

fragment UnsignedSuffix : [uU];

Integer : Digits UnsignedSuffix?;

fragment ExponentSign : ADD | SUB;

fragment Exponent : [eE] ExponentSign? Digits;

Float : Digits DOT Digits? Exponent?
      | DOT Digits Exponent?
      | Digits Exponent?;

// Identifiers (must appear after all keywords in the grammar, since ANTLR uses longest-first matching)

Identifier : Letter LetterOrDigit*;

fragment Letter : [a-zA-Z$_];

fragment LetterOrDigit : Letter
                       | Digit;

// Whitespace and comments

WS : [ \t\r\n\u000C]+ -> skip;

COMMENT : '/*' .*? '*/' -> skip;

LINE_COMMENT : '//' ~[\r\n]* -> skip;
