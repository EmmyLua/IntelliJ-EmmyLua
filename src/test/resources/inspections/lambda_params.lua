---@param param string
local closureParams = function(param)
    ---@type string
    local a
    a = param
end

---@return string
local closureReturn = function()
    return "moo"
end

---@param param string
local badClosureParams = function(param)
    ---@type number
    local a
    a = <error descr="Type mismatch. Required: 'number' Found: 'string'">param</error>
end

---@return string
local badClosureReturn = function()
    <error descr="Type mismatch. Expected: 'string' Found: '1'">return 1</error>
end
