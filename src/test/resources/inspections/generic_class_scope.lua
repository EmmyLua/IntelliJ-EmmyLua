---@class GenericInMethod<T>
---@field a T
local GenericInMethod = {}

---@param arg T
function GenericInMethod:doThing(arg)
    ---@type T
    local thing

    thing = self.a
end

---@generic <error desc="Generic parameters cannot be shadowed, 'T' was previously defined on line 1">T</error>
---@param arg T
function GenericInMethod.doNotShadow(arg)
end

---@generic <error desc="Generic parameters cannot be shadowed, 'T' was previously defined on line 1">T</error>
---@param arg T
GenericInMethod.doNotShadow = function(arg)
end
