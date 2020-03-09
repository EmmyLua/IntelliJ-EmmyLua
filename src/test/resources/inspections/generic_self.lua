---@type number
local aNumber

---@type string
local aString

---@class GenericSelfA<T : string>
---@field a T
local GenericSelfA = {}

---@return self
function GenericSelfA:colonMethod()
    ---@type self
    local selfTypedVar = self

    ---@type GenericSelfA<string>
    local someGenericSelfA

    someGenericSelfA = <error descr="Type mismatch. Required: 'GenericSelfA<string>' Found: '[local self]'">self</error>
    self = <error descr="Type mismatch. Required: '[local self]' Found: 'GenericSelfA<string>'">someSelfA</error>
    selfTypedVar = <error descr="Type mismatch. Required: '[local self]' Found: 'GenericSelfA<string>' ">someSelfA</error>
    aNumber = <error descr="Type mismatch. Required: 'number' Found: '[local self]'">self</error>

    aNumber = <error descr="Type mismatch. Required: 'number' Found: 'T : string'">self.a</error>
    aString = self.a

    return self
end

---@return self
function GenericSelfA.dotMethod()
    ---@type self
    local selfTypedVar = self

    ---@type GenericSelfA<string>
    local someGenericSelfA

    someGenericSelfA = <error descr="Type mismatch. Required: 'GenericSelfA<string>' Found: '[local self]'">self</error>
    self = <error descr="Type mismatch. Required: '[local self]' Found: 'GenericSelfA<string>'">someSelfA</error>
    selfTypedVar = <error descr="Type mismatch. Required: '[local self]' Found: 'GenericSelfA<string>' ">someSelfA</error>
    aNumber = <error descr="Type mismatch. Required: 'number' Found: '[local self]'">self</error>

    aNumber = <error descr="Type mismatch. Required: 'number' Found: 'T : string'">self.a</error>
    aString = self.a

    return self
end

---@return self
GenericSelfA.lambdaMethod = function()
    ---@type self
    local selfTypedVar = self

    ---@type GenericSelfA<string>
    local someGenericSelfA

    someGenericSelfA = <error descr="Type mismatch. Required: 'GenericSelfA<string>' Found: '[local self]'">self</error>
    self = <error descr="Type mismatch. Required: '[local self]' Found: 'GenericSelfA<string>'">someSelfA</error>
    selfTypedVar = <error descr="Type mismatch. Required: '[local self]' Found: 'GenericSelfA<string>' ">someSelfA</error>
    aNumber = <error descr="Type mismatch. Required: 'number' Found: '[local self]'">self</error>

    aNumber = <error descr="Type mismatch. Required: 'number' Found: 'T : string'">self.a</error>
    aString = self.a

    return self
end

---@type GenericSelfA<string>
local selfAString

---@class GenericSelfB<T : string> : GenericSelfA<T>
---@field b T
local GenericSelfB = {}

---@type GenericSelfB<string>
local selfBString

---@type GenericSelfB<"string literal">
local selfBStringLiteral

selfBString = <error descr="Type mismatch. Required: 'GenericSelfB<string>' Found: 'GenericSelfB<T>'">GenericSelfB:colonMethod()</error>
selfBString = <error descr="Type mismatch. Required: 'GenericSelfB<string>' Found: 'GenericSelfB<T>'">GenericSelfB:dotMethod()</error>
selfBString = <error descr="Type mismatch. Required: 'GenericSelfB<string>' Found: 'GenericSelfB<T>'">GenericSelfB:lambdaMethod()</error>

selfAString = <error descr="Type mismatch. Required: 'GenericSelfA<string>' Found: 'GenericSelfA<T>'">GenericSelfA:colonMethod()</error>
selfAString = <error descr="Type mismatch. Required: 'GenericSelfA<string>' Found: 'GenericSelfA<T>'">GenericSelfA:dotMethod()</error>
selfAString = <error descr="Type mismatch. Required: 'GenericSelfA<string>' Found: 'GenericSelfA<T>'">GenericSelfA:lambdaMethod()</error>

selfBString = selfB:colonMethod()
selfBString = selfB:dotMethod()
selfBString = selfB:lambdaMethod()

selfAString = selfB:colonMethod()
selfAString = selfB:dotMethod()
selfAString = selfB:lambdaMethod()

selfBString = <error descr="Type mismatch. Required: 'GenericSelfB<string>' Found: 'GenericSelfA<string>'">selfAString:colonMethod()</error>
selfBString = <error descr="Type mismatch. Required: 'GenericSelfB<string>' Found: 'GenericSelfA<string>'">selfAString:dotMethod()</error>
selfBString = <error descr="Type mismatch. Required: 'GenericSelfB<string>' Found: 'GenericSelfA<string>'">selfAString:lambdaMethod()</error>

selfBStringLiteral = selfBStringLiteral:colonMethod()
selfBStringLiteral = selfBStringLiteral:dotMethod()
selfBStringLiteral = selfBStringLiteral:lambdaMethod()

selfAString = <error descr="Type mismatch. Required: 'GenericSelfA<string>' Found: 'GenericSelfB<\"string literal\">'">selfBStringLiteral:colonMethod()
selfAString = <error descr="Type mismatch. Required: 'GenericSelfA<string>' Found: 'GenericSelfB<\"string literal\">'">selfBStringLiteral:dotMethod()
selfAString = <error descr="Type mismatch. Required: 'GenericSelfA<string>' Found: 'GenericSelfB<\"string literal\">'">selfBStringLiteral:lambdaMethod()

selfBString = <error descr="Type mismatch. Required: 'GenericSelfB<string>' Found: 'GenericSelfB<\"string literal\">'">selfBStringLiteral:colonMethod()
selfBString = <error descr="Type mismatch. Required: 'GenericSelfB<string>' Found: 'GenericSelfB<\"string literal\">'">selfBStringLiteral:dotMethod()
selfBString = <error descr="Type mismatch. Required: 'GenericSelfB<string>' Found: 'GenericSelfB<\"string literal\">'">selfBStringLiteral:lambdaMethod()
