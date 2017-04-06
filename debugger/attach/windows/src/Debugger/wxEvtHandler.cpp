#include "wxEvtHandler.h"
#include "Protocol.h"
#include "DebugEvent.h"
#include "tinyxml.h"
#include "XmlUtility.h"
#include <iostream>
#include "DebugFrontend.h"

using namespace std;

void wxEvtHandler::AddPendingEvent(wxDebugEvent & event)
{
	EventId id = event.GetEventId();
	DebugFrontend& df = DebugFrontend::Get();
	TiXmlDocument document;
	document.LinkEndChild(WriteXmlNode("type", event.GetEventId()));

	switch (id) {
	case EventId_CreateVM:
		this->vm = event.GetVm();
		break;
	case EventId_NameVM:
	case EventId_Exception:
	case EventId_LoadError:
	case EventId_Message:
		document.LinkEndChild(WriteXmlNode("message", event.GetMessageString(), true));
		document.LinkEndChild(WriteXmlNode("message_type", event.GetMessageType()));
		break;
	case EventId_Break:
	{
		TiXmlDocument stacks;
		stacks.Parse(event.GetMessageString().c_str());

		auto root = stacks.RootElement();

		document.LinkEndChild(root->Clone());
		break;
	}
	case EventId_LoadScript:
	{
		auto script = DebugFrontend::Get().GetScript(event.GetScriptIndex());
		document.LinkEndChild(WriteXmlNode("name", script->name));
		document.LinkEndChild(WriteXmlNode("index", event.GetScriptIndex()));
		if (script->name[0] == '@') {
			DebugFrontend::Get().DoneLoadingScript(vm);
		}
		break;
	}
	case EventId_SetBreakpoint:
	{
		auto script = DebugFrontend::Get().GetScript(event.GetScriptIndex());
		document.LinkEndChild(WriteXmlNode("name", script->name));
		document.LinkEndChild(WriteXmlNode("index", event.GetScriptIndex()));
		document.LinkEndChild(WriteXmlNode("line", event.GetLine()));
		break;
	}
	case EventId_EvalResult:
	{
		document.LinkEndChild(WriteXmlNode("result", event.GetEvalResult() ? 1 : 0));
		document.LinkEndChild(WriteXmlNode("id", event.GetEvalId()));

		TiXmlDocument stacks;
		stacks.Parse(event.GetMessageString().c_str());
		auto root = stacks.RootElement();

		TiXmlElement* value = new TiXmlElement("value");
		value->LinkEndChild(root->Clone());
		document.LinkEndChild(value);
		break;
	}
	}

	TiXmlPrinter printer;
	printer.SetIndent("");
	printer.SetLineBreak("");

	document.Accept(&printer);
	string result = printer.Str();

	cout << "[start]" << endl;
	cout << result << endl;
	cout << "[end]" << endl;
}
