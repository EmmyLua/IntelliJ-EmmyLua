---@class GenericA<A>
local GenericA

---@class GenericB<B> : GenericA<B>
local GenericB

---@class GenericC<C> : GenericB<C>s
local GenericC

---@type GenericA<string>
local genericA

---@type GenericB<string>
local genericB

---@type GenericC<string>
local genericC

genericA = genericB
genericA = genericC

genericB = <error>genericA</error>
genericB = genericC

genericC = genericA
genericC = genericB


---@class ClassA : GenericA<string>
local ClassA

---@class ClassB : ClassA
local ClassB

---@class ClassC : ClassB
local ClassC

---@type ClassA
local classA

---@type ClassB
local classB

---@type ClassC
local classC

classA = classB
classA = classC

classB = classA
classB = classC

classC = classA
classC = classB

genericA = classA
genericA = classB
genericA = classC


---@class ClassGenA<T>
local ClassGenA

---@class ClassGenB<T> : ClassGenA<string>
local ClassGenB

---@class ClassGenC<T> : ClassGenB<string>
local ClassGenC

---@type ClassGenA<string>
local classGenA

---@type ClassGenB<string>
local classGenB

---@type ClassGenC<string>
local classGenC

classGenA = classGenB
classGenA = classGenC

classGenB = classGenA
classGenB = classGenC

classGenC = classGenA
classGenC = classGenB
