---@param returnString boolean
---@return string|number
local function stringOrNumber(returnString)
    return returnString and "someString" or 1
end

---@param n number
function wantsNumber(n)
end

wantsNumber(stringOrNumber(false)) -- Type mismatch. Required: 'number' Found 'string|number'
wantsNumber(--[[---@type number]] stringOrNumber(false))

wantsNumber(
        ---@type number @Single line doc comments also work as type casts
        stringOrNumber(false)
)

wantsNumber(--[[---@type fun(): any]] 1) -- Type mismatch. Required: 'number' Found 'fun(): any'

---@param arr any[]
function wantsArray(arr)
end

local aString = "aString"

wantsArray(aString) -- Type mismatch. Required: 'any[]' Found 'string'
wantsArray(--[[--- @type any[] ]] aString) -- Trailing space used to separate array ']' from the block comment ']]'.
