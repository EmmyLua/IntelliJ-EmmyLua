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

math = {}

---
--- Returns the absolute value of `x`.
---@param x number
---@return number
function math.abs(x) return 0 end

---
--- Returns the arc cosine of `x` (in radians).
---@param x number
---@return number
function math.acos(x) return 0 end

---
--- Returns the arc sine of `x` (in radians).
---@param x number
---@return number
function math.asin(x) return 0 end

---
--- Returns the arc tangent of `x` (in radians).
---@param x number
---@return number
function math.atan(x) return 0 end

---
--- Returns the arc tangent of `y/x` (in radians), but uses the signs
--- of both parameters to find the quadrant of the result. (It also handles
--- correctly the case of `x` being zero.)
---@param y number
---@param x number
---@return number
function math.atan2(y, x) return 0 end

---
--- Returns the smallest integer larger than or equal to `x`.
---@param x number
---@return number
function math.ceil(x) return 0 end

---
--- Returns the cosine of `x` (assumed to be in radians).
---@param x number
---@return number
function math.cos(x) return 0 end

---
--- Returns the hyperbolic cosine of `x`.
---@param x number
---@return number
function math.cosh(x) return 0 end

---
--- Returns the angle `x` (given in radians) in degrees.
---@param x number
---@return number
function math.deg(x) return 0 end

---
--- Returns the value *e^x*.
---@param x number
---@return number
function math.exp(x) end

---
--- Returns the largest integer smaller than or equal to `x`.
---@param x number
---@return number
function math.floor(x) end

---
--- Returns the remainder of the division of `x` by `y` that rounds the
--- quotient towards zero.
---@param x number
---@param y number
---@return number
function math.fmod(x, y) end

---
--- Returns `m` and `e` such that *x = m2^e*, `e` is an integer and the
--- absolute value of `m` is in the range *[0.5, 1)* (or zero when `x` is zero).
---@param x number
---@return number
function math.frexp(x) end

---
--- The value `HUGE_VAL`, a value larger than or equal to any other
--- numerical value.
--- function math.huge end
--- * `math.HUGE_VAL`: math.HUGE_VAL

---
--- Returns *m2^e* (`e` should be an integer).
---@param m number
---@param e number
---@return number
function math.ldexp(m, e) end

---
--- Returns the natural logarithm of `x`.
---@param x number
---@return number
function math.log(x) end

---
--- Returns the base-10 logarithm of `x`.
---@param x number
---@return number
function math.log10(x) end

---
--- Returns the maximum value among its arguments.
---@param x number
---@return number
function math.max(x, ...) end

---
--- Returns the minimum value among its arguments.
---@param x number
---@return number
function math.min(x, ...) end

---
--- Returns two numbers, the integral part of `x` and the fractional part of
--- `x`.
---@param x number
---@return number, number
function math.modf(x) end

---
--- The value of *pi*.
--- function math.pi end
--- * `math.pi`: math.pi
---
--- Returns *x^y*. (You can also use the expression `x^y` to compute this
--- value.)
---@param x number
---@param y number
---@return number
function math.pow(x, y) end

---
--- Returns the angle `x` (given in degrees) in radians.
---@param x number
---@return number
function math.rad(x) end

---
--- This function is an interface to the simple pseudo-random generator
--- function `rand` provided by ANSI C. (No guarantees can be given for its
--- statistical properties.)
--- When called without arguments, returns a uniform pseudo-random real
--- number in the range *[0,1)*. When called with an integer number `m`,
--- `math.random` returns a uniform pseudo-random integer in the range *[1,
--- m]*. When called with two integer numbers `m` and `n`, `math.random`
--- returns a uniform pseudo-random integer in the range *[m, n]*.
---@overload fun():number
---@param m number
---@param n number
---@return number
function math.random(m, n) end

---
--- Sets `x` as the "seed" for the pseudo-random generator: equal seeds
--- produce equal sequences of numbers.
---@param x number
function math.randomseed(x) end

---
--- Returns the sine of `x` (assumed to be in radians).
---@param x number
---@return number
function math.sin(x) return 0 end

---
--- Returns the hyperbolic sine of `x`.
---@param x number
---@return number
function math.sinh(x) return 0 end

---
--- Returns the square root of `x`. (You can also use the expression `x^0.5`
--- to compute this value.)
---@param x number
---@return number
function math.sqrt(x) return 0 end

---
--- Returns the tangent of `x` (assumed to be in radians).
---@param x number
---@return number
function math.tan(x) return 0 end

---
--- Returns the hyperbolic tangent of `x`.
---@param x number
---@return number
function math.tanh(x) return 0 end

math.pi = 3.1415

return math


