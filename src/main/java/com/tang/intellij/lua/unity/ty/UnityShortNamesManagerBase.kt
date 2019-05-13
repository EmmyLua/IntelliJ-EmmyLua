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

package com.tang.intellij.lua.unity.ty

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.impl.light.LightElement
import com.intellij.util.Processor
import com.intellij.util.containers.ContainerUtil
import com.tang.intellij.lua.lang.LuaLanguage
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.psi.search.LuaShortNamesManager
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.ty.*
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.EOFException
import java.io.InputStream
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.charset.Charset

abstract class UnityShortNamesManagerBase : LuaShortNamesManager() {
    enum class TypeKind {
        Class,
        Array,
    }

    private val myClassList = mutableListOf<UnityClass>()
    private val myClassMap = mutableMapOf<String, NsMember>()
    private var myRootNS: Namespace? = null
    private var myClient: SocketChannel? = null

    companion object {
        private val typeMap = mapOf(
                "System.String" to "string",
                "System.Boolean" to "boolean",
                "System.Single" to "number",
                "System.Double" to "number",
                "System.Int16" to "number",
                "System.Int32" to "number",
                "System.Int64" to "number",
                "System.SByte" to "number",
                "System.UInt16" to "number",
                "System.UInt32" to "number",
                "System.UInt64" to "number",
                "System.Void" to "void"
        )

        private fun convertType(type: String): String {
            return typeMap[type] ?: type
        }
    }

    protected abstract val project: Project

    private fun DataInputStream.readUTF8(): String {
        val len = readSize()
        val bytes = ByteArray(len)
        read(bytes)
        return String(bytes, Charset.defaultCharset())
    }

    private fun DataInputStream.readType(): ITy {
        val kind = readByte().toInt()
        return when (kind) {
            TypeKind.Array.ordinal -> { // array
                val base = readType()
                TyArray(base)
            }
            else -> {
                val type = readUTF8()
                Ty.create(convertType(type))
            }
        }
    }

    private fun InputStream.readSize(): Int {
        val ch1 = read()
        val ch2 = read()
        val ch3 = read()
        val ch4 = read()
        if (ch1 or ch2 or ch3 or ch4 < 0)
            throw EOFException()
        return (ch1 shl 0) + (ch2 shl 8) + (ch3 shl 16) + (ch4 shl 24)
    }

    protected fun createSocket() {
        val server = ServerSocketChannel.open()
        server.bind(InetSocketAddress(996))
        while (true) {
            val client = server.accept()
            if (this.myClient != null) {
                client.close() // only one client
                continue
            }
            val stream = client.socket().getInputStream()
            this.myClient = client
            ApplicationManager.getApplication().executeOnPooledThread {
                processClient(stream)
            }
        }
    }

    private fun processClient(stream: InputStream) {
        try {
            println("-- connected")
            while (true) {
                val streamSize = stream.readSize()
                val proto = stream.readSize()
                if (proto == 0) {
                    val bytes = ByteArray(streamSize - 8)
                    var read = 0
                    while (read < bytes.size)
                        read += stream.read(bytes, read, bytes.size - read)
                    parseLib(ByteArrayInputStream(bytes))
                    onParseLibrary()
                } else if (proto == 1) {
                    // pin
                }
            }
        } catch (e: Exception) {
            onClose()
        }
    }

    private fun parseLib(stream: InputStream) {
        val mgr = PsiManager.getInstance(project)
        val dataInputStream = DataInputStream(stream)
        reset()
        myRootNS = Namespace("root", null, mgr, false)
        while (dataInputStream.available() > 0) {
            val fullName = dataInputStream.readUTF8()
            if (fullName.isEmpty())
                break

            val hasBaseType = dataInputStream.readBoolean()
            var baseTypeFullName: String? = null
            if (hasBaseType) {
                baseTypeFullName = dataInputStream.readUTF8()
            }

            //println("class $fullName extends $baseTypeFullName")
            val aClass = createType(fullName, baseTypeFullName, mgr)

            // field list
            val fieldsCount = dataInputStream.readSize()
            for (i in 0 until fieldsCount) {
                val name = dataInputStream.readUTF8()
                val type = dataInputStream.readType()
                aClass.addMember(name, type)
                //println(">>> $name - $type")
            }
            // field list
            val properties = dataInputStream.readSize()
            for (i in 0 until properties) {
                val name = dataInputStream.readUTF8()
                val type = dataInputStream.readType()
                aClass.addMember(name, type)
            }
            // methods
            val methodCount = dataInputStream.readSize()
            for (i in 0 until methodCount) {
                val paramList = mutableListOf<LuaParamInfo>()

                // name
                val name = dataInputStream.readUTF8()
                val isStatic = dataInputStream.readBoolean()

                // parameters
                val parameterCount = dataInputStream.readSize()
                for (j in 0 until parameterCount) {
                    val pName = dataInputStream.readUTF8()
                    val pType = dataInputStream.readType()
                    paramList.add(LuaParamInfo(pName, pType))
                }
                // ret
                val retType = dataInputStream.readType()

                val ty = TySerializedFunction(FunSignature(!isStatic, retType, null, paramList.toTypedArray()), emptyArray())
                aClass.addMember(name, ty)
            }
        }
    }

    private fun createType(type: String, baseType: String?, mgr: PsiManager): UnityClass {
        val nsParts = type.split('.')
        var prev: Namespace? = myRootNS
        for (i in 0 until nsParts.size - 1) {
            val ns = nsParts[i]
            prev = prev?.getOrPut(ns)
            if (prev != null)
                myClassMap[prev.fullName] = prev
        }

        //class
        val aClass = UnityClass(nsParts.last(), type, baseType, prev, mgr)
        myClassList.add(aClass)
        myClassMap[type] = aClass
        prev?.addMember(aClass)

        return aClass
    }

    protected open fun onParseLibrary() {
    }

    protected fun close() {
        myClient?.close()
        myClient = null
    }

    private fun onClose() {
        println("-- closed")
        myClient = null
        reset()
    }

    private fun reset() {
        myRootNS = null
        myClassMap.clear()
        myClassList.clear()
    }

    private fun findClass(name: String): NsMember? {
        return myClassMap[name]
    }

    override fun findClass(name: String, context: SearchContext): LuaClass? {
        return findClass(name)
    }

    override fun findMember(type: ITyClass, fieldName: String, context: SearchContext): LuaClassMember? {
        val clazz = findClass(type.className) ?: return null
        return clazz.findMember(fieldName)
    }

    override fun processAllClassNames(project: Project, processor: Processor<String>): Boolean {
        for (clazz in myClassList) {
            if (!processor.process(clazz.fullName))
                return false
        }
        return true
    }

    override fun processClassesWithName(name: String, context: SearchContext, processor: Processor<LuaClass>): Boolean {
        return findClass(name, context)?.let { processor.process(it) } ?: true
    }

    private fun isRoot(type: String): Boolean {
        return type == "_G" || type == "CS" || type == "\$CS"
    }

    override fun getClassMembers(clazzName: String, context: SearchContext): Collection<LuaClassMember> {
        val clazz = if (isRoot(clazzName)) myRootNS else findClass(clazzName)
        if (clazz != null)
            return clazz.members
        return emptyList()
    }

    private fun processAllMembers(type: String, fieldName: String, context: SearchContext, processor: Processor<LuaClassMember>, deep: Boolean = true): Boolean {
        val clazz = (if (isRoot(type)) myRootNS else findClass(type)) ?: return true
        val continueProcess = ContainerUtil.process(clazz.members.filter { it.name == fieldName }, processor)
        if (!continueProcess)
            return false

        if (clazz is UnityClass) {
            val baseType = clazz.baseClassName
            if (deep && baseType != null) {
                return processAllMembers(baseType, fieldName, context, processor, deep)
            }
        }

        return true
    }

    override fun processAllMembers(type: ITyClass, fieldName: String, context: SearchContext, processor: Processor<LuaClassMember>): Boolean {
        return processAllMembers(type.className, fieldName, context, processor)
    }
}

private class UnityClassMember(
        val fieldName: String,
        val type: ITy,
        val parent: UnityClass,
        mg: PsiManager
) : LightElement(mg, LuaLanguage.INSTANCE), PsiNamedElement, LuaClassField {

    override fun toString(): String {
        return fieldName
    }

    override fun guessType(context: SearchContext): ITy {
        return type
    }

    override fun setName(name: String): PsiElement {
        return this
    }

    override fun getName() = fieldName

    override fun guessParentType(context: SearchContext): ITy {
        return parent.type
    }

    override val visibility: Visibility
        get() = Visibility.PUBLIC
    override val isDeprecated: Boolean
        get() = false
}

private class TyUnityClass(val clazz: UnityClass) : TyClass(clazz.fullName, clazz.name, clazz.baseClassName) {
    override fun findMemberType(name: String, searchContext: SearchContext): ITy? {
        return clazz.findMember(name)?.guessType(searchContext)
    }

    override fun findMember(name: String, searchContext: SearchContext): LuaClassMember? {
        return clazz.findMember(name)
    }
}

private class UnityClass(
        className: String,
        val fullName: String,
        val baseClassName: String?,
        parent: Namespace?,
        mg: PsiManager
) : NsMember(className, parent, mg) {

    private val ty: ITyClass by lazy { TyUnityClass(this) }

    override val type: ITyClass
        get() = ty

    override fun toString(): String {
        return fullName
    }

    fun addMember(name: String, type: ITy) {
        val member = UnityClassMember(name, type, this, manager)
        members.add(member)
    }
}

private abstract class NsMember(
        val memberName: String,
        val parent: Namespace?,
        mg: PsiManager
) : LightElement(mg, LuaLanguage.INSTANCE), PsiNamedElement, LuaClass, LuaClassField {

    val members = mutableListOf<LuaClassMember>()

    override fun setName(name: String): PsiElement {
        return this
    }

    override fun getName(): String {
        return memberName
    }

    override fun guessType(context: SearchContext?): ITy {
        return type
    }

    override fun guessParentType(context: SearchContext): ITy {
        return parent?.type ?: Ty.UNKNOWN
    }

    fun getMember(name: String): LuaClassMember? {
        return members.firstOrNull { it.name == name }
    }

    fun findMember(name: String): LuaClassMember? {
        return members.firstOrNull { it.name == name }
    }

    override val visibility: Visibility
        get() = Visibility.PUBLIC
    override val isDeprecated: Boolean
        get() = false
}

private class NamespaceType(val namespace: Namespace) : TyClass(namespace.fullName) {
    override fun findMemberType(name: String, searchContext: SearchContext): ITy? {
        return namespace.getMember(name)?.guessType(searchContext)
    }

    override fun findMember(name: String, searchContext: SearchContext): LuaClassMember? {
        return namespace.getMember(name)
    }

    override val displayName: String
        get() = namespace.toString()
}

private class Namespace(
        val typeName: String,
        parent: Namespace?,
        mg: PsiManager,
        val isValidate: Boolean
) : NsMember(typeName, parent, mg), LuaClass, LuaClassField {

    private val myType by lazy { NamespaceType(this) }
    private val myMembers = mutableMapOf<String, Namespace>()
    private val myClasses = mutableListOf<UnityClass>()

    val fullName: String
        get() {
            return if (parent == null || !parent.isValidate) typeName else "${parent.fullName}.$typeName"
        }

    fun addMember(ns: String): Namespace {
        val member = Namespace(ns, this, myManager, true)
        myMembers[ns] = member
        members.add(member)
        return member
    }

    fun addMember(clazz: UnityClass) {
        myClasses.add(clazz)
        members.add(clazz)
    }

    fun getOrPut(ns: String): Namespace {
        val m = myMembers[ns]
        if (m != null) return m
        return addMember(ns)
    }

    override val type: ITyClass
        get() = myType

    override fun toString(): String {
        return "namespace: $fullName"
    }
}