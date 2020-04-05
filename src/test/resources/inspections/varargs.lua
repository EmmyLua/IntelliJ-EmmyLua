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


---@type 1
local one

---@type 3
local three

---@type table<string, string>
local stringStringTable

---@generic K, V
---@vararg table<K, V>
---@return table<K, V>
local function merge(...)
    ---@type table<K, V>
    local res

    return res
end

local mergedLiteralArr = merge({1, 2}, {3, 4})
local mergedLiteralMap = merge({a = 1, b = 2}, {c = 3, d = 4})

mergedLiteralArr[1] = one
mergedLiteralArr[1] = three
mergedLiteralArr[1] = <error descr="Type mismatch. Required: '1|2|3|4' Found: '5'">5</error>

mergedLiteralMap.a = one
mergedLiteralMap.a = three
mergedLiteralMap.a = <error descr="Type mismatch. Required: '1|2|3|4' Found: '5'">5</error>

<error descr="No such member 'e' found on type 'table<\"a\"|\"b\"|\"c\"|\"d\", 1|2|3|4>'">mergedLiteralMap.e</error> = <error descr="Type mismatch. Required: 'nil' Found: '1'">one</error>

local mergedStringStringMap = merge(stringStringTable, stringStringTable)

mergedStringStringMap.a = "a string"
mergedStringStringMap.a = <error descr="Type mismatch. Required: 'string' Found: '1'">1</error>
mergedStringStringMap['a'] = "a string"
mergedStringStringMap['a'] = <error descr="Type mismatch. Required: 'string' Found: '1'">1</error>


---@type fun<K, V>(...: table<K, V>): table<K, V>
local typeMerge

local typeMergedLiteralArr = typeMerge({1, 2}, {3, 4})
local typeMergedLiteralMap = typeMerge({a = 1, b = 2}, {c = 3, d = 4})

typeMergedLiteralArr[1] = one
typeMergedLiteralArr[1] = three
typeMergedLiteralArr[1] = <error descr="Type mismatch. Required: '1|2|3|4' Found: '5'">5</error>

typeMergedLiteralMap.a = one
typeMergedLiteralMap.a = three
typeMergedLiteralMap.a = <error descr="Type mismatch. Required: '1|2|3|4' Found: '5'">5</error>

<error descr="No such member 'e' found on type 'table<\"a\"|\"b\"|\"c\"|\"d\", 1|2|3|4>'">typeMergedLiteralMap.e</error> = <error descr="Type mismatch. Required: 'nil' Found: '1'">one</error>

local typeMergedStringStringMap = typeMerge(stringStringTable, stringStringTable)

typeMergedStringStringMap.a = "a string"
typeMergedStringStringMap.a = <error descr="Type mismatch. Required: 'string' Found: '1'">1</error>
typeMergedStringStringMap['a'] = "a string"
typeMergedStringStringMap['a'] = <error descr="Type mismatch. Required: 'string' Found: '1'">1</error>

---@overload fun<K, V>(...: table<K, V>): table<K, V>
local function overloadMerge(...)
end

local overloadMergedLiteralArr = overloadMerge({1, 2}, {3, 4})
local overloadMergedLiteralMap = overloadMerge({a = 1, b = 2}, {c = 3, d = 4})

overloadMergedLiteralArr[1 ] = one
overloadMergedLiteralArr[1] = three
overloadMergedLiteralArr[1] = <error descr="Type mismatch. Required: '1|2|3|4' Found: '5'">5</error>

overloadMergedLiteralMap.a = one
overloadMergedLiteralMap.a = three
overloadMergedLiteralMap.a = <error descr="Type mismatch. Required: '1|2|3|4' Found: '5'">5</error>

<error descr="No such member 'e' found on type 'table<\"a\"|\"b\"|\"c\"|\"d\", 1|2|3|4>'">overloadMergedLiteralMap.e</error> = <error descr="Type mismatch. Required: 'nil' Found: '1'">one</error>

local overloadMergedStringStringMap = overloadMerge(stringStringTable, stringStringTable)

overloadMergedStringStringMap.a = "a string"
overloadMergedStringStringMap.a = <error descr="Type mismatch. Required: 'string' Found: '1'">1</error>
overloadMergedStringStringMap['a'] = "a string"
overloadMergedStringStringMap['a'] = <error descr="Type mismatch. Required: 'string' Found: '1'">1</error>
