---@class ClassWithClosureMethods
local ClassWithClosureMethods = {}

---@param tab table
---@return boolean
function ClassWithClosureMethods.regularMethod(tab)
    tab = <error descr="Type mismatch. Required: 'table' Found: '\"not a table\"'">"not a table"</error>
    tab = {}
    return true
end

---@param tab table
---@return boolean
ClassWithClosureMethods.closureMethod = function(tab)
    tab = <error descr="Type mismatch. Required: 'table' Found: '\"not a table\"'">"not a table"</error>
    tab = {}
    return true
end

---@param tab table
---@return boolean
local function localRegularFunction(tab)
    tab = <error descr="Type mismatch. Required: 'table' Found: '\"not a table\"'">"not a table"</error>
    tab = {}
    return true
end

---@param tab table
---@return boolean
local localClosureFunction = function(tab)
    tab = <error descr="Type mismatch. Required: 'table' Found: '\"not a table\"'">"not a table"</error>
    tab = {}
    return true
end

---@type boolean
local aBoolean

---@type string
local aString

-- Usage inspections

aBoolean = ClassWithClosureMethods.regularMethod({})
aString = <error descr="Type mismatch. Required: 'string' Found: 'boolean'">ClassWithClosureMethods.regularMethod({})</error>
aBoolean = ClassWithClosureMethods.regularMethod(<error descr="Type mismatch for argument: tab. Required: 'table' Found: '\"not a table\"'">"not a table"</error>)

aBoolean = ClassWithClosureMethods.closureMethod({})
aString = <error descr="Type mismatch. Required: 'string' Found: 'boolean'">ClassWithClosureMethods.closureMethod({})</error>
aBoolean = ClassWithClosureMethods.closureMethod(<error descr="Type mismatch for argument: tab. Required: 'table' Found: '\"not a table\"'">"not a table"</error>)

aBoolean = localRegularFunction({})
aString = <error descr="Type mismatch. Required: 'string' Found: 'boolean'">localRegularFunction({})</error>
aBoolean = localRegularFunction(<error descr="Type mismatch for argument: tab. Required: 'table' Found: '\"not a table\"'">"not a table"</error>)

aBoolean = localClosureFunction({})
aString = <error descr="Type mismatch. Required: 'string' Found: 'boolean'">localClosureFunction({})</error>
aBoolean = localClosureFunction(<error descr="Type mismatch for argument: tab. Required: 'table' Found: '\"not a table\"'">"not a table"</error>)

---@param param string
---@return string
local closureInternalTypeInspections = function(param)
    ---@type string
    local a
    a = param

    ---@type number
    local b
    b = <error descr="Type mismatch. Required: 'number' Found: 'string'">param</error>

    param = "okay"
    param = <error descr="Type mismatch. Required: 'string' Found: '1'">1</error>

    --@type fun(): boolean
    local sometimes

    if sometimes() then
        return "a string"
    else
        <error descr="Type mismatch. Expected: 'string' Found: '1'">return 1</error>
    end
end
