/*

Decoda
Copyright (C) 2007-2013 Unknown Worlds Entertainment, Inc. 

This file is part of Decoda.

Decoda is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Decoda is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Decoda.  If not, see <http://www.gnu.org/licenses/>.

*/

#ifndef TOKENIZER_H
#define TOKENIZER_H

//
// Forward declarations.
//

class wxInputStream;
class wxString;

/**
 * Returns true if the character is a white space character. This properly handles
 * extended ASCII characters.
 */
bool IsSpace(char c);

/**
 * Returns truie if the character is a symbol. Symbols include all of the punctuation
 * marks except _.
 */
bool IsSymbol(char c);
    
/**
 * Reads a token from the input stream and stores it in result. If the end of the
 * stream was reached before anything was read, the function returns false.
 */
bool GetToken(wxInputStream& input, wxString& result, unsigned int& lineNumber);

/**
 * Reads a token from the input stream and stores it in result without actually pulling
 * the token from the stream. If the end of the stream was reached before anything was
 * read, the function returns false.
 */
bool PeekToken(wxInputStream& input, wxString& result);

#endif