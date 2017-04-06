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

#include "LineMapper.h"
//#include "Tokenizer.h"

#include <algorithm>

void LineMapper::Update(const std::string& oldCode, const std::string& newCode)
{

    std::vector<std::string> X;
    DivideIntoLines(oldCode, X);

    std::vector<std::string> Y;
    DivideIntoLines(newCode, Y);
    
    Diff(X, Y);

}

unsigned int LineMapper::GetOldLine(unsigned int lineNumber) const
{
    if (lineNumber < m_newToOld.size())
    {
        return m_newToOld[lineNumber];
    }
    else if (m_newToOld.empty())
    {
        return lineNumber;
    }
    else
    {
        return m_newToOld[ m_newToOld.size() - 1 ];
    }
}

unsigned int LineMapper::GetNewLine(unsigned int lineNumber) const
{
    if (lineNumber < m_oldToNew.size())
    {
        return m_oldToNew[lineNumber];
    }  
    else if (m_oldToNew.empty())
    {
        return lineNumber;
    }
    else
    {
        return m_oldToNew[ m_oldToNew.size() - 1 ];
    }
}

bool LineMapper::Equal(const std::string& line1, const std::string& line2) const
{
    return line1 == line2;
}

void LineMapper::Diff(const std::vector<unsigned short>& C, unsigned int r, const std::vector<std::string>& X, const std::vector<std::string>& Y, unsigned int sm, unsigned int sn, unsigned int i, unsigned int j)
{
    
    if (i > 0 && j > 0 && Equal(X[i - 1 + sm], Y[j - 1 + sn] ))
    {

        m_oldToNew[i - 1 + sm] = j - 1 + sn;
        m_newToOld[j - 1 + sn] = i - 1 + sm;

        Diff(C, r, X, Y, sm, sn, i - 1, j - 1);

    }
    else if (j > 0 && (i == 0 || C[i + (j-1) * r] >= C[(i-1) + j * r]))
    {
        Diff(C, r, X, Y, sm, sn, i, j-1);
        m_newToOld[j - 1 + sn] = -1;
    }
    else if (i > 0 && (j == 0 || C[i + (j-1) * r] < C[(i-1) + j * r]))
    {
        Diff(C, r, X, Y, sm, sn, i-1, j);
        m_oldToNew[i - 1 + sm] = -1;
    }

}

void LineMapper::Diff(const std::vector<std::string>& X, const std::vector<std::string>& Y)
{

    // This is based off the description of the LCS algorithm here:
    // http://en.wikipedia.org/wiki/Longest_common_subsequence_problem

    unsigned int m = X.size();
    unsigned int n = Y.size();

    m_oldToNew.resize( m );
    m_newToOld.resize( n );

    if (m == 0)
    {
        for (unsigned int i = 0; i < n; ++i)
        {
            m_newToOld[i] = 0;
        }
        return;
    }

    if (n == 0)
    {
        for (unsigned int i = 0; i < m; ++i)
        {
            m_oldToNew[i] = 0;
        }
        return;
    }

    unsigned int sm = 0;
    unsigned int em = m - 1;

    unsigned int sn = 0;
    unsigned int en = n - 1;

    // Trim off the matching lines at the beginning of the files since these will most
    // likey match are reduces our search space.
    while (sm <= em && sn <= en && Equal(X[sm], Y[sn]))
    {
        ++sm;
        ++sn;
    }
    
    // Trim off the matching lines at the end of the files since these will most
    // likey match are reduces our search space.
    while (sm <= em && sn <= en && Equal(X[em], Y[en]))
    {
        --em;
        --en;
    }

    // Compute the length of the longest common subsequence for all of the prefixes.
    // When we're done, C[i, j] is the length of the longest common subsequence of
    // (X[0], X[1], ... X[i-1]) and (Y[0], Y[1], ... Y[j-1]).

    unsigned int c = (en - sn + 1) + 1;
    unsigned int r = (em - sm + 1) + 1;

    std::vector<unsigned short> C( r * c ); 

    for (unsigned int i = 0; i < r; ++i)
    {
        C[i + 0 * r] = 0; 
    }

    for (unsigned int j = 1; j < c; ++j)
    {
        C[0 + j * r] = 0; 
    }
    
    for (unsigned int i = 1; i < r; ++i)
    {
        for (unsigned int j = 1; j < c; ++j)
        {
            if ( Equal(X[i - 1 + sm], Y[j - 1 + sn]) )
            {
                C[i + j * r] = C[(i - 1) + (j - 1) * r] + 1;
            }
            else
            {
                C[i + j * r] = std::max(C[i + (j - 1) * r], C[(i - 1) + j * r]);
            }
        }
    }

    // Match up the lines in the beginning that we skipped.
    for (unsigned int i = 0; i <= sm && i < m; ++i)
    {
        m_oldToNew[i] = i;
        m_newToOld[i] = i;
    }

    Diff(C, r, X, Y, sm, sn, r - 1, c - 1);

    // Match up the lines in the end that we skipped.
    
    for (unsigned int i = em + 1; i < m; ++i)
    {
        m_oldToNew[i] = i - em + en;
    }

    for (unsigned int i = en + 1; i < n; ++i)
    {
        m_newToOld[i] = i - en + em;
    }

}

void LineMapper::DivideIntoLines(const std::string& code, std::vector<std::string>& lines) const
{

    unsigned int s = 0;

    while (s < code.length())
    {
        
        unsigned int e = code.find('\n', s);

        if (e == std::string::npos)
        {
            e = code.length();
        }

        std::string line = code.substr(s, e - s);
        CleanWhiteSpace(line);

        lines.push_back(line);
        s = e + 1;

    }

}

bool IsSpace(char c)
{
	return c > 0&& (c == ' ' || c == '\t' || c == '\n' || c == '\r' );
}

void LineMapper::CleanWhiteSpace(std::string& line) const
{

    // Since white space doesn't matter for diff, remove it. Note this can
    // cause a slight problem if there are spaces inside of a string (since we
    // don't do any special parsing for that), but in practice isn't really an
    // issue.

    TrimWhiteSpaces(line);

    for (unsigned int i = 0; i < line.length(); ++i)
    {

        if (IsSpace(line[i]))
        {
            // Since we trimmed spaces from the front and back, this must
            // not be the last character.
            if (IsSpace(line[i + 1]))
            {
                line.erase(i);
                --i;
            }
            else
            {
                line[i] = ' ';
            }
        }
            
    }

}

void LineMapper::TrimWhiteSpaces(std::string& line) const
{

    const char* whitespace = " \t\n\r"; 

    size_t start = line.find_first_not_of(whitespace);
    size_t end   = line.find_last_not_of(whitespace);

    if (start == std::string::npos)
    {
        line.clear();
        return;
    }

    if (end == std::string::npos)
    {
        end = line.length();
    }

    if (start > 0 || end < line.length())
    {
        line = line.substr(start, end - start + 1);
    }

}