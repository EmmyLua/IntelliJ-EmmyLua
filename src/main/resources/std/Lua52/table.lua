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
---@overload fun(list:(string|number)[]):string
---@overload fun(list:(string|number)[], sep:string):string
---@overload fun(list:(string|number)[], sep:string, i:number):string
---@param list (string|number)[]
---@param sep string
---@param i number
---@param j number
---@return string
function table.concat(list, sep, i, j) end

---
--- Inserts element `value` at position `pos` in `list`, shifting up the
--- elements to `list[pos]`, `list[pos+1]`, `···`, `list[#list]`. The default
--- value for `pos` is ``#list+1`, so that a call `table.insert(t,x)`` inserts
--- `x` at the end of list `t`.
---@overload fun<T>(list:T[], value:T):void
---@generic T
---@param list T[]
---@param pos number
---@param value T
function table.insert(list, pos, value) end

---@shape std__Packed<T>
---@field [number] T
---@field n number

---
--- Returns a new table with all arguments stored into keys 1, 2, etc. and
--- with a field "`n`" with the total number of arguments. Note that the
--- resulting table may not be a sequence, if some arguments are **nil**.
---@generic T
---@vararg T
---@return std__Packed<T>
function table.pack(...) end

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
---@overload fun<V>(list:V[]):V
---@generic V
---@param list V[]
---@param pos number
---@return V
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
---@overload fun(list:V[]):number
---@generic V
---@param list V[]
---@param comp fun(a:V, b:V):boolean
---@return number
function table.sort(list, comp) end

---
--- Returns the elements from the given list. This function is equivalent to
--- return `list[i]`, `list[i+1]`, `···`, `list[j]`
--- By default, i is 1 and j is #list.
---@overload fun<T>(list: T[]): T...
---@overload fun<T>(list: T[], i: number): T...
---@overload fun<T>(list: std__Packed<T>): T...
---@overload fun<T>(list: std__Packed<T>, i: number): T...
---@generic T
---@param list T[]
---@param i number
---@param j number
---@return T...
function table.unpack(list, i, j) end

return table
