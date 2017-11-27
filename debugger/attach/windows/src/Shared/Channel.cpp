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

#include "Channel.h"
#include <stdio.h>
#include <assert.h>

Channel::Channel()
{
    m_pipe      = INVALID_HANDLE_VALUE;
    m_doneEvent = INVALID_HANDLE_VALUE;
    m_readEvent = INVALID_HANDLE_VALUE;
    m_creator   = false;
}

Channel::~Channel()
{
    Destroy();
}

bool Channel::Create(const char* name)
{

    char pipeName[256];
    _snprintf(pipeName, 256, "\\\\.\\pipe\\%s", name);

    DWORD bufferSize = 2048;

    m_pipe = CreateNamedPipe(pipeName, PIPE_ACCESS_DUPLEX | FILE_FLAG_OVERLAPPED,
        PIPE_TYPE_MESSAGE | PIPE_READMODE_MESSAGE, 1, bufferSize, bufferSize, 0, nullptr);

    if (m_pipe != INVALID_HANDLE_VALUE)
    {
        // Remember that we're the creator of the pipe so we can properly
        // destroy it.
        m_creator = true;
    }

    if (m_pipe != INVALID_HANDLE_VALUE)
    {
        m_doneEvent = CreateEvent(nullptr, FALSE, FALSE, nullptr);
        m_readEvent = CreateEvent(nullptr, FALSE, FALSE, nullptr);
    }

    return m_pipe != INVALID_HANDLE_VALUE;

}

bool Channel::Connect(const char* name)
{

    char pipeName[256];
    _snprintf(pipeName, 256, "\\\\.\\pipe\\%s", name);

    m_pipe = CreateFile(pipeName, GENERIC_READ | GENERIC_WRITE, 0, nullptr, OPEN_EXISTING, FILE_FLAG_OVERLAPPED, nullptr);

    if (m_pipe != INVALID_HANDLE_VALUE)
    {
        m_doneEvent = CreateEvent(nullptr, FALSE, FALSE, nullptr);
        m_readEvent = CreateEvent(nullptr, FALSE, FALSE, nullptr);
        DWORD flags = PIPE_READMODE_MESSAGE;
        SetNamedPipeHandleState(m_pipe, &flags, nullptr, nullptr);
    }

    return m_pipe != INVALID_HANDLE_VALUE;

}

bool Channel::WaitForConnection() const
{
    return ConnectNamedPipe(m_pipe, nullptr) != FALSE;
}

void Channel::Destroy()
{

    if (m_creator)
    {
        FlushFileBuffers(m_pipe);
        DisconnectNamedPipe(m_pipe);
        m_creator = false;
    }

    if (m_doneEvent != INVALID_HANDLE_VALUE)
    {
        
        // Signal the done event so that if we're currently blocked reading,
        // we'll stop.

        SetEvent(m_doneEvent);

        CloseHandle(m_doneEvent);
        m_doneEvent = INVALID_HANDLE_VALUE;

    }

    if (m_readEvent != INVALID_HANDLE_VALUE)
    {
        CloseHandle(m_readEvent);
        m_readEvent = INVALID_HANDLE_VALUE;
    }

    if (m_pipe != INVALID_HANDLE_VALUE)
    {
        CloseHandle(m_pipe);
        m_pipe = INVALID_HANDLE_VALUE;
    }

}

bool Channel::Write(const void* buffer, unsigned int length) const
{

    assert(m_pipe != INVALID_HANDLE_VALUE);

    if (length == 0)
    {
        // Because of the way message pipes work, writing 0 is different than
        // writing nothing.
        return true;
    }

    OVERLAPPED overlapped = { 0 };
    overlapped.hEvent = m_readEvent;

    BOOL result = WriteFile(m_pipe, buffer, length, nullptr, &overlapped) != 0;

    if (result == FALSE)
    {
        DWORD error = GetLastError();

        if (error == ERROR_IO_PENDING)
        {
           // Wait for the operation to complete so that we don't need to keep around
           // the buffer.
           WaitForSingleObject(m_readEvent, INFINITE);

           DWORD numBytesWritten = 0;

           if (GetOverlappedResult(m_pipe, &overlapped, &numBytesWritten, FALSE))
           {
               result = (numBytesWritten == length);
           }
        }
    }

    return result == TRUE;

}

bool Channel::WriteUInt32(uint32_t value) const
{
    DWORD temp = value;
    return Write(&temp, 4);
}

bool Channel::WriteUInt64(uint64_t size) const
{
	uint64_t temp = size;
	return Write(&temp, 8);
}

bool Channel::WriteString(const char* value) const
{
    unsigned int length = static_cast<int>(strlen(value));
    if (!WriteUInt32(length))
    {
        return false;
    }
    if (length > 0)
    {
        return Write(value, length);
    }
    return true;
}

bool Channel::WriteString(const std::string& value) const
{
	unsigned int length = value.length();
    if (!WriteUInt32(length))
    {
        return false;
    }
    if (length > 0)
    {
        return Write(value.c_str(), length);
    }
    return true;
}

bool Channel::WriteBool(bool value) const
{
    return WriteUInt32(value ? 1 : 0);
}

bool Channel::ReadUInt32(uint32_t& value) const
{
    DWORD temp;
    if (!Read(&temp, 4))
    {
        return false;
    }
    value = temp;
    return true;
}

bool Channel::ReadUint64(uint64_t& size) const
{
	uint64_t temp;
	if (!Read(&temp, 8)) {
		return false;
	}
	size = temp;
	return true;
}

bool Channel::ReadString(std::string& value) const
{
    unsigned int length;
    
    if (!ReadUInt32(length))
    {
        return false;
    }

    if (length != 0)
    {

        char* buffer = new char[length + 1];

        if (!Read(buffer, length))
        {
            delete [] buffer;
            return false;
        }

        buffer[length] = 0;
        value = buffer;
        
        delete [] buffer;

    }
    else
    {
        value.clear();
    }

    return true;

}

bool Channel::ReadBool(bool& value) const
{

    unsigned int temp;

    if (ReadUInt32(temp))
    {
        value = temp != 0;
        return true;
    }

    return false;

}

bool Channel::Read(void* buffer, unsigned int length) const
{

    assert(m_pipe != INVALID_HANDLE_VALUE);
    
    if (length == 0)
    {
        // Because of the way message pipes work, reading 0 is different than
        // reading nothing.
        return true;
    }

    OVERLAPPED overlapped = { 0 };
    overlapped.hEvent = m_readEvent;

    DWORD numBytesRead;
    BOOL result = ReadFile(m_pipe, buffer, length, &numBytesRead, &overlapped);

    if (result == FALSE)
    {

        DWORD error = GetLastError();

        if (error == ERROR_IO_PENDING)
        {
        
            // Wait for the operation to complete.
            
            HANDLE events[] =
                {
                    m_readEvent,
                    m_doneEvent,
                };

            WaitForMultipleObjects(2, events, FALSE, INFINITE);

            if (WaitForSingleObject(m_doneEvent, 0) == WAIT_OBJECT_0)
            {
                // The pipe has been closed.
                result = FALSE;
            }
            else if (GetOverlappedResult(m_pipe, &overlapped, &numBytesRead, FALSE))
            {
                result = (numBytesRead == length);
            }
        
        }

    }

    return result == TRUE;

}

void Channel::Flush() const
{
    //FlushFileBuffers(m_pipe);
}
