tab = {hi = 2}

for i, v in ipairs(--[[---@type any]] tab.hi) do
end

local upval = {}

function moo()
    ipairs(--[[---@type any]] upval)
end

shallow = {1, function(a) end, 3}
deep = {indeed = {hi = {1, 2, 3}}}

---@return boolean
function moo2()
    local innerMoo = 'indeed'
    moo():blarg(123123)
    ipairs(--[[---@type any]] deep[--[[---@type string]] innerMoo].hi[1])
    return true
end
