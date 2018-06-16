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

os = {}

---
--- Returns an approximation of the amount in seconds of CPU time used by
--- the program.
---@return number
function os.clock() end

---
--- Returns a string or a table containing date and time, formatted according
--- to the given string `format`.
---
--- If the `time` argument is present, this is the time to be formatted
--- (see the `os.time` function for a description of this value). Otherwise,
--- `date` formats the current time.
---
--- If `format` starts with '`!`', then the date is formatted in Coordinated
--- Universal Time. After this optional character, if `format` is the string
--- "`*t`", then `date` returns a table with the following fields:
---
--- * `year` (four digits)
--- * `month` (1--12)
--- * `day` (1--31)
--- * `hour` (0--23)
--- * `min` (0--59)
--- * `sec` (0--61)
--- * `wday` (weekday, Sunday is 1)
--- * `yday` (day of the year)
--- * `isdst` (daylight saving flag, a boolean).
---
--- If `format` is not "`*t`", then `date` returns the date as a string,
--- formatted according to the same rules as the C function `strftime`.
--- When called without arguments, `date` returns a reasonable date and time
--- representation that depends on the host system and on the current locale
--- (that is, `os.date()` is equivalent to `os.date("%c")`).
---@param format string
---@param time number
---@return string|table
function os.date(format, time) end

---
--- Returns the number of seconds from time `t1` to time `t2`. In POSIX,
--- Windows, and some other systems, this value is exactly `t2`*-*`t1`.
---@param t2 string
---@param t1 string
---@return number
function os.difftime(t2, t1) end

---
--- This function is equivalent to the C function `system`. It passes
--- `command` to be executed by an operating system shell. It returns a status
--- code, which is system-dependent. If `command` is absent, then it returns
--- nonzero if a shell is available and zero otherwise.
---@param command string
---@return number
function os.execute(command) end

---
--- Calls the C function `exit`, with an optional `code`, to terminate the
--- host program. The default value for `code` is the success code.
---@overload fun():number
---@param code number
---@return number
function os.exit(code) end

---
--- Returns the value of the process environment variable `varname`, or
--- nil if the variable is not defined.
---@param varname string
---@return string
function os.getenv(varname) end

---
--- Deletes the file or directory with the given name. Directories must be
--- empty to be removed. If this function fails, it returns nil, plus a string
--- describing the error.
---@param filename string
---@return any, string
function os.remove(filename) end

---
--- Renames file or directory named `oldname` to `newname`. If this function
--- fails, it returns nil, plus a string describing the error.
---@param oldname string
---@param newname string
---@return any, string
function os.rename(oldname, newname) end

---
--- Sets the current locale of the program. `locale` is a string specifying
--- a locale; `category` is an optional string describing which category to
--- change: `"all"`, `"collate"`, `"ctype"`, `"monetary"`, `"numeric"`, or
--- `"time"`; the default category is `"all"`. The function returns the name
--- of the new locale, or nil if the request cannot be honored.
--- If `locale` is the empty string, the current locale is set to an
--- implementation-defined native locale. If `locale` is the string "`C`",
--- the current locale is set to the standard C locale.
--- When called with nil as the first argument, this function only returns
--- the name of the current locale for the given category.
---@overload fun(locale:string):void
---@param locale string
---@param category string
function os.setlocale(locale, category) end

---
--- Returns the current time when called without arguments, or a time
--- representing the date and time specified by the given table. This table
--- must have fields `year`, `month`, and `day`, and may have fields `hour`,
--- `min`, `sec`, and `isdst` (for a description of these fields, see the
--- `os.date` function).
--- The returned value is a number, whose meaning depends on your system. In
--- POSIX, Windows, and some other systems, this number counts the number
--- of seconds since some given start time (the "epoch"). In other systems,
--- the meaning is not specified, and the number returned by `time` can be
--- used only as an argument to `date` and `difftime`.
---@param table table
---@return number
function os.time(table) end

---
--- Returns a string with a file name that can be used for a temporary
--- file. The file must be explicitly opened before its use and explicitly
--- removed when no longer needed.
--- On some systems (POSIX), this function also creates a file with that
--- name, to avoid security risks. (Someone else might create the file with
--- wrong permissions in the time between getting the name and creating the
--- file.) You still have to open the file to use it and to remove it (even
--- if you do not use it).
--- When possible, you may prefer to use `io.tmpfile`, which automatically
--- removes the file when the program ends.
---@return string
function os.tmpname() end

return os
