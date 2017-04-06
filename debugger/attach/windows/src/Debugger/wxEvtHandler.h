#ifndef EVT_HANDLER_H
#define EVT_HANDLER_H

class wxDebugEvent;

class wxEvtHandler
{
public:
	void AddPendingEvent(wxDebugEvent &event);
public:
	size_t vm;
};

#endif