---@param num number
local function wantsNumber(num) end

---@type number
local aNumber

---@type string
local aString

---@type any
local unknown

wantsNumber(aNumber)
wantsNumber(<error descr="Type mismatch. Required: 'number' Found: 'string'">aString</error>)
wantsNumber(unknown)

aNumber = aNumber
aNumber = <error descr="Type mismatch. Required: 'number' Found: 'string'">aString</error>
aNumber = unknown

aString = aString
aString = <error descr="Type mismatch. Required: 'string' Found: 'number'">aNumber</error>
aString = unknown

unknown = unknown
unknown = aNumber
unknown = aString
