/*
 * Copyright (c) 2020
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tang.intellij.lua.lang.type

import java.math.BigDecimal
import kotlin.math.pow

class LuaNumber(var value: BigDecimal = BigDecimal(0)) {
	companion object {

		/**
		 * Parses a string into Lua number literal.
		 * @param text string element
		 * @return parsed number, or null if text does not represent a valid Lua number.
		 */
		fun getValue(text: String): LuaNumber? {
			if (text.startsWith("0x")) {
				val longValue = text.substring(2).toLongOrNull(16)
				if (longValue != null) {
					return LuaNumber(longValue.toBigDecimal())
				}
			} else {
				val components = text.split('e', 'E')
				var doubleValue = components[0].toDoubleOrNull()

				if (doubleValue != null && components.size < 3) {
					if (components.size > 1) {
						val exponent = components[1].toIntOrNull() ?: return null
						doubleValue = doubleValue.pow(exponent)
					}

					return LuaNumber(doubleValue.toBigDecimal())
				}
			}

			return null
		}

		private val zeroDecimalRegex = Regex("""\.0$""")
	}

	override fun toString(): String {
		return value.toPlainString().replace(zeroDecimalRegex, "")
	}
}
