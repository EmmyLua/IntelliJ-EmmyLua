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

#include "Tokenizer.h"

bool IsSymbol(char c)
{
    // In addition to the regular tests, we need to make sure this isn't
    // an extended ASCII character as well (ispunct throws up if it is).
    return c > 0 && c != '_' && ispunct(c);
}

bool IsSpace(char c)
{
    // In addition to the regular tests, we need to make sure this isn't
    // an extended ASCII character as well (isspace throws up if it is).
    return c > 0 && isspace(c);
}

bool IsDigit(char c)
{
    return c >= '0' && c <= '9';
}

static void SkipWhitespace(wxInputStream& input, unsigned int& lineNumber)
{

    char c;

    while (!input.Eof())
    {
        c = input.Peek();
        if (c == '\n')
        {
            ++lineNumber;
        }
        else if (c == '-')
        {
            input.GetC();
            char c2 = input.Peek();
            if (c2 == '-')
            {
                // Lua single line comment.
                while (!input.Eof() && input.GetC() != '\n')
                {
                }
                ++lineNumber;
                continue;
            }
        }
        else if (c == '/')
        {
            input.GetC();
            char c2 = input.Peek();
            if (c2 == '*')
            {
                // C++ block comment.
                input.GetC();
                while (!input.Eof())
                {
                    c = input.GetC();
                    if (c == '\n')
                    {
                        ++lineNumber;
                    }
                    if (c == '*' && input.Peek() == '/')
                    {
                        input.GetC();
                        break;
                    }
                }
                continue;
            }
            else if (c2 == '/')
            {
                // C++ single line comment.
                while (!input.Eof() && input.GetC() != '\n')
                {
                }
                ++lineNumber;
                continue;
            }
            else
            {
                input.Ungetch(c);
                break;
            }
        }
        if (!IsSpace(c))
        {
            break;
        }
        input.GetC();
    }

}

bool GetToken(wxInputStream& input, wxString& result, unsigned int& lineNumber)
{

    result.Empty();

    SkipWhitespace(input, lineNumber);

    // Reached the end of the file.
    if (input.Eof())
    {
        return false;
    }

    char c = input.GetC();

    if (c == '\"')
    {

        // Quoted string, search for the end quote.

        do
        {
            result += c;
            c = input.GetC();
        }
        while (input.IsOk() && c != '\"');

        result += c;
        return true;

    }

    char n = input.Peek();

    if (IsDigit(c) || (c == '.' && IsDigit(n)) || (c == '-' && IsDigit(n)))
    {

        bool hasDecimal = false;

        while (!IsSpace(c))
        {

            result.Append(c);

            if (input.Eof())
            {
                return true;
            }

            c = input.Peek();

            if (!IsDigit(c) && c != '.')
            {
                return true;
            }

            input.GetC();

            if (c == '\n')
            {
                ++lineNumber;
                return true;
            }

        }

    }
    else
    {

        if (IsSymbol(c))
        {
            result = c;
            return true;
        }

        while (!IsSpace(c) && !input.Eof())
        {

            result.Append(c);

            if (IsSymbol(input.Peek()))
            {
                break;
            }

            c = input.GetC();

            if (c == '\n')
            {
                ++lineNumber;
                return true;
            }

        }

    }

    return true;

}

bool PeekToken(wxInputStream& input, wxString& result)
{
    
    unsigned int lineNumber = 0;

    if (!GetToken(input, result, lineNumber))
    {
        return false;
    }

    input.Ungetch(result + " ", result.Length() + 1);
    return true;

}