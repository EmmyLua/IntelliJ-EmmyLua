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

local function getMtAndTable(obj)
	local mt = getmetatable(obj)
	local tab
	if tolua.getpeer and type(tolua.getpeer) == 'function' then
		tab = tolua.getpeer(obj)
	else
		tab = mt[tolua.gettag]
	end
	return mt, tab
end

function toluaDebugger.GetValueAsText(ty, obj, depth, typeNameOverride, displayAsKey)
    if ty == 'userdata' then
        if depth <= 1 then return nil end
        local mt, tab = getMtAndTable(obj)
        if mt == nil then return nil end
        local tableNode = toluaDebugger.RawGetValueAsText(obj, depth, nil, false)
        if tableNode == nil then return nil end

        local propMap = {}
        while mt ~= nil and tab ~= nil do
            for property, _ in pairs(tab) do
                if not propMap[property] then
                    propMap[property] = true
                    local key = toluaDebugger.RawGetValueAsText(property, 0, nil, true)
                    local value = toluaDebugger.RawGetValueAsText(obj[property], depth - 1, nil, false)
                    toluaDebugger.AddChildNode(tableNode, key, value)
                end
            end
            mt, tab = getMtAndTable(mt)
        end
        return tableNode
    end
end

local xluaDebugger = {}
function xluaDebugger.GetValueAsText(ty, obj, depth, typeNameOverride, displayAsKey)
    if ty == 'userdata' then
        local mt = getmetatable(obj)
        if mt == nil or depth <= 1 then return nil end

        local CSType = obj:GetType()
        if CSType then
            local tableNode = xluaDebugger.RawGetValueAsText(obj, depth, nil, false)

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
                    local value = xluaDebugger.RawGetValueAsText(value, depth - 1, nil, false)
                    xluaDebugger.AddChildNode(tableNode, key, value)
                end
            end

            return tableNode
        end
    end
end

emmy = {}

if tolua then
    emmy = toluaDebugger
elseif xlua then
    emmy = xluaDebugger
end

function emmy.Reload(fileName)
    local a, b, c = string.find(fileName, '%.lua')
    if a then
        fileName = string.sub(fileName, 1, a - 1)
    end

    emmy.DebugLog('Try reload : ' .. fileName, 1)
    local searchers = package.searchers or package.loaders
    for _, load in ipairs(searchers) do
        local result = load(fileName)
        if type(result) == 'function' then
            break
        end
    end
end

if emmy_init then
    emmy_init()
end