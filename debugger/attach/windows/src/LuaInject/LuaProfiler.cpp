#include "LuaProfiler.h"

int idCounter = 0;

LPFunctionCall::LPFunctionCall(): id(idCounter++), line(0), count(0), time(0), isDirty(true), lastTime(0)
{
}
