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

stringLiteralOrNumber = stringOrNumberVar -- Expect error as string is not a subtype of "hi" (a string literal).
