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

package com.tang.intellij.lua.ty

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectAndLibrariesScope
import com.intellij.testFramework.LightVirtualFileBase
import com.intellij.util.Processor
import com.intellij.util.keyFMap.ArrayBackedFMap
import com.intellij.util.keyFMap.KeyFMap
import com.tang.intellij.lua.Constants
import com.tang.intellij.lua.comment.psi.LuaDocTableDef
import com.tang.intellij.lua.comment.psi.LuaDocTagClass
import com.tang.intellij.lua.project.LuaSettings
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.psi.search.LuaClassInheritorsSearch
import com.tang.intellij.lua.psi.search.LuaShortNamesManager
import com.tang.intellij.lua.search.SearchContext

interface ITyClass : ITy {
    val className: String
    val varName: String
    var superClassName: String?
    var aliasName: String?
    fun processAlias(processor: Processor<String>): Boolean
    fun lazyInit(searchContext: SearchContext)
    fun processMembers(context: SearchContext, processor: (ITyClass, LuaClassMember) -> Unit, deep: Boolean = true)
    fun processMembers(context: SearchContext, processor: (ITyClass, LuaClassMember) -> Unit) {
        processMembers(context, processor, true)
    }
    fun findMember(name: String, searchContext: SearchContext): LuaClassMember?
    fun findMemberType(name: String, searchContext: SearchContext): ITy?
    fun findSuperMember(name: String, searchContext: SearchContext): LuaClassMember?

    fun recoverAlias(context: SearchContext, aliasSubstitutor: TyAliasSubstitutor): ITy {
        return this
    }
}

fun ITyClass.isVisibleInScope(project: Project, contextTy: ITy, visibility: Visibility): Boolean {
    if (visibility == Visibility.PUBLIC)
        return true
    var isVisible = false
    TyUnion.process(contextTy) {
        if (it is ITyClass) {
            if (it == this)
                isVisible = true
            else if (visibility == Visibility.PROTECTED) {
                isVisible = LuaClassInheritorsSearch.isClassInheritFrom(GlobalSearchScope.projectScope(project), project, className, it.className)
            }
        }
        !isVisible
    }
    return isVisible
}

abstract class TyClass(override val className: String,
                       override val varName: String = "",
                       override var superClassName: String? = null
) : Ty(TyKind.Class), ITyClass {

    final override var aliasName: String? = null

    private var _lazyInitialized: Boolean = false

    override fun equals(other: Any?): Boolean {
        return other is ITyClass && other.className == className && other.flags == flags
    }

    override fun hashCode(): Int {
        return className.hashCode()
    }

    override fun processAlias(processor: Processor<String>): Boolean {
        val alias = aliasName
        if (alias != null && !processor.process(alias))
            return false
        if (!isGlobal && !isAnonymous && LuaSettings.instance.isRecognizeGlobalNameAsType)
            return processor.process(getGlobalTypeName(className))
        return true
    }

    override fun processMembers(context: SearchContext, processor: (ITyClass, LuaClassMember) -> Unit, deep: Boolean) {
        val clazzName = className
        val project = context.project

        val manager = LuaShortNamesManager.getInstance(project)
        val list = manager.getClassMembers(clazzName, project, ProjectAndLibrariesScope(project))

        processAlias(Processor { alias ->
            val classMembers = manager.getClassMembers(alias, project, ProjectAndLibrariesScope(project))
            list.addAll(classMembers)
        })

        for (member in list) {
            processor(this, member)
        }

        // super
        if (deep) {
            val superType = getSuperClass(context) as? ITyClass ?: return
            superType.processMembers(context, processor, deep)
        }
    }

    override fun findMember(name: String, searchContext: SearchContext): LuaClassMember? {
        return LuaShortNamesManager.getInstance(searchContext.project).findMember(this, name, searchContext)
    }

    override fun findMemberType(name: String, searchContext: SearchContext): ITy? {
        return infer(findMember(name, searchContext), searchContext)
    }

    override fun findSuperMember(name: String, searchContext: SearchContext): LuaClassMember? {
        // Travel up the hierarchy to find the lowest member of this type on a superclass (excluding this class)
        var superClass = getSuperClass(searchContext)
        while (superClass != null && superClass is TyClass) {
            val member = superClass.findMember(name, searchContext)
            if (member != null) return member
            superClass = superClass.getSuperClass(searchContext)
        }
        return null
    }

    override fun accept(visitor: ITyVisitor) {
        visitor.visitClass(this)
    }

    override fun lazyInit(searchContext: SearchContext) {
        if (!_lazyInitialized) {
            _lazyInitialized = true
            doLazyInit(searchContext)
        }
    }

    open fun doLazyInit(searchContext: SearchContext) {
        val classDef = LuaShortNamesManager.getInstance(searchContext.project).findClass(className, searchContext)
        if (classDef != null && aliasName == null) {
            val tyClass = classDef.type
            aliasName = tyClass.aliasName
            superClassName = tyClass.superClassName
        }
    }

    override fun getSuperClass(context: SearchContext): ITy? {
        lazyInit(context)
        val clsName = superClassName
        if (clsName != null) {
            return Ty.getBuiltin(clsName) ?: LuaShortNamesManager.getInstance(context.project).findClass(clsName, context)?.type
        }
        return null
    }

    override fun subTypeOf(other: ITy, context: SearchContext, strict: Boolean): Boolean {
        // class extends table
        if (other == Ty.TABLE) return true
        if (super.subTypeOf(other, context, strict)) return true

        // Lazy init for superclass
        this.doLazyInit(context)
        // Check if any of the superclasses are type
        var superClass = getSuperClass(context)
        while (superClass != null) {
            if (other == superClass) return true
            superClass = superClass.getSuperClass(context)
        }

        return false
    }

    override fun substitute(substitutor: ITySubstitutor): ITy {
        return substitutor.substitute(this)
    }

    companion object {
        // for _G
        val G: TyClass = createSerializedClass(Constants.WORD_G)

        fun createAnonymousType(nameDef: LuaNameDef): TyClass {
            val stub = nameDef.stub
            val tyName = stub?.anonymousType ?: getAnonymousType(nameDef)
            return createSerializedClass(tyName, nameDef.name, null, null, TyFlags.ANONYMOUS)
        }

        fun createGlobalType(nameExpr: LuaNameExpr, store: Boolean): ITy {
            val name = nameExpr.name
            val g = createSerializedClass(getGlobalTypeName(nameExpr), name, null, null, TyFlags.GLOBAL)
            if (!store && LuaSettings.instance.isRecognizeGlobalNameAsType)
                return createSerializedClass(name, name, null, null, TyFlags.GLOBAL).union(g)
            return g
        }

        fun createGlobalType(name: String): ITy {
            val g = createSerializedClass(getGlobalTypeName(name), name, null, null, TyFlags.GLOBAL)
            if (LuaSettings.instance.isRecognizeGlobalNameAsType)
                return createSerializedClass(name, name, null, null, TyFlags.GLOBAL).union(g)
            return g
        }
    }
}

class TyPsiDocClass(tagClass: LuaDocTagClass) : TyClass(tagClass.name) {

    init {
        val supperRef = tagClass.superClassNameRef
        if (supperRef != null)
            superClassName = supperRef.text
        aliasName = tagClass.aliasName
    }

    override fun doLazyInit(searchContext: SearchContext) {}
}

open class TySerializedClass(name: String,
                             varName: String = name,
                             supper: String? = null,
                             alias: String? = null,
                             flags: Int = 0)
    : TyClass(name, varName, supper) {
    init {
        aliasName = alias
        this.flags = flags
    }

    override fun recoverAlias(context: SearchContext, aliasSubstitutor: TyAliasSubstitutor): ITy {
        val alias = LuaShortNamesManager.getInstance(context.project).findAlias(className, context.project, context.getScope())
        return alias?.type?.substitute(aliasSubstitutor) ?: this
    }
}

//todo Lazy class ty
class TyLazyClass(name: String) : TySerializedClass(name)

fun createSerializedClass(name: String,
                          varName: String = name,
                          supper: String? = null,
                          alias: String? = null,
                          flags: Int = 0): TyClass {
    val list = name.split("|")
    if (list.size == 3) {
        val type = list[0].toInt()
        if (type == 10) {
            return TySerializedDocTable(name)
        }
    }

    return TySerializedClass(name, varName, supper, alias, flags)
}

/**
 * 嘿嘿
 */
fun getFilePath(psiFile: PsiFile): String? {
    var filePath : String? = null
    if (psiFile.virtualFile != null) {
        filePath = psiFile.virtualFile.path
    } else {
        //return "${nameDef.node.startOffset}@${nameDef.containingFile.name}"
        if (psiFile is UserDataHolderBase) {
            val field = UserDataHolderBase::class.java.getDeclaredField("myUserMap")
            field.isAccessible = true
            val fMap = field.get(psiFile) as KeyFMap
            if (fMap is ArrayBackedFMap) {
                val valuesFields = ArrayBackedFMap::class.java.getDeclaredField("values")
                valuesFields.isAccessible = true
                val values = valuesFields.get(fMap) as Array<*>
                values.forEach {
                    if (it is VirtualFile) {
                        filePath = it.path
                        return@forEach
                    }
                }
            } else if (fMap.javaClass.name == "com.intellij.util.keyFMap.PairElementsFMap") {
                val virtualFile = psiFile.viewProvider.virtualFile
                if (virtualFile is LightVirtualFileBase) {
                    filePath = virtualFile.originalFile.path
                } else {
                    fMap.keys.forEach {
                        val v = fMap.get(it)
                        if (v is VirtualFile) {
                            filePath = v.path
                            return@forEach
                        }
                    }
                }
            }

        }
    }
    return filePath
}

fun getFilePathAsName(psiFile: PsiFile): String {
    var filePath = getFilePath(psiFile.originalFile)
    if (filePath == null) {
        filePath = psiFile.name
    } else {
        filePath = filePath.replace(":", "").replace('/', '_')
    }

    return filePath
}

fun getTableTypeName(table: LuaTableExpr): String {
    val stub = table.stub
    if (stub != null)
        return stub.tableTypeName

    return "${getFilePathAsName(table.containingFile)}@(${table.node.startOffset})table"
}

//fun getAnonymousType(nameDef: LuaNameDef): String {
fun getAnonymousType(psiElement: PsiElement): String {
    /*if (nameDef.containingFile.virtualFile != null) nameDef.containingFile.virtualFile.path.replace(File.separatorChar, '_') else */
    //(nameDef.containingFile as LuaPsiFile).getUserData(Key.create<String>("Context virtual file"))
    return "${psiElement.node.startOffset}@${getFilePathAsName(psiElement.containingFile)}"
}

fun getGlobalTypeName(text: String): String {
    return if (text == Constants.WORD_G) text else "$$text"
}

fun getGlobalTypeName(nameExpr: LuaNameExpr): String {
    return getGlobalTypeName(nameExpr.name)
}

class TyTable(val table: LuaTableExpr) : TyClass(getTableTypeName(table)) {
    init {
        this.flags = TyFlags.ANONYMOUS or TyFlags.ANONYMOUS_TABLE
    }

    override fun processMembers(context: SearchContext, processor: (ITyClass, LuaClassMember) -> Unit, deep: Boolean) {
        for (field in table.tableFieldList) {
            processor(this, field)
        }
        super.processMembers(context, processor, deep)
    }

    override fun toString(): String = displayName

    override fun doLazyInit(searchContext: SearchContext) = Unit

    override fun subTypeOf(other: ITy, context: SearchContext, strict: Boolean): Boolean {
        // Empty list is a table, but subtype of all lists
        return super.subTypeOf(other, context, strict) || other == Ty.TABLE || (other is TyArray && table.tableFieldList.size == 0)
    }
}

fun getDocTableTypeName(table: LuaDocTableDef): String {
    val stub = table.stub
    if (stub != null)
        return stub.className

    val fileName = getFilePathAsName(table.containingFile)//table.containingFile.name
    return "10|$fileName|${table.node.startOffset}"
}

class TyDocTable(val table: LuaDocTableDef) : TyClass(getDocTableTypeName(table)) {
    override fun doLazyInit(searchContext: SearchContext) {}

    override fun processMembers(context: SearchContext, processor: (ITyClass, LuaClassMember) -> Unit, deep: Boolean) {
        table.tableFieldList.forEach {
            processor(this, it)
        }
    }

    override fun findMember(name: String, searchContext: SearchContext): LuaClassMember? {
        return table.tableFieldList.firstOrNull { it.name == name }
    }
}

class TySerializedDocTable(name: String) : TySerializedClass(name) {
    override fun recoverAlias(context: SearchContext, aliasSubstitutor: TyAliasSubstitutor): ITy {
        return this
    }
}
