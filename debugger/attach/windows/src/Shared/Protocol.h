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

#ifndef PROTOCOL_H
#define PROTOCOL_H

enum MessageType
{
    MessageType_Normal          = 0,
    MessageType_Warning         = 1,
    MessageType_Error           = 2,
	MessageType_Stdout			= 3,
	MessageType_Stderr			= 4,
};

enum CodeState
{
    CodeState_Normal            = 0,    // The code is normal.
    CodeState_Unavailable       = 1,    // The code for the script was not available.
    CodeState_Binary            = 2,    // The code was loaded as a binary/compiled file
	CodeState_Reload            = 3,
	CodeState_ReqReload         = 4,
};

enum EventId
{
    EventId_Initialize          = 11,   // Sent when the backend is ready to have its initialize function called
    EventId_CreateVM            = 1,    // Sent when a script VM is created.
    EventId_DestroyVM           = 2,    // Sent when a script VM is destroyed.
    EventId_LoadScript          = 3,    // Sent when script data is loaded into the VM.
    EventId_Break               = 4,    // Sent when the debugger breaks on a line.
    EventId_SetBreakpoint       = 5,    // Sent when a breakpoint has been added in the debugger.
    EventId_Exception           = 6,    // Sent when the script encounters an exception (e.g. crash).
    EventId_LoadError           = 7,    // Sent when there is an error loading a script (e.g. syntax error).
    EventId_Message             = 9,    // Event containing a string message from the debugger.
    EventId_SessionEnd          = 8,    // This is used internally and shouldn't be sent.
    EventId_NameVM              = 10,   // Sent when the name of a VM is set.

	EventId_EvalResult          = 100,  //计算的结果
};

enum CommandId
{
    CommandId_Continue          = 1,    // Continues execution until the next break point.
    CommandId_StepOver          = 2,    // Steps to the next line, not entering any functions.
	CommandId_StepInto			= 3,    // Steps to the next line, entering any functions.
	CommandId_StepOut			= 16,    // Steps out functions
	CommandId_AddBreakpoint     = 4,    // Toggles a breakpoint on a line on.
	CommandId_DelBreakpoint     = 15,   // Toggles a breakpoint on a line off.
    CommandId_Break             = 5,    // Instructs the debugger to break on the next line of script code.
    CommandId_Evaluate          = 6,    // Evaluates the value of an expression in the current context.
    CommandId_Detach            = 8,    // Detaches the debugger from the process.
    CommandId_PatchReplaceLine  = 9,    // Replaces a line of code with a new line.
    CommandId_PatchInsertLine   = 10,   // Adds a new line of code.
    CommandId_PatchDeleteLine   = 11,   // Deletes a line of code.
    CommandId_LoadDone          = 12,   // Signals to the backend that the frontend has finished processing a load.
    CommandId_IgnoreException   = 13,   // Instructs the backend to ignore the specified exception message in the future.
    CommandId_DeleteAllBreakpoints = 14,// Instructs the backend to clear all breakpoints set
	CommandId_InitEmmy          = 17,// set emmy env
};

#endif