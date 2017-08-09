#ifndef EVT_HANDLER_H
#define EVT_HANDLER_H

class wxDebugEvent;

class wxEvtHandler
{
public:
	wxEvtHandler();
	void AddPendingEvent(wxDebugEvent &event);
	size_t vm;
};

#endif