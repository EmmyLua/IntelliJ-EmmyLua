---@class Unconstrained<T>
local Unconstrained = {}

---@class NumberConstrained<T : number>
local GenericB = {}

---@type Unconstrained<string>
local a

---@type Unconstrained<number>
local b

---@type NumberConstrained<number>
local c

---@type <error descr="Type mismatch. Required: '[local T]' Found: 'string'">NumberConstrained<string></error>
local d
