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

#include "StlUtility.h"

void ReplaceAll(std::string& string, const std::string& find, const std::string& sub)
{

    size_t pos = 0;
    
    while (pos != -1)
    {

        pos = string.find(find, pos);

        if (pos != -1)
        {
            string.replace(string.begin() + pos, string.begin() + pos + find.length(), sub);
            pos += sub.length();
        }

    }

}

std::string TrimSpaces(const std::string& string)
{

    const char* whitespace = " \t\n"; 

    size_t start = string.find_first_not_of(whitespace);
    size_t end   = string.find_last_not_of(whitespace);

    if (start == std::string::npos)
    {
        return std::string();
    }

    return string.substr(start, end - start + 1);

}

std::string FixFileName(const std::string& fileName)
{
	std::string out = fileName;
	ReplaceAll(out, "\\", "/");
	// remove ./
	if (out[0] == '.' && out[1] == '/') {
		out.erase(0, 2);
	}
	else if (out[0] == '@' && out[1] == '.' && out[2] == '/') {
		out.erase(1, 2);
	}
	return out;
}

std::string GetDirectory(const std::string& fileName)
{

    size_t slash = fileName.find_last_of("\\/");

    if (slash == std::string::npos)
    {
        slash = 0;
    }

    return fileName.substr(0, slash);

}

bool GetIsSlash(char c)
{
    return c == '\\' || c == '/';
}

void CopyString(std::string& dst, const char* data, size_t size)
{
	char* temp = (char*)malloc(size + 1);
	memcpy(temp, data, size);
	temp[size] = '\0';
	dst = temp;
	free(temp);
}