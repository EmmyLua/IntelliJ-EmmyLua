---@type any
local anything

---@type number
local anyNumber

---@type string
local anyString

---@type 1
local number1

---@type 2
local number2

---@type "string1"
local string1

---@type 1|"string1"
local number1OrString1

---@generic T
---@param arg T
local function fn(arg)
    -- T = anonymous type : void (inherits from void because we can make no assumptions about it)
    local var = arg

    anyNumber = <error descr="Type mismatch. Required: 'number' Found: 'T'">var</error>
    anyString = <error descr="Type mismatch. Required: 'string' Found: 'T'">var</error>
    anything = var
end

-- T = any
fn(anything)
fn(anyNumber)
fn(number1)
fn(anyString)
fn(string1)


---@generic T : number
---@param arg T
local function fn2(arg)
    -- T = anonymous type : number
    local var = arg

    anyNumber = var
    number1 = <error descr="Type mismatch. Required: '1' Found: 'T : number'">var</error>
    anyString = <error descr="Type mismatch. Required: 'string' Found: 'T : number'">var</error>
end

-- T = number
fn2(anything)
fn2(anyNumber)
fn2(number1)
fn2(<error descr="Type mismatch. Required: 'T : number' Found: 'string'">anyString</error>)
fn2(<error descr="Type mismatch. Required: 'T : number' Found: '\"string1\"'">string1</error>)




---@generic T
---@param arg1 T
---@param arg2 T
local function fn3(arg1, arg2) end

-- T = 1|2
fn3(number1, number2)

-- T = 1|"string1"
fn3(number1, string1)



---@generic T : number
---@param arg1 T
---@param arg2 T
local function fn4(arg1, arg2)
    -- T = anonymous type : number

    anyNumber = arg1
    anyNumber = arg2
    number1 = <error descr="Type mismatch. Required: '1' Found: 'T : number'">arg1</error>
    number1 = <error descr="Type mismatch. Required: '1' Found: 'T : number'">arg2</error>

    ---@type T
    local t

    t = arg1
    t = arg2

    arg1 = arg2
    arg2 = arg1
end

-- T = 1|2
fn4(number1, number2)

-- T = number
fn4(<error descr="Type mismatch. Required: 'T : number' Found: '1'">number1</error>, <error descr="Type mismatch. Required: 'T : number' Found: '\"string1\"'">string1</error>)



---@generic T
---@param arg T
---@return T
local function fn5(arg)
    return arg
end

-- T = typeof(arg)
string1 = fn5(string1)
anyString = fn5(string1)
number1 = fn5(number1)
number2 = fn5(number2)
number1 = <error descr="Type mismatch. Required: '1' Found: '2'">fn5(number2)</error>
anyNumber = fn5(number1)



---@generic T
---@param arg1 T
---@param arg2 T
------@return T
local function fn6(arg1, arg2)
    return arg1
end

-- T = 1|2
anything = fn6(number1, number2)
anyNumber = fn6(number1, number2)
anyString = <error descr="Type mismatch. Required: 'string' Found: '1|2'">fn6(number1, number2)</error>
number1 = <error descr="Type mismatch. Required: '1' Found: '1|2'">fn6(number1, number2)</error>
number2 = <error descr="Type mismatch. Required: '2' Found: '1|2'">fn6(number1, number2)</error>

-- T = 1|"string1"
anyNumber = <error descr="Type mismatch. Required: 'number' Found: '\"string1\"|1'">fn6(number1, string1)</error>
anyString = <error descr="Type mismatch. Required: 'string' Found: '\"string1\"|1'">fn6(number1, string1)</error>
number1OrString1 = fn6(number1, string1)

-- T = number
anything = fn6(number1, anyNumber)
anyNumber = fn6(number1, anyNumber)
anyString = <error descr="Type mismatch. Required: 'string' Found: 'number'">fn6(number1, anyNumber)</error>
number1 = <error descr="Type mismatch. Required: '1' Found: 'number'">fn6(number1, anyNumber)</error>
number2 = <error descr="Type mismatch. Required: '2' Found: 'number'">fn6(number1, anyNumber)</error>



---@generic T
---@param arg1 T
---@param arg2 T
---@param arg3 table<T, T>
------@return T
local function fn7(arg1, arg2, arg3)
    return arg1
end

---@type table<number, number>
local numberNumberTable

---@type table<1, 1>
local number1Number1Table

-- T = 1
anything = fn7(number1, number1, number1Number1Table)
anyNumber = fn7(number1, number1, number1Number1Table)
anyString = <error descr="Type mismatch. Required: 'string' Found: '1'">fn7(number1, number1, number1Number1Table)</error>
number1 = fn7(number1, number1, number1Number1Table)
number2 = <error descr="Type mismatch. Required: '2' Found: '1'">fn7(number1, number1, number1Number1Table)</error>

-- T = number
anything = fn7(number1, number2, numberNumberTable)
anyNumber = fn7(number1, number2, numberNumberTable)
anyString = <error descr="Type mismatch. Required: 'string' Found: 'number'">fn7(number1, number2, numberNumberTable)</error>
number1 = <error descr="Type mismatch. Required: '1' Found: 'number'">fn7(number1, number2, numberNumberTable)</error>
number2 = <error descr="Type mismatch. Required: '2' Found: 'number'">fn7(number1, number2, numberNumberTable)</error>

---@type number[]
local numberArray

-- T = number
anything = fn7(number1, number2, numberArray)
anyNumber = fn7(number1, number2, numberArray)
anyString = <error descr="Type mismatch. Required: 'string' Found: 'number'">fn7(number1, number2, numberArray)</error>
number1 = <error descr="Type mismatch. Required: '1' Found: 'number'">fn7(number1, number2, numberArray)</error>
number2 = <error descr="Type mismatch. Required: '2' Found: 'number'">fn7(number1, number2, numberArray)</error>
anyNumber = fn7(number1, number2, {3})
number1 = <error descr="Type mismatch. Required: '1' Found: 'number'">fn7(number1, number2, {3})</error>


---@type table<string|number, string|number>
local stringOrNumberStringOrNumberTable

---@type string|number
local stringOrNumber

-- T = commonAncestor(arg1, arg2) = number|string
anything = fn7(number1, string1, stringOrNumberStringOrNumberTable)
stringOrNumber = fn7(number1, string1, stringOrNumberStringOrNumberTable)
anyNumber = <error descr="Type mismatch. Required: 'number' Found: 'number|string'">fn7(number1, number2, stringOrNumberStringOrNumberTable)</error>
anyString = <error descr="Type mismatch. Required: 'string' Found: 'number|string'">fn7(number1, number2, stringOrNumberStringOrNumberTable)</error>


---@generic K, V
---@param arg1 K
---@param arg2 V
---@param arg3 table<K, V>
---@return table<K, V>
local function fn8(arg1, arg2, arg3)
    return arg3
end

---@type table<string, number>
local stringNumberTable

-- K = string, V = number
stringOrNumber = <error descr="Type mismatch. Required: 'number|string' Found: 'table<string, number>'">fn8(anyString, number1, stringNumberTable)</error>
stringNumberTable = fn8(anyString, number1, stringNumberTable)
stringNumberTable = fn8(string1, number1, stringNumberTable)
stringNumberTable = fn8(anyString, number1, <error descr="Type mismatch. Required: 'table<number|string, number>' Found: 'number[]'">numberArray</error>)
stringNumberTable = <error descr="Type mismatch. Required: 'table<string, number>' Found: 'table<number, number>'">fn8(anyNumber, number1, numberArray)</error>


---@generic K : string, T : table<K, string>
---@param arg1 K
---@param arg2 T
---@return T
local function fn9(arg1, arg2)
    return arg2
end

---@type table<string, string>
local stringStringTable

-- K = string, T = table<string, string>
anyString = <error descr="Type mismatch. Required: 'string' Found: 'table<string, string>'">fn9(anyString, stringStringTable)</error>
stringStringTable = fn9(anyString, stringStringTable)
stringStringTable = fn9(anyString, <error descr="Type mismatch. Required: 'T : table<K : string, string>' Found: 'table<string, number>'">stringNumberTable</error>)
stringStringTable = fn9(string1, stringStringTable)

---@type table<"string1", string>
local string1StringTable

-- K = "string1", T = table<"string1", string>
string1StringTable = fn9(string1, string1StringTable)
string1StringTable = <error descr="Type mismatch. Required: 'table<\"string1\", string>' Found: 'table<string, string>'">fn9(string1, stringStringTable)</error>
stringStringTable = <error descr="Type mismatch. Required: 'table<string, string>' Found: 'table<\"string1\", string>'">fn9(string1, string1StringTable)</error>

---@generic T : boolean
local function fn10()
    ---@generic <error descr="Generic parameters cannot be shadowed, 'T' was previously defined on line 250">T : string</error>
    local function fn10Nested(arg)
    end
end


---@type fun<K, V>(tab: table<K, V>, func: fun(key: K, value: V))
local fn11

-- K = 1|2,
fn11({a = 1, b = 2}, function(key, value)
    anyString = key
    anyNumber = <error descr="Type mismatch. Required: 'number' Found: '\"a\"|\"b\"'">key</error>
    anyString = <error descr="Type mismatch. Required: 'string' Found: '1|2'">value</error>
    anyNumber = value
end)

fn11(stringNumberTable, function(key, value)
    anyString = key
    anyNumber = <error descr="Type mismatch. Required: 'number' Found: 'string'">key</error>
    anyString = <error descr="Type mismatch. Required: 'string' Found: 'number'">value</error>
    anyNumber = value
end)

fn11({a = "a", b = "b"}, function(key, value)
    anyString = key
    anyNumber = <error descr="Type mismatch. Required: 'number' Found: '\"a\"|\"b\"'">key</error>
    anyString = value
    anyNumber = <error descr="Type mismatch. Required: 'number' Found: '\"a\"|\"b\"'">value</error>
end)

fn11(stringStringTable, function(key, value)
    anyString = key
    anyNumber = <error descr="Type mismatch. Required: 'number' Found: 'string'">key</error>
    anyString = value
    anyNumber = <error descr="Type mismatch. Required: 'number' Found: 'string'">value</error>
end)

fn11({[1] = "a", [2] = "b"}, function(key, value)
    anyString = <error descr="Type mismatch. Required: 'string' Found: '1|2'">key</error>
    anyNumber = key
    anyString = value
    anyNumber = <error descr="Type mismatch. Required: 'number' Found: '\"a\"|\"b\"'">value</error>
end)

fn11({"a", "b"}, function(key, value)
    anyString = <error descr="Type mismatch. Required: 'string' Found: 'number'">key</error>
    anyNumber = key
    anyString = value
    anyNumber = <error descr="Type mismatch. Required: 'number' Found: '\"a\"|\"b\"'">value</error>
end)

fn11(numberArray, function(key, value)
    anyString = <error descr="Type mismatch. Required: 'string' Found: 'number'">key</error>
    anyNumber = key
    anyString = <error descr="Type mismatch. Required: 'string' Found: 'number'">value</error>
    anyNumber = value
end)


---@type 1
local one

---@type 3
local three


---@generic K, V
---@param a table<K, V>
---@param b table<K, V>
---@return table<K, V>
local function merge(a, b)
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


---@type fun<K, V>(a: table<K, V>, b: table<K, V>): table<K, V>
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


---@overload fun<K, V>(a: table<K, V>, b: table<K, V>): table<K, V>
local function overloadMerge(a, b)
end

local overloadMergedLiteralArr = overloadMerge({1, 2}, {3, 4})
local overloadMergedLiteralMap = overloadMerge({a = 1, b = 2}, {c = 3, d = 4})

overloadMergedLiteralArr[1] = one
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
