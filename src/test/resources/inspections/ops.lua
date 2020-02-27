---@type number
local aNumber

---@type string
local aString

---@type boolean
local aBoolean

---@type 1
local theNumberOne

---@type string|number
local aStringOrNumber

---@type true|1
local trueOrOne

---@type boolean
local unknownBoolean

---@type true
local trueLiteral

---@type false
local falseLiteral

-- or
theNumberOne = 1 or 1
theNumberOne = 1 or nil
theNumberOne = 1 or false
theNumberOne = 1 or true
theNumberOne = 1 or aBoolean
theNumberOne = nil or 1
theNumberOne = false or 1
theNumberOne = true or 1 -- Expect error
theNumberOne = aBoolean or 1 -- Expect error

-- and
theNumberOne = 1 and 1
theNumberOne = 1 and nil -- Expect error
theNumberOne = 1 and false -- Expect error
theNumberOne = 1 and true -- Expect error
theNumberOne = 1 and aBoolean -- Expect error
theNumberOne = nil and 1 -- Expect error
theNumberOne = false and 1 -- Expect error
theNumberOne = true and 1
theNumberOne = aBoolean and 1 -- Expect error

-- combinatorial
aStringOrNumber = unknownBoolean and "someString" or 1
aStringOrNumber = "someString" and unknownBoolean and "someString" or 1
aStringOrNumber = unknownBoolean or "someString" and 1 -- Expect error
aString = 1 and "someString" or unknownBoolean
trueOrOne = unknownBoolean or "someString" and 1

-- arithmetic
aNumber = theNumberOne + 1
theNumberOne = theNumberOne + 1 -- Expect error
aNumber = aStringOrNumber + 1 -- Expect error

aNumber = theNumberOne - 1
theNumberOne = theNumberOne - 1 -- Expect error
aNumber = aStringOrNumber + 1 -- Expect error

-- getn
aNumber = #{}
aString = #{} -- Expect error

-- not
trueLiteral = not false
trueLiteral = not true -- Expect error
trueLiteral = not unknownBoolean -- Expect error
falseLiteral = not true
falseLiteral = not false -- Expect error
falseLiteral = not unknownBoolean -- Expect error

-- minus
aNumber = -theNumberOne
theNumberOne = -theNumberOne -- Expect error
