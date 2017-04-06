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

#ifndef LINE_MAPPER_H
#define LINE_MAPPER_H

#include <vector>
#include <string>

/**
 * This class is used to map between lines in an original and a modified
 * document.
 */
class LineMapper
{

public:

    static const unsigned int s_invalidLine = static_cast<unsigned short>(-1);

    void Update(const std::string& oldCode, const std::string& newCode);

    unsigned int GetOldLine(unsigned int lineNumber) const;
    unsigned int GetNewLine(unsigned int lineNumber) const;

private:
    
    /**
     * Initializes the line mapping based on the diff between two sets of lines.
     */
    void Diff(const std::vector<std::string>& X, const std::vector<std::string>& Y);
    
    /**
     * Initializes the line mapping based on a matrix C which stores the length of
     * the longest common substrings of prefixes of two sets of lines, X and Y.
     */
    void Diff(const std::vector<unsigned short>& C, unsigned int r, const std::vector<std::string>& X,
        const std::vector<std::string>& Y, unsigned int sm, unsigned int sn, unsigned int i, unsigned int j);

    /**
     * Returns true if two lines are equal with respect to diffing.
     */
    bool Equal(const std::string& line1, const std::string& line2) const;

    /**
     * Tokenizes the specified code into lines.
     */
    void DivideIntoLines(const std::string& code, std::vector<std::string>& lines) const;

    /**
     * "Standardizes" the white space in a line. This will replace tabs and newlines with
     * spaces.
     */
    void CleanWhiteSpace(std::string& line) const;

    /**
     * Removes spaces from the beginning and end of a line.
     */
    void TrimWhiteSpaces(std::string& line) const;

private:

    // We store shorts to save memory. That gives us 60k lines, which should
    // be more than sufficient for code files.

    std::vector<unsigned short> m_oldToNew;
    std::vector<unsigned short> m_newToOld;

};

#endif