-- Copyright (c) 2020
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

bit32 = {}

---
--- Returns the number x shifted disp bits to the right. The number disp may be
--- any representable integer. Negative displacements shift to the left.
---
--- This shift operation is what is called arithmetic shift. Vacant bits on the
--- left are filled with copies of the higher bit of x; vacant bits on the right
--- are filled with zeros. In particular, displacements with absolute values
--- higher than 31 result in zero or 0xFFFFFFFF (all original bits are shifted
--- out).
---@param x number
---@param disp number
---@return number
function bit32.arshift(x, disp) end

---
--- Returns the bitwise and of its operands.
---@vararg number
---@return number
function bit32.band(...) end

---
--- Returns the bitwise negation of x. For any integer x, the following identity
--- holds:
---
---     assert(bit32.bnot(x) == (-1 - x) % 2^32)
---@param x number
---@return number
function bit32.bnot(x) end

---
--- Returns the bitwise or of its operands.
---@vararg number
---@return number
function bit32.bor(...) end

---
--- Returns a boolean signaling whether the bitwise and of its operands is different from zero.
---@vararg number
---@return boolean
function bit32.btest(...) end

---
--- Returns the bitwise exclusive or of its operands.
---@vararg number
---@return number
function bit32.bxor(...) end

---
--- Returns the unsigned number formed by the bits field to field + width - 1
--- from n. Bits are numbered from 0 (least significant) to 31 (most
--- significant). All accessed bits must be in the range [0, 31].
---
--- The default for width is 1.
---@overload fun(n:number, field:number):number
---@param n number
---@param field number
---@param width number
---@return number
function bit32.extract(n, field, width) end

---
--- Returns a copy of n with the bits field to field + width - 1 replaced by the
--- value v. See bit32.extract for details about field and width.
---@overload fun(n:number, v:number, field:number):number
---@param n number
---@param v number
---@param field number
---@param width number
---@return number
function bit32.replace(n, v, field, width) end

---
--- Returns the number x rotated disp bits to the left. The number disp may be
--- any representable integer.
---
--- For any valid displacement, the following identity holds:
---
---     assert(bit32.lrotate(x, disp) == bit32.lrotate(x, disp % 32))
---
--- In particular, negative displacements rotate to the right.
---@param x number
---@param disp number
---@return number
function bit32.lrotate(x, disp) end

---
--- Returns the number x shifted disp bits to the left. The number disp may be
--- any representable integer. Negative displacements shift to the right. In any
--- direction, vacant bits are filled with zeros. In particular, displacements
--- with absolute values higher than 31 result in zero (all bits are shifted
--- out).
---
--- For positive displacements, the following equality holds:
---
---     assert(bit32.lshift(b, disp) == (b * 2^disp) % 2^32)
---@param x number
---@param disp number
---@return number
function bit32.lshift(x, disp) end

---
--- Returns the number x rotated disp bits to the right. The number disp may be
--- any representable integer.
---
--- For any valid displacement, the following identity holds:
---
---     assert(bit32.rrotate(x, disp) == bit32.rrotate(x, disp % 32))
---
--- In particular, negative displacements rotate to the left.
---@param x number
---@param disp number
---@return number
function bit32.rrotate(x, disp) end

---
--- Returns the number x shifted disp bits to the right. The number disp may be
--- any representable integer. Negative displacements shift to the left. In any
--- direction, vacant bits are filled with zeros. In particular, displacements
--- with absolute values higher than 31 result in zero (all bits are shifted
--- out).
---
--- For positive displacements, the following equality holds:
---
---     assert(bit32.rshift(b, disp) == math.floor(b % 2^32 / 2^disp))
---
--- This shift operation is what is called logical shift.
---@param x number
---@param disp number
---@return number
function bit32.rshift(x, disp) end
