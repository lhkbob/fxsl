// Upper case = terminal symbol or lexer rule
// Lower case = nonterminal symbol or parser rule
// All capitals = simple token / symbol / keyword
grammar FXSL;

// ** Parser rules **

typeKeyValue : Identifier COLON type;
exprKeyValue : Identifier COLON expr;
optTypeKeyValue : Identifier
                | typeKeyValue;
arrayLength : Integer
            | Identifier;

type : LPAREN params+=type (COMMA params+=type)* DECLARE returnType=type RPAREN #FunctionType
     | type LBRACK arrayLength RBRACK #ArrayType
     | LBRACE typeKeyValue (COMMA typeKeyValue)* RBRACE #StructType
     | type (BITOR type)+ #UnionType // can only hold functional types to represent ad-hoc polymorphism
     | Identifier #AliasType
     | LPAREN type RPAREN #GroupedType;

ctor : LPAREN params+=optTypeKeyValue (COMMA params+=optTypeKeyValue)* DECLARE returnExpr=expr RPAREN #Function
     | LBRACK expr (COMMA expr)* RBRACK #Array
     | LBRACK length=expr COLON func=expr RBRACK #DynamicArray
     | LBRACE exprKeyValue (COMMA exprKeyValue)* RBRACE #Struct
     | UNIFORM LPAREN optTypeKeyValue RPAREN #Uniform
     | ATTR LPAREN optTypeKeyValue RPAREN #Attr
     | (Boolean | Integer | Float) #Primitive;

// Definitions

def : TYPE Identifier ASSIGN type #TypeDef
    | VAR optTypeKeyValue ASSIGN expr #VarDef;

// Expressions

expr : LET def (SEMI def)* IN expr #Let
     | func=expr LPAREN params+=expr (COMMA params+=expr)* RPAREN #FunctionCall
     | value=expr LBRACE field=Identifier RBRACE #FieldAccess
     | value=expr LBRACK index=expr RBRACK #ArrayAccess
     | <assoc=right>left=expr op=POW right=expr #BinaryExpression
     | op=(BANG|SUB|TILDE) expr #UnaryExpression
     | left=expr op=(MUL|DIV|MOD) right=expr #BinaryExpression
     | left=expr op=(ADD|SUB) right=expr #BinaryExpression
     | left=expr op=(BITAND|BITOR|BITXOR) right=expr #BinaryExpression
     | left=expr op=(AND|OR) right=expr #BinaryExpression
     | left=expr op=(EQUAL|NOTEQUAL|GT|LT|GE|LE) right=expr #BinaryExpression
     | left=expr op=Identifier right=expr #BinaryExpression
     | Identifier #Variable
     | ctor #Value
     | LPAREN expr RPAREN #GroupedExpression;

// Shader unit

stm : expr
    | def;
stmList : stm (SEMI stm)* SEMI?;

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
BITXOR          : '^';
MOD             : '%';
DECLARE         : '->';
POW             : '*^';

// Keywords

TYPE : 'type';
LET : 'let';
IN : 'in';
VAR : 'var';
UNIFORM : 'uniform';
ATTR : 'attr';

// Primitive types

Boolean :   'true'
        |   'false';

fragment Digit : [0-9];

fragment Digits : Digit+;

Integer : Digits;

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
