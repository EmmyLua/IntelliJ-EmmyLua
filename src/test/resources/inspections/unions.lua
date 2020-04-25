---@type string|number
local stringOrNumberVar

---@param returnString boolean
---@return string|number
local function stringOrNumber(returnString)
    return returnString and "someString" or 1
end

---@type any
local unknown

stringOrNumberVar = "hi"
stringOrNumberVar = 7
stringOrNumberVar = stringOrNumber(unknown)

---@type "hi"|number
local stringLiteralOrNumber

stringOrNumberVar = stringLiteralOrNumber

stringLiteralOrNumber = <error>stringOrNumberVar</error>

---@type table<string, "A" | "B">
local aOrB

---@type table<string, "B" | "A">
local bOrA

---@type table<string, "A" | "C">
local aOrC

aOrB = bOrA
bOrA = aOrB
aOrB = <error>aOrC</error>
