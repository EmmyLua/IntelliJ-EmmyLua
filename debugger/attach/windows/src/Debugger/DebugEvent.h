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

#ifndef DEBUG_EVENT_H
#define DEBUG_EVENT_H

#include "Protocol.h"
#include <string>

//
// Event definitions.
//

//DECLARE_EVENT_TYPE(wxEVT_DEBUG_EVENT, -1)

/**
 * Event class used to pass information from the debug server to the
 * wxWidget UI.
 */
class wxDebugEvent// : public wxEvent
{

public:

    /**
     * Constructor.
     */
    wxDebugEvent(EventId eventId, size_t vm);

    /** 
     * Returns the event id of the event.
     */
    EventId GetEventId() const;

    /**
     * Returns the id of the virtual machine the event came from.
     */
	size_t GetVm() const;

    /**
     * Returns the index of the script the event relates to.
     */
    unsigned int GetScriptIndex() const;

    /**
     * Sets the index of the script the event relates to.
     */
    void SetScriptIndex(unsigned int scriptIndex);

    /**
     * Returns the number of the line in the script the event relates to.
     */
    unsigned int GetLine() const;

    /**
     * Sets the number of the line in the script the event relates to.
     */
    void SetLine(unsigned int scriptIndex);

    /**
     * Gets the boolean value for the event. This is a generic value that's
     * meaning depends on the event.
     */
    bool GetEnabled() const;

    /**
     * Sets the boolean value for the event. This is a generic value that's
     * meaning depends on the event.
     */
    void SetEnabled(bool enabled);

    /**
     * Returns the message associated with the event. Not all events will have
     * messages.
     */
    const std::string& GetMessageString() const;

    /**
     * Sets the message associated with the event.
     */
    void SetMessageString(const std::string& message);

    /**
     * Returns the type of the string message (error, warning, etc.) This is only
     * relevant when the event deals with a message.
     */
    MessageType GetMessageType() const;

    /**
     * Sets the type of the string message. This is only relevant when the event
     * deals with a message.
     */
    void SetMessageType(MessageType messageType);

    /**
     * From wxEvent.
     */
    virtual wxDebugEvent* Clone() const;

	void SetEvalResult(bool suc);
	bool GetEvalResult() const;
	void SetEvalId(int id);
	int GetEvalId() const;
private:

    EventId         m_eventId;
	size_t    m_vm;

    unsigned int    m_scriptIndex;
    unsigned int    m_line;

    bool            m_enabled;

    std::string		m_message;
    MessageType     m_messageType;

	bool m_evalResult;
	int m_evalId;
};

//typedef void (wxEvtHandler::*wxDebugEventFunction)(wxDebugEvent&);

#define EVT_DEBUG(fn) \
    DECLARE_EVENT_TABLE_ENTRY( wxEVT_DEBUG_EVENT, 0, -1, \
    (wxObjectEventFunction) (wxEventFunction) wxStaticCastEvent( wxDebugEventFunction, & fn ), (wxObject *) NULL ),

#endif