---@class ClassWithAField
local ClassWithAField = { field = "name" }

---@return number, ClassWithAField
local function multipleReturns()
    ---@type ClassWithAField
    local res
    return 1, res
end

---@type number
local aNumber

---@type ClassWithAField
local classWithAField

---@type string
local aString

aNumber, classWithAField = multipleReturns()
<error descr="Type mismatch. Required: 'ClassWithAField' Found: 'number'">classWithAField</error>, <error descr="Type mismatch. Required: 'number' Found: 'ClassWithAField'">aNumber</error> = <error descr="Type mismatch. Required: 'ClassWithAField' Found: 'number'"><error descr="Type mismatch. Required: 'number' Found: 'ClassWithAField'">multipleReturns()</error></error>
aNumber, classWithAField, <error descr="Too many assignees, will be assigned nil.">aString</error> = multipleReturns()
aNumber = <weak_warning descr="Insufficient assignees, values will be discarded.">multipleReturns()</weak_warning>
aNumber, aString = multipleReturns(), "some string"
aString, aNumber = "some string", <weak_warning descr="Insufficient assignees, values will be discarded.">multipleReturns()</weak_warning>

local implicitNumber, implicitClassWithAField = multipleReturns()


aNumber = implicitNumber
classWithAField = implicitClassWithAField

local a, b, <error descr="Too many assignees, will be assigned nil.">c</error> = multipleReturns()
local d = <weak_warning descr="Insufficient assignees, values will be discarded.">multipleReturns()</weak_warning>

---@param arg1 number
---@param arg2 string
---@vararg boolean
local function acceptsNumberStringVariadicBoolean(arg1, arg2, ...) end

---@param arg1 number
---@param arg2 string
local function acceptsNumberString(arg1, arg2) end

---@return number, string, boolean...
local function returnsNumberStringVariadicBoolean()
    return 1, "a string", true
end

---@return string, boolean...
local function returnStringVariadicBoolean()
    return "a string", true
end

---@return number, string, boolean
local function returnsNumberStringBoolean()
    return 1, "a string", true
end

acceptsNumberStringVariadicBoolean(1, "a string", true)
acceptsNumberStringVariadicBoolean(returnsNumberStringVariadicBoolean())
acceptsNumberStringVariadicBoolean(returnsNumberStringVariadicBoolean(), <error descr="Type mismatch. Required: 'string' Found: 'true'">true</error>)
acceptsNumberStringVariadicBoolean(1, returnStringVariadicBoolean())
acceptsNumberString(<weak_warning descr="1 result is an excess argument.">returnsNumberStringVariadicBoolean()</weak_warning>)
acceptsNumberString(<weak_warning descr="1 result is an excess argument.">returnsNumberStringBoolean()</weak_warning>)
