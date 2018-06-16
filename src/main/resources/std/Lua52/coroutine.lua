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

coroutine = {}

---
--- Creates a new coroutine, with body `f`. `f` must be a Lua
--- function. Returns this new coroutine, an object with type `"thread"`.
---@return thread
function coroutine.create(f) end

---
--- Starts or continues the execution of coroutine `co`. The first time
--- you resume a coroutine, it starts running its body. The values
--- ... are passed as the arguments to the body function. If the coroutine
--- has yielded, `resume` restarts it; the values  ... are passed
--- as the results from the yield.
--- If the coroutine runs without any errors, `resume` returns true plus any
--- values passed to `yield` (if the coroutine yields) or any values returned
--- by the body function (if the coroutine terminates). If there is any error,
--- `resume` returns false plus the error message.
---@param co thread
function coroutine.resume(co, ...) end

---
--- Returns the running coroutine. Or nil when called by the main thread.
---@return thread
function coroutine.running() end

---
--- Returns the status of coroutine `co`. Result is a string: `"running"`, if
--- the coroutine is running (that is, it called `status`); `"suspended"`, if
--- the coroutine is suspended in a call to `yield`, or if it has not started
--- running yet; `"normal"` if the coroutine is active but not running (that
--- is, it has resumed another coroutine); and `"dead"` if the coroutine has
--- finished its body function, or if it has stopped with an error.
---@param co thread
function coroutine.status(co) end

---
--- Creates a new coroutine, with body `f`. `f` must be a Lua
--- function. Returns a function that resumes the coroutine each time it is
--- called. Any arguments passed to the function behave as the extra arguments to
--- `resume`. Returns the same values returned by `resume`, except the first
--- boolean. In case of error, propagates the error.
function coroutine.wrap(f) end

---
--- Suspends the execution of the calling coroutine. The coroutine cannot
--- be running a C function, a metamethod, or an iterator. Any arguments to
--- `yield` are passed as extra results to `resume`.
function coroutine.yield(...) end
