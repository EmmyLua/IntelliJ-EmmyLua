local a ---@type number

---@param num number
local function wantsNumber(num) end

wantsNumber(a)

local b = "hello" ---@type number @Expect error
