---@class EmptyClass
local SimpleClass = {}

---@type EmptyClass
local emptyClass

---@class ClassWithFields
---@field a string
---@field b number
local ClassWithFields = {}

---@type ClassWithFields
local tablesAreNotClasses

tablesAreNotClasses.a = <error descr="Type mismatch. Required: 'string' Found: '1'">1</error>
tablesAreNotClasses.b = <error descr="Type mismatch. Required: 'number' Found: '\"someString\"'">"someString"</error>

tablesAreNotClasses.a = "someString"
tablesAreNotClasses.b = 1

tablesAreNotClasses.a = "someOtherString"
tablesAreNotClasses.b = 2

tablesAreNotClasses = <error descr="Type mismatch. Required: 'ClassWithFields' Found: 'table'">{
    a = "someString",
    b = 1
}</error>
