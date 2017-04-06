//=============================================================================
//
// CrashHandler.h
// 
// Created by Max McGuire (max@unknownworlds.com)
// Copyright 2009, Unknown Worlds Entertainment
//
//=============================================================================

#ifndef CRASH_HANDLER_H
#define CRASH_HANDLER_H

#include "Updater.h"

/**
 * Interface for setting a handler in the event of a crash.
 */
class CrashHandler
{

public:

    /// Prototype for the callback function which is called by the handler.
    typedef void (*Callback)(void* address);

    /**
     * Sets a callback function which is called in the event the application
     * crashes.
     */
    UPDATER_EXPORT static void SetCallback(Callback callback);

    /**
     * Writes a minidump file to the specified location. This can only be called
     * when a crash has occurred.
     */
    UPDATER_EXPORT static bool WriteMiniDump(const char* fileName);

};

#endif