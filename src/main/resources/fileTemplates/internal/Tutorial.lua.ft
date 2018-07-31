-- Tutorial for EmmyDoc
-- https://emmylua.github.io
-- https://github.com/EmmyLua

--------------------------------------------------------------------------------
-- Tutorial for annotation "@class" and "@field"

---define a class
---**Markdown** is available for comment strings
---@class Person comment string goes here
---@field public age number age value
---@field weight number
local Person = {}

function Person:sayHello()
    print('Hi!')
end

---define class Emmy
---@class Emmy : Person @`Emmy` inherited from `Person`
local EmmyClass = {
    name = "Emmy",
    ["*love daddy*"] = true
}

function EmmyClass:sayHello()
    print('Hi! I am Emmy.')
end

---@param book string
function EmmyClass:reading(book)
end

---define another class Daddy
---@class Daddy : Person
local DaddyClass = {
    name = 'Tang'
}

---@param code string
function DaddyClass:coding(code)
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
-- Tutorial for @generic



---@generic T : Person
---@param clazz T
---@return T
local function createPersonInstance(clazz)
    --type "clazz:" here
    -- todo return a instance of clazz
end

local emmy = createPersonInstance(EmmyClass)
-- type "emmy:" here
local daddy = createPersonInstance(DaddyClass)
-- type "daddy:" here



-- one more complex generic test
---@generic T : Person
---@param clazz T
---@param initHandler fun(person:T):void
---@return T
local function createPersonInstance2(clazz, initHandler)
    local instance = clazz() -- new clazz()
    initHandler(instance)
    return instance
end

local emmy2 = createPersonInstance2(EmmyClass, function(emmy)
    -- type "emmy:" here
end)
-- type "emmy2:" here

local daddy2 = createPersonInstance2(DaddyClass, function(daddy)
    -- type "daddy:" here
end)
-- type "daddy2:" here