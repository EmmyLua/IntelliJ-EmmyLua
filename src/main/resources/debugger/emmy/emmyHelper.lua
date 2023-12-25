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

---@class emmy
---@field createNode fun(): Variable
local emmy = {}

local function convertStringToHex(str)
    return (str:gsub('.', function (c)
        return string.format('%02X', string.byte(c))
    end))
end

local printableChars = {
    [8] = true,   -- Backspace
    [9] = true,   -- Horizontal tab
    [10] = true,  -- Line feed
    [12] = true,  -- Form feed
    [13] = true,  -- Carriage return
}

local function isPrintable(charNum)
    -- Check if charNum is in the range of printable ASCII characters
    -- or if it's one of the control characters considered printable
    return (charNum >= 32 and charNum <= 126) or printableChars[charNum] == true
end

local function convertHexToPrintableASCIIString(hexStr)
    return (hexStr:gsub('(%x%x)', function(h)
        local charNum = tonumber(h, 16)
        return isPrintable(charNum) and string.char(charNum) or "�"
    end))
end

local find = string.find
local function isValidUtf8AndPrintable(str)
    local i, len = 1, #str
    local isValidUtf8 = true

    while i <= len do
        local byte = string.byte(str, i)
        if i == find(str, "[%z\1-\127]", i) then
            if not isPrintable(byte) then
                return false
            end
            i = i + 1
        elseif i == find(str, "[\194-\223][\128-\191]", i) then
            i = i + 2
        elseif i == find(str,        "\224[\160-\191][\128-\191]", i)
                or i == find(str, "[\225-\236][\128-\191][\128-\191]", i)
                or i == find(str,        "\237[\128-\159][\128-\191]", i)
                or i == find(str, "[\238-\239][\128-\191][\128-\191]", i) then
            i = i + 3
        elseif i == find(str,        "\240[\144-\191][\128-\191][\128-\191]", i)
                or i == find(str, "[\241-\243][\128-\191][\128-\191][\128-\191]", i)
                or i == find(str,        "\244[\128-\143][\128-\191][\128-\191]", i) then
            i = i + 4
        else
            isValidUtf8 = false
            break
        end
    end

    return isValidUtf8
end

local EmmyStringOutputMode = {
    Auto = "Auto",
    Concise = "Concise",
    Complete = "Complete",
}

local function processNonPrintable(objStr)
    local hexStr = convertStringToHex(objStr)
    local printableASCIISStr = convertHexToPrintableASCIIString(hexStr)
    return string.format("[Hex]:%s  [ASCII]:%s  [tostring]:%s", hexStr, printableASCIISStr, objStr)
end

local function processString(objStr)
    local currentOutputMode = rawget(_G , "EmmyStringOutputMode")
    if currentOutputMode == nil then
        currentOutputMode = EmmyStringOutputMode.Auto
    end
    if currentOutputMode == EmmyStringOutputMode.Auto then
        if isValidUtf8AndPrintable(objStr) then
            return objStr
        else
            return processNonPrintable(objStr)
        end
    elseif currentOutputMode == EmmyStringOutputMode.Concise then
        return objStr
    elseif currentOutputMode == EmmyStringOutputMode.Complete then
        return processNonPrintable(objStr)
    else
        return string.format("Error: 'processString' function received an unimplemented 'StringOutputMode': %s. Please use one of the implemented modes: 'Auto'(0), 'Concise'(1), or 'Complete'(2).", tostring(currentOutputMode))
    end
end

---@class Variable
---@field query fun(self: Variable, obj: any, depth: number, queryHelper: boolean):void
---@field name string
---@field value string
---@field valueTypeName string

local toluaHelper = {
    ---@param variable Variable
    queryVariable = function(variable, obj, typeName, depth)
        if typeName == 'table' then
            local cname = rawget(obj, '__cname')
            if cname then
                variable:query(obj, depth)
                variable.valueTypeName = cname
                return true
            end
        elseif typeName == 'string' then
            variable.value = processString(tostring(obj))
            return true
        elseif typeName == 'userdata' then
            local mt = getmetatable(obj)
            if mt == nil then return false end

            variable.valueTypeName = 'C#'
            variable.value = tostring(obj)

            if depth > 1 then
                local parent = variable
                local propMap = {}
                while mt ~= nil do
                    local getTab = mt[tolua.gettag]
                    if getTab then
                        for property, _ in pairs(getTab) do
                            if not propMap[property] then
                                propMap[property] = true
                                local v = emmy.createNode()
                                v.name = property
                                v:query(obj[property], depth - 1, true)
                                parent:addChild(v)
                            end
                        end
                    end
                    mt = getmetatable(mt)
                    if mt then
                        local super = emmy.createNode()
                        super.name = "base"
                        super.value = mt[".name"]
                        super.valueType = 9
                        super.valueTypeName = "C#"
                        parent:addChild(super)
                        parent = super
                    end
                end
            end
            return true
        end
    end
}

local xluaDebugger = {
    queryVariable = function(variable, obj, typeName, depth)
        if typeName == 'userdata' then
            local mt = getmetatable(obj)
            if mt == nil then
                return false
            end

            local CSType = obj:GetType()
            if CSType then
                variable.valueTypeName = 'C#'
                variable.value = tostring(obj)--CSType.FullName

                if depth > 1 then
                    local Type = CS.System.Type
                    local ObsoleteType = Type.GetType('System.ObsoleteAttribute')
                    local BindType = Type.GetType('System.Reflection.BindingFlags')
                    local bindValue = CS.System.Enum.ToObject(BindType, 5174) -- Instance | Public | NonPublic | GetProperty | DeclaredOnly | GetField

                    local parent = variable
                    while CSType do
                        local properties = CSType:GetProperties(bindValue)
                        for i = 1, properties.Length do
                            local p = properties[i - 1]
                            if CS.System.Attribute.GetCustomAttribute(p, ObsoleteType) == nil then
                                local property = p.Name
                                local value = obj[property]

                                local v = emmy.createNode()
                                v.name = property
                                v:query(value, depth - 1, true)
                                parent:addChild(v)
                            end
                        end
                        local fields = CSType:GetFields(bindValue)
                        for i = 1, fields.Length do
                            local p = fields[i - 1]
                            if CS.System.Attribute.GetCustomAttribute(p, ObsoleteType) == nil then
                                local property = p.Name
                                local value = obj[property]

                                local v = emmy.createNode()
                                v.name = property
                                v:query(value, depth - 1, true)
                                parent:addChild(v)
                            end
                        end

                        CSType = CSType.BaseType
                        if CSType then
                            local super = emmy.createNode()
                            super.name = "base"
                            super.value = CSType.FullName
                            super.valueType = 9
                            super.valueTypeName = "C#"
                            parent:addChild(super)
                            parent = super
                        end
                    end
                end

                return true
            end
        elseif typeName == 'string' then
            variable.value = processString(tostring(obj))
            return true
        end
    end
}

local cocosLuaDebugger = {
    queryVariable = function(variable, obj, typeName, depth)
        if typeName == 'userdata' then
            local mt = getmetatable(obj)
            if mt == nil then return false end
            variable.valueTypeName = 'C++'
            variable.value = mt[".classname"]

            if depth > 1 then
                local parent = variable
                local propMap = {}
                while mt ~= nil do
                    for property, _ in pairs(mt) do
                        if not propMap[property] then
                            propMap[property] = true
                            local v = emmy.createNode()
                            v.name = property
                            v:query(obj[property], depth - 1, true)
                            parent:addChild(v)
                        end
                    end
                    mt = getmetatable(mt)
                    if mt then
                        local super = emmy.createNode()
                        super.name = "base"
                        super.value = mt[".classname"]
                        super.valueType = 9
                        super.valueTypeName = "C++"
                        parent:addChild(super)
                        parent = super
                    end
                end
            end
            return true
        elseif typeName == 'string' then
            variable.value = processString(tostring(obj))
            return true
        end
    end
}

if tolua then
    if tolua.gettag then
        emmy = toluaHelper
    else
        emmy = cocosLuaDebugger
    end
elseif xlua then
    emmy = xluaDebugger
end

local emmyHelper = rawget(_G, "emmyHelper")
if emmyHelper == nil then
    rawset(_G, 'emmyHelper', emmy)
else
    emmyHelper.queryVariable = emmy.queryVariable
end

local emmyHelperInit = rawget(_G, 'emmyHelperInit')
if emmyHelperInit then
    emmyHelperInit()
end