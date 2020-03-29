---@class LambdaClass
local LambdaClass = {}

setmetatable(LambdaClass,  {
    ---@param a number
    __call = function(_, a)
        local self = --[[---@type LambdaClass]] {}

        ---@return number
        function self.getNumber()
            return 1
        end

        return self
    end
})

local lambdaClass = LambdaClass()

---@type number
local aNumber = lambdaClass.getNumber()

---@type string
local aString = <error descr="Type mismatch. Required: 'string' Found: 'number'">lambdaClass.getNumber()</error>
