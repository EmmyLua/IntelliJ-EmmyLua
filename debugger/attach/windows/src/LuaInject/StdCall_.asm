.486                                    ; create 32 bit code
.model flat, stdcall                    ; 32 bit memory model
option casemap :none                    ; case sensitive
GetIsStdCallConvention PROTO C :DWORD, :DWORD, :DWORD, :DWORD
.data

.code

; GetIsStdCallConvention1
GetIsStdCallConvention1 proc C function:DWORD, arg1:DWORD, result:DWORD
    LOCAL stackLeftOver:DWORD
    LOCAL final:DWORD
    ; Remember the stack pointer, so we can check if it got cleaned up.
    mov     stackLeftOver,  esp
    
    ; Call the function.
    push    arg1
    call    function

    ; Store the result.
    mov     final,          eax

    ; Compute if the arguments were left on the stack by the function.
    mov     eax,            stackLeftOver
    sub     eax,            esp
    mov     stackLeftOver,  eax

    ; Fix the stack.
    add     esp,            stackLeftOver

    ; if (result) *result = final
    cmp result, 0
    jz return
    mov eax, result
    mov ebx, final
    mov [eax], ebx
    
return:
    .if stackLeftOver == 0
        mov eax, 1
    .else
        mov eax, 0
    .endif

    ret
GetIsStdCallConvention1 endp

; GetIsStdCallConvention2
GetIsStdCallConvention2 proc C function:DWORD, arg1:DWORD, arg2:DWORD, result:DWORD
    LOCAL stackLeftOver:DWORD
    LOCAL final:DWORD
    ; Remember the stack pointer, so we can check if it got cleaned up.
    mov     stackLeftOver,  esp
    
    ; Call the function.
    push    arg2
    push    arg1
    call    function

    ; Store the result.
    mov     final,          eax

    ; Compute if the arguments were left on the stack by the function.
    mov     eax,            stackLeftOver
    sub     eax,            esp
    mov     stackLeftOver,  eax

    ; Fix the stack.
    add     esp,            stackLeftOver

    ; if (result) *result = final
    cmp result, 0
    jz return
    mov eax, result
    mov ebx, final
    mov [eax], ebx
    
return:
    .if stackLeftOver == 0
        mov eax, 1
    .else
        mov eax, 0
    .endif

    ret
GetIsStdCallConvention2 endp

; GetIsStdCallConvention3
GetIsStdCallConvention3 proc C function:DWORD, arg1:DWORD, arg2:DWORD, arg3:DWORD, result:DWORD
    LOCAL stackLeftOver:DWORD
    LOCAL final:DWORD
    ; Remember the stack pointer, so we can check if it got cleaned up.
    mov     stackLeftOver,  esp
    
    ; Call the function.
    push    arg3
    push    arg2
    push    arg1
    call    function

    ; Store the result.
    mov     final,          eax

    ; Compute if the arguments were left on the stack by the function.
    mov     eax,            stackLeftOver
    sub     eax,            esp
    mov     stackLeftOver,  eax

    ; Fix the stack.
    add     esp,            stackLeftOver

    ; if (result) *result = final
    cmp result, 0
    jz return
    mov eax, result
    mov ebx, final
    mov [eax], ebx
    
return:
    .if stackLeftOver == 0
        mov eax, 1
    .else
        mov eax, 0
    .endif

    ret
GetIsStdCallConvention3 endp

; GetIsStdCallConvention4
GetIsStdCallConvention4 proc C function:DWORD, arg1:DWORD, arg2:DWORD, arg3:DWORD, arg4:DWORD, result:DWORD
    LOCAL stackLeftOver:DWORD
    LOCAL final:DWORD
    ; Remember the stack pointer, so we can check if it got cleaned up.
    mov     stackLeftOver,  esp
    
    ; Call the function.
    push    arg4
    push    arg3
    push    arg2
    push    arg1
    call    function

    ; Store the result.
    mov     final,          eax

    ; Compute if the arguments were left on the stack by the function.
    mov     eax,            stackLeftOver
    sub     eax,            esp
    mov     stackLeftOver,  eax

    ; Fix the stack.
    add     esp,            stackLeftOver

    ; if (result) *result = final
    cmp result, 0
    jz return
    mov eax, result
    mov ebx, final
    mov [eax], ebx
    
return:
    .if stackLeftOver == 0
        mov eax, 1
    .else
        mov eax, 0
    .endif

    ret
GetIsStdCallConvention4 endp

; GetIsStdCallConvention5
GetIsStdCallConvention5 proc C function:DWORD, arg1:DWORD, arg2:DWORD, arg3:DWORD, arg4:DWORD, arg5:DWORD, result:DWORD
    LOCAL stackLeftOver:DWORD
    LOCAL final:DWORD
    ; Remember the stack pointer, so we can check if it got cleaned up.
    mov     stackLeftOver,  esp
    
    ; Call the function.
    push    arg5
    push    arg4
    push    arg3
    push    arg2
    push    arg1
    call    function

    ; Store the result.
    mov     final,          eax

    ; Compute if the arguments were left on the stack by the function.
    mov     eax,            stackLeftOver
    sub     eax,            esp
    mov     stackLeftOver,  eax

    ; Fix the stack.
    add     esp,            stackLeftOver

    ; if (result) *result = final
    cmp result, 0
    jz return
    mov eax, result
    mov ebx, final
    mov [eax], ebx
    
return:
    .if stackLeftOver == 0
        mov eax, 1
    .else
        mov eax, 0
    .endif

    ret
GetIsStdCallConvention5 endp


; GetIsStdCallConvention6
GetIsStdCallConvention6 proc C function:DWORD, arg1:DWORD, arg2:DWORD, arg3:DWORD, arg4:DWORD, arg5:DWORD, arg6:DWORD, result:DWORD
    LOCAL stackLeftOver:DWORD
    LOCAL final:DWORD
    ; Remember the stack pointer, so we can check if it got cleaned up.
    mov     stackLeftOver,  esp
    
    ; Call the function.
    push    arg6
    push    arg5
    push    arg4
    push    arg3
    push    arg2
    push    arg1
    call    function

    ; Store the result.
    mov     final,          eax

    ; Compute if the arguments were left on the stack by the function.
    mov     eax,            stackLeftOver
    sub     eax,            esp
    mov     stackLeftOver,  eax

    ; Fix the stack.
    add     esp,            stackLeftOver

    ; if (result) *result = final
    cmp result, 0
    jz return
    mov eax, result
    mov ebx, final
    mov [eax], ebx
    
return:
    .if stackLeftOver == 0
        mov eax, 1
    .else
        mov eax, 0
    .endif

    ret
GetIsStdCallConvention6 endp
end