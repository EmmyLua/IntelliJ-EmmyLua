.486                                    ; create 32 bit code
.model flat, stdcall                    ; 32 bit memory model
option casemap :none                    ; case sensitive

HookHandlerWorker PROTO C api:PTR DWORD, L:PTR DWORD, ar:PTR DWORD, _stdcall:PTR DWORD
CFunctionHandlerWorker PROTO C args:PTR DWORD, L:PTR DWORD, _stdcall:PTR DWORD

luaL_loadfilex_worker PROTO C api:PTR DWORD, L:PTR DWORD, fileName:DWORD, mode:PTR DWORD, _stdcall:PTR DWORD
luaL_newstate_worker PROTO C api:PTR DWORD, _stdcall:PTR DWORD
luaL_newmetatable_worker PROTO C api:PTR DWORD, L:PTR DWORD, tname:PTR DWORD, _stdcall:PTR DWORD

lua_newstate_worker PROTO C api:PTR DWORD, f:PTR DWORD, ud:PTR DWORD, _stdcall:PTR DWORD
lua_call_worker PROTO C api:PTR DWORD, L:PTR DWORD, nargs:DWORD, nresults:DWORD, _stdcall:PTR DWORD
lua_pcall_worker PROTO C api:PTR DWORD, L:PTR DWORD, nargs:DWORD, nresults:DWORD, errfunc:DWORD, _stdcall:PTR DWORD
lua_close_worker PROTO C api:PTR DWORD, L:PTR DWORD, _stdcall:PTR DWORD
.data

.code

HookHandler proc C api:DWORD, L:DWORD, ar:DWORD
    LOCAL _stdcall:DWORD

    mov _stdcall, 0
    invoke HookHandlerWorker, api, L, ar, addr _stdcall

    .if _stdcall == 0
        ret 4
    .else
        ret 4 + 4 * 3
    .endif
HookHandler endp

CFunctionHandler proc C args:DWORD, L:DWORD
    LOCAL _stdcall:DWORD

    mov _stdcall, 0
    invoke CFunctionHandlerWorker, args, L, addr _stdcall

    .if _stdcall == 0
        ret 4
    .else
        ret 4 + 4
    .endif
CFunctionHandler endp

luaL_loadfile_intercept proc C api:DWORD, L:DWORD, fileName:DWORD
    LOCAL _stdcall:DWORD
    mov _stdcall, 0
    invoke luaL_loadfilex_worker, api, L, fileName, 0, addr _stdcall
    ; 结果已经在eax中
    .if _stdcall == 0
        ret 4
    .else
        ret 4 + 4 * 3
    .endif
luaL_loadfile_intercept endp


luaL_newstate_intercept proc C api:DWORD
    LOCAL _stdcall:DWORD
    mov _stdcall, 0
    invoke luaL_newstate_worker, api, addr _stdcall
    ; 结果已经在eax中
    ret 4
luaL_newstate_intercept endp


lua_newstate_intercept proc C api:DWORD, f:DWORD, ud:DWORD
    LOCAL _stdcall:DWORD
    mov _stdcall, 0
    invoke lua_newstate_worker, api, f, ud, addr _stdcall
    ; 结果已经在eax中
    .if _stdcall == 0
        ret 4
    .else
        ret 4 + 8
    .endif
lua_newstate_intercept endp


luaL_newmetatable_intercept proc C api:DWORD, L:DWORD, tname:DWORD
    LOCAL _stdcall:DWORD
	mov _stdcall, 0
    invoke luaL_newmetatable_worker, api, L, tname, addr _stdcall
    ; 结果已经在eax中
    .if _stdcall == 0
        ret 4
    .else
        ret 4 + 8
    .endif
luaL_newmetatable_intercept endp


lua_call_intercept proc C api:DWORD, L:DWORD, nargs:DWORD, nresults:DWORD
    LOCAL _stdcall:DWORD
    mov _stdcall, 0
    invoke lua_call_worker, api, L, nargs, nresults, addr _stdcall
    .if _stdcall == 0
        ret 4
    .else
        ret 4 + 12
    .endif
lua_call_intercept endp


lua_pcall_intercept proc C api:DWORD, L:DWORD, nargs:DWORD, nresults:DWORD, errfunc:DWORD
    LOCAL _stdcall:DWORD
    mov _stdcall, 0
    invoke lua_pcall_worker, api, L, nargs, nresults, errfunc, addr _stdcall
    ; 结果已经在eax中
    .if _stdcall == 0
        ret 4
    .else
        ret 4 + 16
    .endif
lua_pcall_intercept endp


lua_close_intercept proc C api:DWORD, L:DWORD
    LOCAL _stdcall:DWORD
    mov _stdcall, 0
    invoke lua_close_worker, api, L, addr _stdcall
    ; 结果已经在eax中
    .if _stdcall == 0
        ret 4
    .else
        ret 4 + 4
    .endif
lua_close_intercept endp
end