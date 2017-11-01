#include "wxEvtHandler.h"
#include "Protocol.h"
#include "DebugEvent.h"
#include "tinyxml.h"
#include "XmlUtility.h"
#include <iostream>
#include "DebugFrontend.h"

using namespace std;

wxEvtHandler::wxEvtHandler(): vm(0)
{
}

void wxEvtHandler::AddPendingEvent(wxDebugEvent & event)
{
	auto vm = event.GetVm();
	if (vm != 0)
		this->vm = event.GetVm();

	EventId id = event.GetEventId();
	DebugFrontend& df = DebugFrontend::Get();
	TiXmlDocument document;
	document.LinkEndChild(WriteXmlNode("type", event.GetEventId()));

	switch (id) {
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
	case EventId_EvalResult:
	{
		document.LinkEndChild(WriteXmlNode("result", event.GetEvalResult() ? 1 : 0));
		document.LinkEndChild(WriteXmlNode("id", event.GetEvalId()));

		TiXmlDocument stacks;
		stacks.Parse(event.GetMessageString().c_str());
		auto root = stacks.RootElement();

		if (root != nullptr) {
			TiXmlElement* value = new TiXmlElement("value");
			value->LinkEndChild(root->Clone());
			document.LinkEndChild(value);
		}
		break;
	}
	case EventId_Initialize: break;
	case EventId_CreateVM: break;
	case EventId_DestroyVM: break;
	case EventId_SessionEnd: break;
	default: ;
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
