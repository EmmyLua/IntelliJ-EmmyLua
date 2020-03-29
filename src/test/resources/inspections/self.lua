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
    self = <error descr="Type mismatch. Required: '[local self]' Found: 'SelfA'">someSelfA</error>
    selfTypedVar = <error descr="Type mismatch. Required: '[local self]' Found: 'SelfA'">someSelfA</error>
    aNumber = <error descr="Type mismatch. Required: 'number' Found: '[local self]'">self</error>

    aNumber = <error descr="Type mismatch. Required: 'number' Found: 'string'">self.a</error>
    aString = self.a

    return self
end

---@return self
function SelfA.dotMethod()
    ---@type self
    local selfTypedVar = <error descr="Type mismatch. Required: '[local self]' Found: 'SelfA|[global self]'">self</error>

    ---@type SelfA
    local someSelfA

    someSelfA = <error descr="Type mismatch. Required: 'SelfA' Found: 'SelfA|[global self]'">self</error>
    self = someSelfA
    selfTypedVar = <error descr="Type mismatch. Required: '[local self]' Found: 'SelfA'">someSelfA</error>
    aNumber = <error descr="Type mismatch. Required: 'number' Found: 'SelfA|[global self]'">self</error>

    aNumber = <error descr="Type mismatch. Required: 'number' Found: 'string|nil'">self.a</error>
    aString = self.a

    return <error descr="Type mismatch. Required: '[local self]' Found: 'SelfA|[global self]'">self</error>
end

---@return self
SelfA.lambdaMethod = function()
    ---@type self
    local selfTypedVar = <error descr="Type mismatch. Required: '[local self]' Found: 'SelfA|[global self]'">self</error>

    ---@type SelfA
    local someSelfA

    someSelfA = <error descr="Type mismatch. Required: 'SelfA' Found: 'SelfA|[global self]'">self</error>
    self = someSelfA
    selfTypedVar = <error descr="Type mismatch. Required: '[local self]' Found: 'SelfA'">someSelfA</error>
    aNumber = <error descr="Type mismatch. Required: 'number' Found: 'SelfA|[global self]'">self</error>

    aNumber = <error descr="Type mismatch. Required: 'number' Found: 'string|nil'">self.a</error>
    aString = self.a

    return <error descr="Type mismatch. Required: '[local self]' Found: 'SelfA|[global self]'">self</error>
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
