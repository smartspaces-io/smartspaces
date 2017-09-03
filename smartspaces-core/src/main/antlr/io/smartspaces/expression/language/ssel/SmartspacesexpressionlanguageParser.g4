/*
 * Copyright (C) 2017 Keith M. Hughes
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Portions are from the Java Anlter4 grammar on GitHub, that is covered by the BSD License.
 */

grammar SmartspacesexpressionlanguageParser ;

options {
	//tokenVocab = SmartspacesexpressionlanguageLexer;
	//superClass = ;

    //visitor = true;
}

@header {





  package io.smartspaces.expression.language.ssel;

  //import ;





}

expression
	: constant
	| symbol
	| functionCall ;

symbol
	: SYMBOL_NAME ;

functionCall
	: functionName BEGIN_ARG_LIST functionArgument (ARG_LIST_SEPARATOR functionArgument)* END_ARG_LIST ;

functionName
	: FUNCTION_NAME ;

functionArgument
	: expression ;

constant
	: integer
	| string ;

integer
	: INTEGER ;

string
	: StringLiteral ;

INTEGER
	: Digits ;

SYMBOL_NAME
	: Dollar DottedName ;

BEGIN_ARG_LIST
	: LeftParenthesis ;

END_ARG_LIST
	: RightParenthesis ;

ARG_LIST_SEPARATOR
	: Comma ;

WS
	: [ \t\r\n]+ -> skip ;

FUNCTION_NAME
	: NameComponent ;

StringLiteral
	: '"' StringCharacters? '"' ;

fragment HexNumeral
	: '0' [xX] HexDigits ;

fragment HexDigits
	: HexDigit+ ;

fragment HexDigit
	: [0-9a-fA-F] ;

fragment OctalDigits
	: OctalDigit+ ;

fragment OctalDigit
	: [0-7] ;

fragment StringCharacters
	: StringCharacter+ ;

fragment StringCharacter
	: ~["\\]
	| EscapeSequence ;

fragment EscapeSequence
	: '\\' [btnfr"'\\]
	| OctalEscape
	| UnicodeEscape ;

fragment OctalEscape
	: '\\' OctalDigit
	| '\\' OctalDigit OctalDigit
	| '\\' ZeroToThree OctalDigit OctalDigit ;

fragment UnicodeEscape
	: '\\' 'u' HexDigit HexDigit HexDigit HexDigit ;

fragment ZeroToThree
	: [0-3] ;

fragment Comma
	: ',' ;

fragment Dot
	: '.' ;

fragment Dollar
	: '$' ;

fragment RightCurlyBrace
	: '}' ;

fragment LeftParenthesis
	: '(' ;

fragment RightParenthesis
	: ')' ;

fragment NameStart
	: [a-zA-Z] ;

fragment NameLaterCharacters
	: [a-zA-Z0-9] ;

fragment NameComponent
	: NameStart NameLaterCharacters* ;

fragment DottedName
	: NameComponent (Dot NameComponent)* ;

fragment Digit
	: [0-9] ;

fragment Digits
	: Digit+ ;

