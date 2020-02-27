---@class ClassWithOverloads
local ClassWithOverloads = {}

--- Returns true if `tab` only contains sequential positive integer keys.
---@overload fun(tab: any[]): true
---@overload fun(tab: table): false
---@param tab table
---@return boolean
function ClassWithOverloads.isArray(tab)
    local i = 0
    for _ in pairs(tab) do
        i = i + 1
        if tab[i] == nil then return false end
    end
    return true
end

--- Returns true if `tab` only contains sequential positive integer keys.
---@overload fun(tab: any[]): true
---@overload fun(tab: table): false
---@param tab table
---@return boolean
ClassWithOverloads.isArrayFromClosure = function(tab)
    local i = 0
    for _ in pairs(tab) do
        i = i + 1
        if tab[i] == nil then return false end
    end
    return true
end

--- Returns true if `tab` only contains sequential positive integer keys.
---@overload fun(tab: any[]): true
---@overload fun(tab: table): false
---@param tab table
---@return boolean
local function isArray(tab)
    local i = 0
    for _ in pairs(tab) do
        i = i + 1
        if tab[i] == nil then return false end
    end
    return true
end

--- Returns true if `tab` only contains sequential positive integer keys.
---@overload fun(tab: any[]): true
---@overload fun(tab: table): false
---@param tab table
---@return boolean
local isArrayFromClosure = function(tab)
    local i = 0
    for _ in pairs(tab) do
        i = i + 1
        if tab[i] == nil then return false end
    end
    return true
end

---@type true
local t

---@type false
local f

t = ClassWithOverloads.isArray({1, 2, 3})
t = ClassWithOverloads.isArrayFromClosure({1, 2, 3})
t = isArray({1, 2, 3})
t = isArrayFromClosure({1, 2, 3})

f = ClassWithOverloads.isArray({1, 2, 3}) -- Expect error
f = ClassWithOverloads.isArrayFromClosure({1, 2, 3}) -- Expect error
f = isArray({1, 2, 3}) -- Expect error
f = isArrayFromClosure({1, 2, 3}) -- Expect error

f = ClassWithOverloads.isArray({one = 1, two = 2, three = 3})
f = ClassWithOverloads.isArrayFromClosure({one = 1, two = 2, three = 3})
f = isArray({one = 1, two = 2, three = 3})
f = isArrayFromClosure({one = 1, two = 2, three = 3})

t = ClassWithOverloads.isArray({one = 1, two = 2, three = 3}) -- Expect error
t = ClassWithOverloads.isArrayFromClosure({one = 1, two = 2, three = 3}) -- Expect error
t = isArray({one = 1, two = 2, three = 3}) -- Expect error
t = isArrayFromClosure({one = 1, two = 2, three = 3}) -- Expect error
