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

#ifndef STL_UTILITY_H
#define STL_UTILITY_H

#include <vector>
#include <string>

template <typename T>
void ClearVector(std::vector<T*>& v)
{
    for (unsigned int i = 0; i < v.size(); ++i)
    {
        delete v[i];
    }
    v.clear();
}

template <typename T>
void CopyVector(std::vector<T*>& dst, const std::vector<T*>& src)
{

    ClearVector(dst);
    dst.resize(src.size());

    for (unsigned int i = 0; i < src.size(); ++i)
    {
        dst[i] = new T(*src[i]);
    }

}

/**
 * Replaces all instances of find in string with sub.
 */
void ReplaceAll(std::string& string, const std::string& find, const std::string& sub);

/**
 * Removes white space characters from the beginning and end of a string.
 */
std::string TrimSpaces(const std::string& string);

std::string FixFileName(const std::string & fileName);

/**
 * Returns the directory component of a file name. The returned directory
 * does not end in a slash.
 */
std::string GetDirectory(const std::string& fileName);

/**
 * Returns true if the character is a slash (/ or \)
 */
bool GetIsSlash(char c);

void CopyString(std::string & dst, const char* data, size_t size);

#endif