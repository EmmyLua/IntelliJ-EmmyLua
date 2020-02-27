---@class GenericFieldsA<A>
---@field a A
local GenericFieldsA

---@class GenericFieldsB<B> : GenericFieldsA<B>
---@field b B
local GenericFieldsB

---@class GenericFieldsC<C> : GenericFieldsB<C>
---@field c C
local GenericFieldsC

---@type GenericFieldsA<string>
local genericA

---@type GenericFieldsB<string>
local genericB

---@type GenericFieldsC<string>
local genericC

---@type string
local aString

---@type number
local aNumber

aString = genericA.a
aNumber = <error descr="Type mismatch. Required: 'number' Found: 'string'">genericA.a</error>

aString = genericB.a
aNumber = <error descr="Type mismatch. Required: 'number' Found: 'string'">genericB.a</error>
aString = genericB.b
aNumber = <error descr="Type mismatch. Required: 'number' Found: 'string'">genericB.b</error>

aString = genericC.a
aNumber = <error descr="Type mismatch. Required: 'number' Found: 'string'">genericC.a</error>
aString = genericC.b
aNumber = <error descr="Type mismatch. Required: 'number' Found: 'string'">genericC.b</error>
aString = genericC.c
aNumber = <error descr="Type mismatch. Required: 'number' Found: 'string'">genericC.c</error>

aString = genericA.a
aString = <error descr="Type mismatch. Required: 'string' Found: 'nil'">genericA.b</error>
aString = <error descr="Type mismatch. Required: 'string' Found: 'nil'">genericA.c</error>
