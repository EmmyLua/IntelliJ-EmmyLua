---@type number
local aNumber

---@class SelfA
---@field a string
local SelfA = {}

---@return self
function SelfA:colonMethod()
    ---@type self
    local selfTypedVar = self

    ---@type SelfA
    local someSelfA

    someSelfA = self
    self = <error descr="Type mismatch. Required: '' Found: 'SelfA'">someSelfA</error>
    selfTypedVar = <error descr="Type mismatch. Required: '' Found: 'SelfA'">someSelfA</error>
    aNumber = <error descr="Type mismatch. Required: 'number' Found: ''">self</error>

    return self
end

---@return self
function SelfA.dotMethod()
    ---@type self
    local selfTypedVar = self

    ---@type SelfA
    local someSelfA

    someSelfA = self
    self = <error descr="Type mismatch. Required: '' Found: 'SelfA'">someSelfA</error>
    selfTypedVar = <error descr="Type mismatch. Required: '' Found: 'SelfA'">someSelfA</error>
    aNumber = <error descr="Type mismatch. Required: 'number' Found: ''">self</error>

    return self
end

---@return self
SelfA.lambdaMethod = function()
    ---@type self
    local selfTypedVar = self

    ---@type SelfA
    local someSelfA

    someSelfA = self
    self = <error descr="Type mismatch. Required: '' Found: 'SelfA'">someSelfA</error>
    selfTypedVar = <error descr="Type mismatch. Required: '' Found: 'SelfA'">someSelfA</error>
    aNumber = <error descr="Type mismatch. Required: 'number' Found: ''">self</error>

    return self
end

---@type SelfA
local selfA

---@class SelfB : SelfA
---@field bb string
local SelfB = {}

---@type SelfB
local selfB

selfB = SelfB:colonMethod()
selfB = SelfB:dotMethod()
selfB = SelfB:lambdaMethod()

selfA = SelfB:colonMethod()
selfA = SelfB:dotMethod()
selfA = SelfB:lambdaMethod()

selfB = <error descr="Type mismatch. Required: 'SelfB' Found: 'SelfA'">SelfA:colonMethod()</error>
selfB = <error descr="Type mismatch. Required: 'SelfB' Found: 'SelfA'">SelfA:dotMethod()</error>
selfB = <error descr="Type mismatch. Required: 'SelfB' Found: 'SelfA'">SelfA:lambdaMethod()</error>

selfB = selfB:colonMethod()
selfB = selfB:dotMethod()
selfB = selfB:lambdaMethod()

selfA = selfB:colonMethod()
selfA = selfB:dotMethod()
selfA = selfB:lambdaMethod()

selfB = <error descr="Type mismatch. Required: 'SelfB' Found: 'SelfA'">selfA:colonMethod()</error>
selfB = <error descr="Type mismatch. Required: 'SelfB' Found: 'SelfA'">selfA:dotMethod()</error>
selfB = <error descr="Type mismatch. Required: 'SelfB' Found: 'SelfA'">selfA:lambdaMethod()</error>
