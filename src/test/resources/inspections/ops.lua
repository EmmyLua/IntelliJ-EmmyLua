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
theNumberOne = <error descr="Type mismatch. Required: '1' Found: 'true'">true or 1</error>
theNumberOne = <error descr="Type mismatch. Required: '1' Found: 'true|1'">aBoolean or 1</error>

-- and
theNumberOne = 1 and 1
theNumberOne = <error descr="Type mismatch. Required: '1' Found: 'nil'">1 and nil</error>
theNumberOne = <error descr="Type mismatch. Required: '1' Found: 'false'">1 and false</error>
theNumberOne = <error descr="Type mismatch. Required: '1' Found: 'true'">1 and true</error>
theNumberOne = <error descr="Type mismatch. Required: '1' Found: 'boolean'">1 and aBoolean</error>
theNumberOne = <error descr="Type mismatch. Required: '1' Found: 'nil'">nil and 1</error>
theNumberOne = <error descr="Type mismatch. Required: '1' Found: 'false'">false and 1</error>
theNumberOne = true and 1
theNumberOne = <error descr="Type mismatch. Required: '1' Found: 'false|1'">aBoolean and 1</error>

-- combinatorial
aStringOrNumber = unknownBoolean and "someString" or 1
aStringOrNumber = "someString" and unknownBoolean and "someString" or 1
aStringOrNumber = <error descr="Type mismatch. Required: 'string|number' Found: 'true|1'">unknownBoolean or "someString" and 1</error>
aString = 1 and "someString" or unknownBoolean
trueOrOne = unknownBoolean or "someString" and 1

-- arithmetic
aNumber = theNumberOne + 1
theNumberOne = <error descr="Type mismatch. Required: '1' Found: 'number'">theNumberOne + 1</error>
aNumber = <error descr="Type mismatch. Required: 'number' Found: 'string|number'">aStringOrNumber + 1</error>

aNumber = theNumberOne - 1
theNumberOne = <error descr="Type mismatch. Required: '1' Found: 'number'">theNumberOne - 1</error>
aNumber = <error descr="Type mismatch. Required: 'number' Found: 'string|number'">aStringOrNumber + 1</error>

-- getn
aNumber = #{}
aString = <error descr="Type mismatch. Required: 'string' Found: 'number'">#{}</error>

-- not
trueLiteral = not false
trueLiteral = <error descr="Type mismatch. Required: 'true' Found: 'false'">not true</error>
trueLiteral = <error descr="Type mismatch. Required: 'true' Found: 'boolean'">not unknownBoolean</error>
falseLiteral = not true
falseLiteral = <error descr="Type mismatch. Required: 'false' Found: 'true'">not false</error>
falseLiteral = <error descr="Type mismatch. Required: 'false' Found: 'boolean'">not unknownBoolean</error>

-- minus
aNumber = -theNumberOne
theNumberOne = <error descr="Type mismatch. Required: '1' Found: 'number'">-theNumberOne</error>
