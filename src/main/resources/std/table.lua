-- Copyright (c) 2017. tangzx(love.tangzx@qq.com)
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.

table = {}

---
--- Given an array where all elements are strings or numbers, returns
--- `table[i]..sep..table[i+1] ... sep..table[j]`. The default value for
--- `sep` is the empty string, the default for `i` is 1, and the default for
--- `j` is the length of the table. If `i` is greater than `j`, returns the
--- empty string.
---@overload fun(t:table, sep:string):string
---@param t table
---@param sep string
---@param i number
---@param j number
---@return string
function table.concat(t, sep, i, j) end

---
--- Inserts element `value` at position `pos` in `table`, shifting up
--- other elements to open space, if necessary. The default value for `pos` is
--- `n+1`, where `n` is the length of the table (see §2.5.5), so that a call
--- `table.insert(t,x)` inserts `x` at the end of table `t`.
---@overload fun(t:table, value:any):void
---@param t table
---@param pos number
---@param value any
function table.insert(t, pos, value) end

---
--- Returns the largest positive numerical index of the given table, or
--- zero if the table has no positive numerical indices. (To do its job this
--- function does a linear traversal of the whole table.)
---@param t table
---@return string
function table.maxn(t) return 0 end

---
--- Removes from `table` the element at position `pos`, shifting down other
--- elements to close the space, if necessary. Returns the value of the removed
--- element. The default value for `pos` is `n`, where `n` is the length of the
--- table, so that a call `table.remove(t)` removes the last element of table
--- `t`.
---@overload fun(t:table):void
---@param t table
---@param pos number
function table.remove(t, pos) end

---
--- Sorts table elements in a given order,
--- *in-place*, from `table[1]` to `table[n]`, where `n` is the length of the
--- table. If `comp` is given, then it must be a function that receives two
--- table elements, and returns true when the first is less than the second
--- (so that `not comp(a[i+1],a[i])` will be true after the sort). If `comp`
--- is not given, then the '<' operator will be used.
---@param t table
---@param comp fun(a:any, b:any):number
function table.sort(t, comp) end

return table
