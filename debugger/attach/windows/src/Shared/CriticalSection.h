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

#ifndef CRITICAL_SECTION_H
#define CRITICAL_SECTION_H

#ifndef _WIN32_WINNT 
#define _WIN32_WINNT 0x400
#endif
#include <windows.h>

/**
 * Critical section class.
 */
class CriticalSection
{

public:

    /**
     * Destructor.
     */
    CriticalSection();

    /**
     * Destructor.
     */
    virtual ~CriticalSection();

    /**
     * Obstains ownership of the critical section. If another thread is
     * currently holding the critical section this method will block until
     * the other thread releases it.
     */
    void Enter();

    /**
     * Releases ownership of the critical section.
     */
    void Exit();

    /**
     * Tries to obtain ownership of the critical section.  If another thread
     * is currently holding the critical section, the method will return
     * false without acquiring it.  If the critical section is not being
     * held, this behaves exactly as Enter()
     */
    bool TryEnter();

private:

    /**
     * Prevent copying.
     */
    CriticalSection(const CriticalSection&);

    /**
     * Prevent copying.
     */
    CriticalSection& operator=(const CriticalSection&);

private:

    CRITICAL_SECTION    m_criticalSection;

};

#endif