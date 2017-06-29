-- Copyright (c) 2017. tangzx(love.tangzx@qq.com)
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.


local toluaDebugger = {}

function toluaDebugger.GetValueAsText(ty, obj, depth, typeNameOverride, displayAsKey)
    if ty == 'userdata' then
        local getTab = obj[tolua.gettag]
        if getTab then
            local tableNode = toluaDebugger.RawGetValueAsText(obj, depth + 1, nil, false)
            if tableNode then
                for property, _ in pairs(getTab) do
                    local key = toluaDebugger.RawGetValueAsText(property, 0, nil, true)
                    local value = toluaDebugger.RawGetValueAsText(obj[property], depth, nil, false)
                    toluaDebugger.AddChildNode(tableNode, key, value)
                end
            end
            return tableNode
        end
    elseif ty == 'table' then
        --local clsName = obj.__cname
        --if clsName then
        --    return e.RawGetValueAsText(obj, depth + 1, clsName, displayAsKey)
        --end
    end
end

local xluaDebugger = {}
function xluaDebugger.GetValueAsText(ty, obj, depth, typeNameOverride, displayAsKey)
    if ty == 'userdata' then
        local CSType = obj:GetType()
        if CSType then
            local tableNode = xluaDebugger.RawGetValueAsText(obj, depth + 1, nil, false)

            local Type = CS.System.Type
            local ObsoleteType = Type.GetType('System.ObsoleteAttribute')
            local BindType = Type.GetType('System.Reflection.BindingFlags')
            local bindValue = CS.System.Enum.ToObject(BindType, 4157) -- 60 | 4096
            local properties = CSType:GetProperties(bindValue)
            for i = 1, properties.Length do
                local p = properties[i - 1]
                if CS.System.Attribute.GetCustomAttribute(p, ObsoleteType) == nil then
                    local property = p.Name
                    local value = obj[property]

                    local key = xluaDebugger.RawGetValueAsText(property, 0, nil, true)
                    local value = xluaDebugger.RawGetValueAsText(value, depth, nil, false)
                    xluaDebugger.AddChildNode(tableNode, key, value)
                end
            end

            return tableNode
        end
    end
end

if tolua then
    emmy = toluaDebugger
elseif xlua then
    emmy = xluaDebugger
end

if emmy_init then
    emmy_init()
end