---@class GenericInMethod<T>
---@field a T
local GenericInMethod = {}

---@param arg T
function GenericInMethod:colonMethod(arg)
    ---@type T
    local thing

    thing = self.a
end

---@param arg T
function GenericInMethod.dotMethod(arg)
    ---@type T
    local thing

    thing = self.a
end

---@param arg T
GenericInMethod.lambdaMethod = function(arg)
    ---@type T
    local thing

    thing = self.a
end

---@generic <error descr="Generic parameters cannot be shadowed, 'T' was previously defined on line 1">T</error>
---@param arg T
function GenericInMethod:colonMethodShadow(arg)
end

---@generic <error descr="Generic parameters cannot be shadowed, 'T' was previously defined on line 1">T</error>
---@param arg T
function GenericInMethod:dotMethodShadow(arg)
end

---@generic <error descr="Generic parameters cannot be shadowed, 'T' was previously defined on line 1">T</error>
---@param arg T
GenericInMethod.lambdaMethodShadow = function(arg)
end
