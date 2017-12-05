/*
 * Copyright (c) 2017. tangzx(love.tangzx@qq.com)
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

package com.tang.intellij.lua.unity.vfs

import at.pollaknet.api.facile.Facile
import at.pollaknet.api.facile.symtab.symbols.*
import com.intellij.openapi.vfs.impl.ArchiveHandler
import com.tang.intellij.lua.refactoring.LuaRefactoringUtil

private val TypeRef.luaType: String get() {
    val ns: String? = namespace
    val fullQName = if (ns == null || ns.isEmpty())
        name
    else
        "$ns.$name"

    val converted = typeConvertMap[fullQName]
    return converted ?: fullQName
}

private val obsoleteAttrSet = mutableSetOf(
        "ObsoleteAttribute",
        "MonoNotSupportedAttribute",
        "MonoTODOAttribute"
)
private val typeConvertMap = mapOf(
        "System.Single" to "number",
        "System.Int32" to "number",
        "System.UInt32" to "number",
        "System.Double" to "number",
        "System.Int16" to "number",
        "System.UInt16" to "number",
        "System.Int64" to "number",
        "System.UInt64" to "number",
        "System.Decimal" to "number",

        "System.Boolean" to "boolean",
        "System.String" to "string",
        "System.Object" to "table"
)

private val TypeRef.isValid: Boolean get() {
    if (namespace.isNotEmpty() && !LuaRefactoringUtil.isLuaIdentifier(namespace))
        return false
    if (!LuaRefactoringUtil.isLuaIdentifier(name))
        return false
    for (attribute in this.customAttributes) {
        if (obsoleteAttrSet.contains(attribute.typeRef.name)) {
            return false
        }
    }
    return true
}

private val Method.isPublic: Boolean get() = this.flags and Method.FLAGS_VISIBILITY_PUBLIC == Method.FLAGS_VISIBILITY_PUBLIC

private val Method.isStatic: Boolean get() = this.flags and Method.FLAGS_STATIC == Method.FLAGS_STATIC

private val Method.isValid: Boolean get() {
    return isPublic
            && LuaRefactoringUtil.isLuaIdentifier(name)
            && !isPropertyMethod
            && !isEventMethod
            && !name.startsWith("op_")
}

private val Property.isPublic: Boolean get() {
    return this.methods.any { it.isPublic }
}

private val Field.isPublic: Boolean get() = this.flags and Field.FLAGS_VISIBILITY_PUBLIC == Field.FLAGS_VISIBILITY_PUBLIC

class NetArchiveHandler(path: String) : ArchiveHandler(path) {

    private data class TypeData(val type: Type, val code: ByteArray)

    private val typeMap = mutableMapOf<String, TypeData>()
    private val nsMap = mutableMapOf<String, ByteArray>()

    override fun createEntriesMap(): MutableMap<String, EntryInfo> {
        typeMap.clear()
        nsMap.clear()

        val map = mutableMapOf<String, EntryInfo>()
        val rootEntry = createRootEntry()
        map.put("", rootEntry)
        val f = Facile.load(this.file.path)
        f.assembly.allTypes.forEach {
            if (it.isValid) {
                val ns = it.namespace
                if (ns.isNotEmpty()) {
                    val arr = ns.split(".")
                    if (arr.isNotEmpty()) {
                        var qua = ""
                        for (i in 0 until arr.size) {
                            qua = if (i == 0) arr[0] else "$qua.${arr[i]}"
                            if (!nsMap.containsKey(qua)) {
                                val code = "$qua = {}".toByteArray()
                                val fileName = "$qua.ns.lua"
                                nsMap[fileName] = code
                                map.put(fileName, EntryInfo(fileName, false, code.size.toLong(), DEFAULT_TIMESTAMP, rootEntry))
                            }
                        }
                    }
                }

                val code = decode(it).toByteArray()
                val fileName = if (ns.isNullOrEmpty()) "${it.name}.lua" else "$ns.${it.name}.lua"
                val entry = EntryInfo(fileName, false, code.size.toLong(), DEFAULT_TIMESTAMP, rootEntry)
                map.put(fileName, entry)
                typeMap.put(fileName, TypeData(it, code))
            }
        }
        return map
    }

    private fun decode(type: Type): String {
        val fieldSB = StringBuilder()
        val bodySB = StringBuilder()
        with(bodySB) {
            fieldSB.append("--- fields\n")
            type.fields.forEach {
                if (it.isPublic) {
                    fieldSB.append("---@field public ${it.name} ${it.typeRef.luaType}\n")
                }
            }
            fieldSB.append("--- properties\n")
            type.properties.forEach {
                if (it.isPublic) {
                    fieldSB.append("---@field public ${it.name} ${it.propertySignature.typeRef.luaType}\n")
                }
            }

            val extends: TypeRef? = type.extends
            if (extends != null)
                append("---@class ${type.luaType} : ${extends.luaType}\n")
            else
                append("---@class ${type.luaType}\n")
            append("local m = {}\n")

            type.methods.forEach {
                if (it.isValid) {
                    val methodName = it.name
                    val signature = it.methodSignature

                    append("\n")

                    //parameter docs
                    signature.parameters.forEach { par ->
                        append("---@param ${par.name} ${par.typeRef.luaType}\n")
                    }

                    //return
                    val ret = signature.returnType
                    if (ret.name != null)
                        append("---@return ${signature.returnType.luaType}\n")

                    //static or instance
                    if (it.isStatic)
                        append("function m.$methodName(")
                    else
                        append("function m:$methodName(")

                    //parameters
                    var parIndex = 0
                    signature.parameters.forEach { par ->
                        if (parIndex++ == 0)
                            append(par.name)
                        else
                            append(", ${par.name}")
                    }
                    append(")end\n")
                }
            }

            // UnityEngine.GameObject = m
            val ns = type.namespace
            if (ns.isNullOrEmpty())
                append("${type.name} = m\n")
            else
                append("$ns.${type.name} = m\n")

            append("return m")
        }

        return buildString {
            append(fieldSB)
            append(bodySB)
        }
    }

    override fun contentsToByteArray(relativePath: String): ByteArray {
        val nsCode = nsMap[relativePath]
        if (nsCode != null)
            return nsCode

        val typeData = typeMap[relativePath]
        if (typeData != null)
            return typeData.code
        return kotlin.ByteArray(0)
    }
}