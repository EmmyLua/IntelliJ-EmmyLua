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
classWithAField, aNumber = <error descr="Type mismatch. Required: 'ClassWithAField' Found: 'number'"><error descr="Type mismatch. Required: 'number' Found: 'ClassWithAField'">multipleReturns()</error></error>
aNumber, classWithAField, <error descr="Too many assignees, will be assigned nil.">aString</error> = multipleReturns()
aNumber = <weak_warning descr="Insufficient assignees, values will be discarded.">multipleReturns()</weak_warning>
aNumber, aString = multipleReturns(), "some string"
aString, aNumber = "some string", <weak_warning descr="Insufficient assignees, values will be discarded.">multipleReturns()</weak_warning>

local implicitNumber, implicitClassWithAField = multipleReturns()


aNumber = implicitNumber
classWithAField = implicitClassWithAField

local a, b, <error descr="Too many assignees, will be assigned nil.">c</error> = multipleReturns()
local d = <weak_warning descr="Insufficient assignees, values will be discarded.">multipleReturns()</weak_warning>
