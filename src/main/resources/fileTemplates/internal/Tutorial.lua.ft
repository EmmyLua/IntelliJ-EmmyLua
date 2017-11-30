-- Tutorial for annotations
-- https://emmylua.github.io
-- https://github.com/EmmyLua

--------------------------------------------------------------------------------
-- Tutorial for annotation "@class" and "@field"

---define a class
---@class Person
---@field public age number
---@field weight number
local Person = {}

function Person:sayHello()
    print('Hi!')
end

---define class Emmy
---@class Emmy : Person
local tbl = {
    name = "Emmy",
    ["*love daddy*"] = true
}

function tbl:sayHello()
    print('Hi! I am Emmy.')
end
--------------------------------------------------------------------------------
-- Tutorial for annotation "@type"
---@type Emmy
local inst1
-- type "inst1." here

---@type Emmy[]
local array = {}

local inst2 = array[1]
-- type "inst2." here

for i, emmy in ipairs(array) do
    -- type "emmy." here
end

---@type table<string, Emmy>
local dict = {}

local inst3 = dict['test']
-- type "inst3." here

for k, emmy in pairs(dict) do
    -- type "emmy." here
end

---@type fun(name:string):Emmy
local creatorFn
local inst4 = creatorFn('inst4')
-- type "inst4." here

--------------------------------------------------------------------------------
-- Tutorial for annotation "@return"
---@return Emmy
local function createEmmy()

end

local inst = createEmmy()

-- type "inst." here
-- inst.

--------------------------------------------------------------------------------
-- Tutorial for annotation "@param"

---@param emmy Emmy
local function testHello(emmy)
    -- type "emmy:" here
end

local list = {}
---@param emmy Emmy
for i, emmy in ipairs(list) do
    -- type "emmy:" here
end

---@param emmy Emmy
pcall(function(emmy)
    -- type "emmy:" here
end, inst)

--------------------------------------------------------------------------------