---@type fun(numberParam: number, vararg boolean)
local varargFunction

varargFunction(1, true, true)
varargFunction(1, true, true, <error descr="Type mismatch. Required: 'boolean' Found: '4'">4</error>, true)

---@type fun(numberParam: number, ...: boolean)
local varargFunction2

varargFunction2(1, true, true)
varargFunction2(1, true, true, <error descr="Type mismatch. Required: 'boolean' Found: '4'">4</error>, true)

---@param numberParam number
---@vararg boolean
local function varargFunction3(numberParam, ...)
end

varargFunction3(1, true, true)
varargFunction3(1, true, true, <error descr="Type mismatch. Required: 'boolean' Found: '4'">4</error>, true)

---@type fun(vararg boolean)
local varargFunction4

varargFunction4(true, true)
varargFunction4(true, true, <error descr="Type mismatch. Required: 'boolean' Found: '4'">4</error>, true)
varargFunction4(<error descr="Type mismatch. Required: 'boolean' Found: '1'">1</error>, true)

---@type fun(...: boolean)
local varargFunction5

varargFunction5(true, true)
varargFunction5(true, true, <error descr="Type mismatch. Required: 'boolean' Found: '4'">4</error>, true)
varargFunction5(<error descr="Type mismatch. Required: 'boolean' Found: '1'">1</error>, true)

---@vararg boolean
local function varargFunction6(...)
end

varargFunction6(true, true)
varargFunction6(true, true, <error descr="Type mismatch. Required: 'boolean' Found: '4'">4</error>, true)
varargFunction6(<error descr="Type mismatch. Required: 'boolean' Found: '1'">1</error>, true)

---@type number
local aNumber

---@type boolean
local aBoolean

---@type fun<T>(index: number, vararg T): T
local genericVarargFunction

aNumber = genericVarargFunction(1, 1, 2, 3)
aNumber = <error descr="Type mismatch. Required: 'number' Found: 'boolean'">genericVarargFunction(1, true, false, true)</error>
aBoolean = <error descr="Type mismatch. Required: 'boolean' Found: '1|2|3'">genericVarargFunction(1, 1, 2, 3)</error>
aBoolean = genericVarargFunction(1, true, false, true)

----@type fun<T>(index: number, ...: T): T
local genericVarargFunction2

aNumber = genericVarargFunction2(1, 1, 2, 3)
aNumber = <error descr="Type mismatch. Required: 'number' Found: 'boolean'">genericVarargFunction2(1, true, false, true)</error>
aBoolean = <error descr="Type mismatch. Required: 'boolean' Found: '1|2|3'">genericVarargFunction2(1, 1, 2, 3)</error>
aBoolean = genericVarargFunction2(1, true, false, true)

---@generic T
---@param index number
---@vararg T
---@return T
local function genericVarargFunction3(index, ...)
    return table.unpack({...}, index, 1)
end

aNumber = genericVarargFunction3(1, 1, 2, 3)
aNumber = <error descr="Type mismatch. Required: 'number' Found: 'boolean'">genericVarargFunction3(1, true, false, true)</error>
aBoolean = <error descr="Type mismatch. Required: 'boolean' Found: '1|2|3'">genericVarargFunction3(1, 1, 2, 3)</error>
aBoolean = genericVarargFunction3(1, true, false, true)
