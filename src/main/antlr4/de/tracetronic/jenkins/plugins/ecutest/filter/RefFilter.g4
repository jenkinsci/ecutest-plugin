/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

grammar RefFilter;

options{
    language = Java;
}

// PARSER
filterExpression : filter (LOGICAL filter)*;
filter : (expression (LOGICAL filter)* | LPAREN filter RPAREN);
expression : ((equalExpression | relExpression | hasExpression) | (STRING | NUMBER | BOOLEAN));
equalExpression : (KEYWORD OPERATOR_EQUAL (STRING | NUMBER | BOOLEAN));
relExpression : (KEYWORD OPERATOR_REL NUMBER);
hasExpression : (KEYWORD OPERATOR_HAS STRING);

// LEXER
LPAREN : '(' ;
RPAREN : ')' ;
KEYWORD : (KEYWORDS | SINGLE_QUOTED_KEYWORD | DOUBLE_QUOTED_KEYWORD);
SINGLE_QUOTED_KEYWORD : '\'' (KEYWORDS | KEYWORDS_QUOTED) '\'';
DOUBLE_QUOTED_KEYWORD : '"' (KEYWORDS | KEYWORDS_QUOTED) '"';
KEYWORDS : ('Designer'|'Name'|'Status'|'Testlevel'|'Tools'|'VersionCounter');
KEYWORDS_QUOTED : ('Design Contact'|'Design Department'|'Estimated Duration [min]'|'Execution Priority'|'Test Comment');
OPERATOR_EQUAL : ('='|'!=');
OPERATOR_REL : ('<'|'>'|'<='|'>=');
OPERATOR_HAS : ('has'|'hasnot');
STRING : (SINGLE_QUOTED_STRING | DOUBLE_QUOTED_STRING);
SINGLE_QUOTED_STRING : '\'' ~('\t'|'\r'|'\n'|'\'')* '\'';
DOUBLE_QUOTED_STRING : '"' ~('\t'|'\r'|'\n'|'"')* '"';
NUMBER : [0-9]+;
BOOLEAN : ('True'|'False');
LOGICAL : ('and'|'or');
WS : [ \t\r\n]+ -> skip; // skip spaces, tabs, newlines
