//=============================================================================
//
// CriticalSectionTryLock.h
// 
// Created by Tony Cannon (tony.cannon@rad-ent.com).
//
//=============================================================================

#ifndef CRITICAL_SECTION_TRY_LOCK_H
#define CRITICAL_SECTION_TRY_LOCK_H

//
// Forward declarations.
//

class CriticalSection;

/**
 *
 */
class CriticalSectionTryLock
{

public:

    /**
     * Constructor.
     */
    CriticalSectionTryLock(CriticalSection& criticalSection);

    /**
     * Destructor.
     */
    ~CriticalSectionTryLock();

    /**
     * Returns whether or not the lock is currently held.
     */
    bool IsHeld() const;

private:

    CriticalSection&    m_criticalSection;
    bool                m_isHeld;
};

#endif