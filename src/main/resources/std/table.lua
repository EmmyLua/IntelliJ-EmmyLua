-- Copyright (c) 2018. tangzx(love.tangzx@qq.com)
--
-- Licensed under the Apache License, Version 2.0 (the "License"); you may not
-- use this file except in compliance with the License. You may obtain a copy of
-- the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
-- WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
-- License for the specific language governing permissions and limitations under
-- the License.

table = {}

---
--- Given a list where all elements are strings or numbers, returns the string
--- `list[i]..sep..list[i+1] ... sep..list[j]`. The default value for
--- `sep` is the empty string, the default for `i` is 1, and the default for
--- `j` is #list. If `i` is greater than `j`, returns the empty string.
---@overload fun(t:table, sep:string):string
---@param list table
---@param optional sep string
---@param optional i number
---@param optional j number
---@return string
function table.concat(list, sep, i, j) end

---
--- Inserts element `value` at position `pos` in `list`, shifting up the
--- elements to `list[pos]`, `list[pos+1]`, `···`, `list[#list]`. The default
--- value for `pos` is ``#list+1`, so that a call `table.insert(t,x)`` inserts
--- `x` at the end of list `t`.
---@overload fun(t:table, pos:number, value:any):number
---@param list table
---@param optional pos number
---@param value any
---@return number
function table.insert(list, pos, value) end

---
--- Moves elements from table a1 to table `a2`, performing the equivalent to
--- the following multiple assignment: `a2[t]`,`··· = a1[f]`,`···,a1[e]`. The
--- default for `a2` is `a1`. The destination range can overlap with the source
--- range. The number of elements to be moved must fit in a Lua integer.
---
--- Returns the destination table `a2`.
---@param a1 table
---@param f number
---@param e number
---@param t number
---@param optional a2 table
---@return table
function table.move(a1, f, e, t, a2) end

---
--- Returns a new table with all arguments stored into keys 1, 2, etc. and
--- with a field "`n`" with the total number of arguments. Note that the
--- resulting table may not be a sequence, if some arguments are **nil**.
---@return table
function table.pack(···) end

---
--- Removes from `table` the element at position `pos`, shifting down other
--- elements to close the space, if necessary. Returns the value of the removed
--- element. The default value for `pos` is `n`, where `n` is the length of the
--- table, so that a call `table.remove(t)` removes the last element of table
--- `t`.
---@overload fun(t:table, pos:number):any
---@param t table
---@param optional pos number
---@return any
function table.remove(t, pos) end

---
--- Sorts table elements in a given order, *in-place*, from `table[1]` to
--- `table[n]`, where `n` is the length of the table. If `comp` is given,
--- then it must be a function that receives two table elements, and returns
--- true when the first is less than the second in the final order  (so that,
--- after the `sort`, `i < j` implies `not comp(a[i+1], a[i])`. If comp is
--- not given, then the standard Lua operator `<` is used instead.
---
--- Note that the comp function must define a strict partial order over the
--- elements in the list; that is, it must be asymmetric and transitive.
--- Otherwise, no valid sort may be possible.
---
--- The sort algorithm is not stable: elements considered equal by the given
--- order may have their relative positions changed by the sort.
---@param t table
---@param optional comp fun(a:any, b:any):number
---@return number
function table.sort(t, comp) end

---
--- Returns the elements from the given list. This function is equivalent to
---
--- return `list[i]`, `list[i+1]`, `···`, `list[j]`
--- By default, i is 1 and j is #list.
---@param list table
---@param optional i number
---@param optional j number
---@return number
function table.unpack(list, i, j) end

return table
