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
fn2(<error descr="Type mismatch for argument: arg. Required: 'T : number' Found: 'string'">anyString</error>)
fn2(<error descr="Type mismatch for argument: arg. Required: 'T : number' Found: '\"string1\"'">string1</error>)




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
fn4(<error descr="Type mismatch for argument: arg1. Required: 'T : number' Found: '1'">number1</error>, <error descr="Type mismatch for argument: arg2. Required: 'T : number' Found: '\"string1\"'">string1</error>)



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
anyNumber = <error descr="Type mismatch. Required: 'number' Found: '1|\"string1\"'">fn6(number1, string1)</error>
anyString = <error descr="Type mismatch. Required: 'string' Found: '1|\"string1\"'">fn6(number1, string1)</error>
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
anyNumber = <error descr="Type mismatch. Required: 'number' Found: 'string|number'">fn7(number1, number2, stringOrNumberStringOrNumberTable)</error>
anyString = <error descr="Type mismatch. Required: 'string' Found: 'string|number'">fn7(number1, number2, stringOrNumberStringOrNumberTable)</error>


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
stringOrNumber = <error descr="Type mismatch. Required: 'string|number' Found: 'table<string, number>'">fn8(anyString, number1, stringNumberTable)</error>
stringNumberTable = fn8(anyString, number1, stringNumberTable)
stringNumberTable = fn8(string1, number1, stringNumberTable)
stringNumberTable = fn8(anyString, number1, <error descr="Type mismatch for argument: arg3. Required: 'table<string|number, number>' Found: 'number[]'">numberArray</error>)
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
stringStringTable = fn9(anyString, <error descr="Type mismatch for argument: arg2. Required: 'T : table<K : string, string>' Found: 'table<string, number>'">stringNumberTable</error>)
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
