---@type table
local implicitUnknown

pairs(implicitUnknown)

---@type table<any, any>
local explicitUnknown

pairs(explicitUnknown)

---@type table<number, number>
local numberNumberTable

---@type table<number, string>
local numberStringTable

---@type number[]
local numberArray

---@type string[]
local stringArray

numberNumberTable = numberArray
numberNumberTable = stringArray -- Expect error
numberArray = stringArray -- Expect error

numberArray = {1, 2, 3}
numberNumberTable = {1, 2, 3}

-- Widen literal tables
numberArray = {[1] = 1, [3] = 3} -- Expect error
numberNumberTable = {[1] = 1, [3] = 3}

numberArray = {one = 1, two = 2, three = 3} -- Expect error
numberNumberTable = {one = 1, two = 2, three = 3} -- Expect error

stringArray = {"one", "two", "three"}
numberStringTable = {"one", "two", "three"}

-- Widen literal tables
stringArray = {[1] = "one", [3] = "three"} -- Expect error
numberStringTable = {[1] = "three", [3] = "three"}

---@param arg table<number, number>
local function wantsNumberNumberTable(arg) end

wantsNumberNumberTable(numberNumberTable)
wantsNumberNumberTable(numberStringTable) -- Expect error
wantsNumberNumberTable({[1] = 1, [3] = 3})
wantsNumberNumberTable({1, 2, 3})

---@type "stringLiteral"
local explictlyTypedLiteral

local tableAssignedWithLiteral = {
    a = "stringLiteral",
    b = "aDifferentStringLiteral"
}

explictlyTypedLiteral = tableAssignedWithLiteral.a
explictlyTypedLiteral = tableAssignedWithLiteral.b -- Expect error

local tableAssignedAfterDeclaration = {}

tableAssignedAfterDeclaration.a = "stringLiteral"
tableAssignedAfterDeclaration.b = "aDifferentStringLiteral"

explictlyTypedLiteral = tableAssignedAfterDeclaration.a
explictlyTypedLiteral = tableAssignedAfterDeclaration.b -- Expect error

---@type any
local anyValue

local tableWithoutEntries = {}

anyValue = tableWithoutEntries.keyThatDoesNotExist -- Expect error
