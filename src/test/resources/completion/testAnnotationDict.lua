---@generic K, V
---@param t table<K, V>|V[]
---@return fun(tbl: table<K, V>):K, V
function pairs(t) end

---@type fun():table<string, Emmy>
local emmyCreator

for k, emmy in pairs(emmyCreator()) do
    emmy.<caret>
end