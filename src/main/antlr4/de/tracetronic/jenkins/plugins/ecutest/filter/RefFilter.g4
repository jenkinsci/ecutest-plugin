/*
 * Copyright (c) 2015 TraceTronic GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice, this
 *      list of conditions and the following disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above copyright notice, this
 *      list of conditions and the following disclaimer in the documentation and/or
 *      other materials provided with the distribution.
 *
 *   3. Neither the name of TraceTronic GmbH nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
