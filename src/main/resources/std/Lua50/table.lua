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
---@overload fun(list:table):string
---@overload fun(list:table, sep:string):string
---@overload fun(list:table, sep:string, i:number):string
---@param list table
---@param sep string
---@param i number
---@param j number
---@return string
function table.concat(list, sep, i, j) end

--- Executes the given f over all elements of `table`. For each element, `f` is
--- called with the index and respective value as arguments. If `f` returns a
--- non-nil value, then the loop is broken, and this value is returned as the
--- final value of `foreach`.
--- See the `next` function for extra information about table traversals.
---
---@param table table
---@param f fun(key:any, value:any):any
function table.foreach (table, f) end

--- Executes the given `f` over the numerical indices of `table`. For each index,
--- `f` is called with the index and respective value as arguments. Indices are
--- visited in sequential order, from 1 to `n`, where `n` is the size of the `table`
--- (see 5.4). If `f` returns a non-nil value, then the loop is broken and this
--- value is returned as the result of `foreachi`.
---
---@param table table
---@param f fun(index:number, value:any):any
function table.foreachi(table, f) end

--- Returns the size of a table, when seen as a list. If the table has an n field
--- with a numeric value, this value is the size of the table. Otherwise, if there
--- was a previous call to table.setn over this table, the respective value is
--- returned. Otherwise, the size is one less the first integer index with a nil value.
---@param table table
---@return number
function table.getn(table) end

---
--- Inserts element `value` at position `pos` in `list`, shifting up the
--- elements to `list[pos]`, `list[pos+1]`, `···`, `list[#list]`. The default
--- value for `pos` is ``#list+1`, so that a call `table.insert(t,x)`` inserts
--- `x` at the end of list `t`.
---@overload fun(list:table, value:any):number
---@param list table
---@param pos number
---@param value any
---@return number
function table.insert(list, pos, value) end

---
--- Removes from `list` the element at position `pos`, returning the value of
--- the removed element. When `pos` is an integer between 1 and `#list`, it
--- shifts down the elements `list[pos+1]`, `list[pos+2]`, `···`,
--- `list[#list]` and erases element `list[#list]`; The index pos can also be 0
--- when `#list` is 0, or `#list` + 1; in those cases, the function erases
--- the element `list[pos]`.
---
--- The default value for `pos` is `#list`, so that a call `table.remove(l)`
--- removes the last element of list `l`.
---@overload fun(list:table):any
---@param list table
---@param pos number
---@return any
function table.remove(list, pos) end

---
--- Sorts list elements in a given order, *in-place*, from `list[1]` to
--- `list[#list]`. If `comp` is given, then it must be a function that receives
--- two list elements and returns true when the first element must come before
--- the second in the final order (so that, after the sort, `i < j` implies not
--- `comp(list[j],list[i]))`. If `comp` is not given, then the standard Lua
--- operator `<` is used instead.
---
--- Note that the `comp` function must define a strict partial order over the
--- elements in the list; that is, it must be asymmetric and transitive.
--- Otherwise, no valid sort may be possible.
---
--- The sort algorithm is not stable: elements considered equal by the given
--- order may have their relative positions changed by the sort.
---@generic V
---@overload fun(list:table):number
---@param list table<number, V>
---@param comp fun(a:V, b:V):number
---@return number
function table.sort(list, comp) end

--- Updates the size of a `table`. If the `table` has a field "n" with a numerical
--- value, that value is changed to the given `n`. Otherwise, it updates an internal
--- state so that subsequent calls to `table.getn(table)` return `n`.
---
---@param table table
---@param n number
function table.setn(table, n) end

return table
