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
numberNumberTable = <error descr="Type mismatch. Required: 'table<number, number>' Found: 'string[]'">stringArray</error>
numberArray = <error descr="Type mismatch. Required: 'number[]' Found: 'string[]'">stringArray</error>

numberArray = {1, 2, 3}
numberNumberTable = {1, 2, 3}

-- Widen literal tables
numberArray = <error descr="Type mismatch. Required: 'number[]' Found: 'table'">{[1] = 1, [3] = 3}</error>
numberNumberTable = {[1] = 1, [3] = 3}

numberArray = <error descr="Type mismatch. Required: 'number[]' Found: 'table'">{one = 1, two = 2, three = 3}</error>
numberNumberTable = <error descr="Type mismatch. Required: 'table<number, number>' Found: 'table'">{one = 1, two = 2, three = 3}</error>

stringArray = {"one", "two", "three"}
numberStringTable = {"one", "two", "three"}

-- Widen literal tables
stringArray = <error descr="Type mismatch. Required: 'string[]' Found: 'table'">{[1] = "one", [3] = "three"}</error>
numberStringTable = {[1] = "three", [3] = "three"}

---@param arg table<number, number>
local function wantsNumberNumberTable(arg) end

wantsNumberNumberTable(numberNumberTable)
wantsNumberNumberTable(<error descr="Type mismatch. Required: 'table<number, number>' Found: 'table<number, string>'">numberStringTable</error>)
wantsNumberNumberTable({[1] = 1, [3] = 3})
wantsNumberNumberTable({1, 2, 3})

---@type "stringLiteral"
local explictlyTypedLiteral

local tableAssignedWithLiteral = {
    a = "stringLiteral",
    b = "aDifferentStringLiteral"
}

explictlyTypedLiteral = tableAssignedWithLiteral.a
explictlyTypedLiteral = <error descr="Type mismatch. Required: '\"stringLiteral\"' Found: '\"aDifferentStringLiteral\"'">tableAssignedWithLiteral.b</error>

local tableAssignedAfterDeclaration = {}

tableAssignedAfterDeclaration.a = "stringLiteral"
tableAssignedAfterDeclaration.b = "aDifferentStringLiteral"

explictlyTypedLiteral = tableAssignedAfterDeclaration.a
explictlyTypedLiteral = <error descr="Type mismatch. Required: '\"stringLiteral\"' Found: '\"aDifferentStringLiteral\"'">tableAssignedAfterDeclaration.b</error>

---@type any
local anyValue

local tableWithoutEntries = {}

anyValue = tableWithoutEntries.<error descr="No such member 'keyThatDoesNotExist' found on type 'table'">keyThatDoesNotExist</error>
