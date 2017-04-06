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

#ifndef CHANNEL_H
#define CHANNEL_H

#include <windows.h>
#include <string>
#include "Def.h"

/**
 * Communication channel used to between two processess. The current
 * implementation uses pipes, however in the future we may expand this
 * to include sockets for communictating across a network.
 */
class Channel
{

public:

    /**
     * Constructor. Create must be called on the channel before it can be used.
     */
    Channel();
    
    /**
     * Destructor.
     */
    virtual ~Channel();

    /**
     * Initializes the channel.
     */
    bool Create(const char* name);

    /**
     * Connects to an existing channel.
     */
    bool Connect(const char* name);

    /**
     * Waits for someone to connect to the channel.
     */
    bool WaitForConnection();

    /**
     * Shuts down the channel.
     */
    void Destroy();

    /**
     * Writes a 32-bit unsigned integer to the channel and returns immediately.
     */
    bool WriteUInt32(unsigned int value);

	bool WriteSize(size_t size);

    /**
     * Writes a string to the channel and returns immediately.
     */
    bool WriteString(const char* value);

    /**
     * Writes a string to the channel and returns immediately.
     */
    bool WriteString(const std::string& value);

    /**
     * Writes a boolean to the channel and returns immediately.
     */
    bool WriteBool(bool value);

    /**
     * Reads a 32-bit unsigned integer from the channel. This operation blocks
     * until the data is available.
     */
    bool ReadUInt32(unsigned int& value);

	bool ReadSize(size_t& size);

    /**
     * Reads a string from the channel. This operation blocks until the
     * data is available.
     */
    bool ReadString(std::string& value);

    /**
     * Reads a boolean from the channel. This operation blocks until the
     * data is available.
     */
    bool ReadBool(bool& value);

    /**
     * Flushes the buffers, causing any written data to be sent.
     */
    void Flush();

private:

    /**
     * Writes data to the channel and returns immediately.
     */
    bool Write(const void* buffer, unsigned int length);

    /**
     * Reads data to the channel. Returns when the specified amount has been
     * read or when an error occurs.
     */
    bool Read(void* buffer, unsigned int length);

private:

    HANDLE  m_pipe;
    HANDLE  m_doneEvent;
    HANDLE  m_readEvent;

    bool    m_creator;

};

#endif