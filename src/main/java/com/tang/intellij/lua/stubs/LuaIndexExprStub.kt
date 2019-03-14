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

package com.tang.intellij.lua.stubs

import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.BitUtil
import com.intellij.util.io.StringRef
import com.tang.intellij.lua.psi.*
import com.tang.intellij.lua.psi.impl.LuaIndexExprImpl
import com.tang.intellij.lua.search.SearchContext
import com.tang.intellij.lua.stubs.index.LuaClassMemberIndex
import com.tang.intellij.lua.stubs.index.StubKeys
import com.tang.intellij.lua.ty.*
import java.util.*

/**
 * Created by TangZX on 2017/4/12.
 */
class LuaIndexExprType : LuaStubElementType<LuaIndexExprStub, LuaIndexExpr>("INDEX_EXPR") {

    override fun createPsi(indexStub: LuaIndexExprStub): LuaIndexExpr {
        return LuaIndexExprImpl(indexStub, this)
    }

    /*override fun shouldCreateStub(node: ASTNode): Boolean {
        val psi = node.psi as LuaIndexExpr
        val parent = psi.parent
        if (parent is LuaExprList || parent is LuaCallExpr)
            return super.createStubIfParentIsStub(node)

        if (psi.id != null || psi.idExpr != null) {
            if (parent is LuaVarList) {
                return true
            }
        }
        return false
    }*/

    override fun createStub(indexExpr: LuaIndexExpr, stubElement: StubElement<*>): LuaIndexExprStub {
        val stat = indexExpr.assignStat
        val docTy = stat?.comment?.docTy
        val classNameSet = mutableSetOf<String>()
        // for chain
        val indexExprNames = mutableListOf<String>()
        // end
        if (stat != null) {
            val context = SearchContext(indexExpr.project, indexExpr.containingFile, true)
            var ty = indexExpr.guessParentType(context)
            // for chain
            if (ty == Ty.UNKNOWN) {
                /**
                 * local t = {}
                 * t.data = {}  赋值时保存t的data属性
                 * t.data.name = "Tom" 赋值时，由于无法获取t.data属性所以，查找最近的类型
                 */
                ty = getKnownIndexLuaExprType(indexExpr, context, indexExprNames)
            }
            // end

            TyUnion.each(ty) {
                if (it is ITyClass)
                    classNameSet.add(it.className)
            }
            context.forStore = false
        }
        val visibility = indexExpr.visibility

        var flags = BitUtil.set(0, visibility.bitMask, true)
        flags = BitUtil.set(flags, LuaIndexExprType.FLAG_DEPRECATED, indexExpr.isDeprecated)
        flags = BitUtil.set(flags, LuaIndexExprType.FLAG_BRACK, indexExpr.lbrack != null)
        flags = BitUtil.set(flags, LuaIndexExprType.FLAG_ASSIGN, stat != null)
        // for chain
        val stub: LuaIndexExprStub
        if (indexExprNames.isEmpty()) {
            stub = LuaIndexExprStubImpl(classNameSet.toTypedArray(),
                    indexExpr.name,
                    flags,
                    docTy,
                    stubElement,
                    this)
        } else {
            stub = LuaIndexExprHolderImpl(classNameSet.toTypedArray(),
                    indexExprNames.toTypedArray(), // 保存访问到最近的有类型的父类型的路径
                    indexExpr.name,
                    flags,
                    docTy,
                    stubElement,
                    this)
        }
        return stub
        // end
    }

    override fun serialize(indexStub: LuaIndexExprStub, stubOutputStream: StubOutputStream) {
        stubOutputStream.writeNames(indexStub.classNames)
        stubOutputStream.writeName(indexStub.name)
        // for chain
        if (indexStub is LuaIndexExprHolderImpl) {
            stubOutputStream.writeInt(FLAG_EXTRA)
            stubOutputStream.writeNames(indexStub.indexExprNames)
        }
        // end
        stubOutputStream.writeInt(indexStub.flags)
        stubOutputStream.writeTyNullable(indexStub.docTy)
    }

    override fun deserialize(stubInputStream: StubInputStream, stubElement: StubElement<*>): LuaIndexExprStub {
        val classNames = stubInputStream.readNames()
        val fieldName = stubInputStream.readName()
        var flags = stubInputStream.readInt()
        // for chain
        var indexExprNames: Array<String>? = null
        if (flags == FLAG_EXTRA) {
            indexExprNames = stubInputStream.readNames()
            flags = stubInputStream.readInt()
        }
        // end

        val docTy = stubInputStream.readTyNullable()
        // for chain
        if (indexExprNames == null) {
            return LuaIndexExprStubImpl(classNames,
                    StringRef.toString(fieldName),
                    flags,
                    docTy,
                    stubElement,
                    this)
        } else {
            return LuaIndexExprHolderImpl(classNames,
                    indexExprNames,
                    StringRef.toString(fieldName),
                    flags,
                    docTy,
                    stubElement,
                    this)
        }
        // end
    }

    override fun indexStub(indexStub: LuaIndexExprStub, indexSink: IndexSink) {
        val fieldName = indexStub.name
        val classNames = indexStub.classNames
        if (indexStub.isAssign && classNames.isNotEmpty() && fieldName != null) {
            // for chain
            // t.data.name的时候没有获取到t.data的类型，为其创建一个占位的类型
            if (indexStub is LuaIndexExprHolderImpl && classNames.isNotEmpty()) {
                val baseClassName = classNames[0]
                var parentClassName: String
                val indexExprNames = indexStub.indexExprNames
                var i = 1
                indexExprNames.forEach {
                    parentClassName = getFiledNameAsClassName(baseClassName, indexExprNames, i)
                    var currentFieldName: String
                    if (i < indexExprNames.size) {
                        // t.data.name.text
                        // 索引t.__@@data、t.__data.__@@name、
                        // t.__data.__name.text
                        // 上面两个是占位的，便于在输入t.的时候提示t.data、t.data.name（如果没有此索引，就必须在t.data.name.的时候才提示text）
                        // 使用__@@前缀是为了防止resolveTypeByRoot的时候resolve到，因为此处的t.data和t.data.name并不是实际声明的地方
                        currentFieldName = getFieldHolderName(indexExprNames[i])
                    } else {
                        // t.data.name.text，最后一个属性就是当前LuaIndexExprStub声明的地方，使用.text索引
                        currentFieldName = fieldName
                    }
                    LuaClassMemberIndex.indexStub(indexSink, parentClassName, currentFieldName)
                    i++
                }
            } else {
            // end
                // 原始逻辑
                classNames.forEach {
                    LuaClassMemberIndex.indexStub(indexSink, it, fieldName)
                }
            // for chain
            }
            // end   

            indexSink.occurrence(StubKeys.SHORT_NAME, fieldName)
        }
    }

    companion object {
        // for chain
        const val FLAG_EXTRA = 0x00 // 扩展标记，如果读取到此标记则表示后面可以读取扩展数据，同理如果要写扩展数据，则先要写入扩展数据
        // end
        const val FLAG_DEPRECATED = 0x20
        const val FLAG_BRACK = 0x40
        const val FLAG_ASSIGN = 0x80

        // for chain
        /**
         * 获取LuaIndexExpr最近的、有类型的父Expr的类型。
         * 比如对于LuaIndexExpr：t.data.name，获取到t，因为对于如下代码：
         * t.data = {}
         * t.data.name = 'Tom'
         * 在对t.data.name进行LuaIndexExprStub创建的时候，t.data还未进行indexStub，因此获取到的t.data的ty是TyUnknown，
         * 因此再向前获取t类型，直到获取到有类型，或者全部都没有类型（放弃）
         * 找到类型后，保存当前的LuaIndexExpr和该类型的关系：t   {"data"}：即：当前属性是t类型的data属性的的属性，
         * 因此就给t.data创建一个占位符版本的LuaIndexExprStub
         */
        fun getKnownIndexLuaExprType(_indexExpr: LuaIndexExpr, context: SearchContext, indexExprNames: MutableList<String>): ITy {
            val result = getAllKnownIndexLuaExprType(_indexExpr, context, true)
            if (result.isEmpty()) {
                return Ty.UNKNOWN
            }
            val first = result.entries.first()
            indexExprNames.addAll(first.value)
            return first.key
        }

        fun getAllKnownIndexLuaExprType(_indexExpr: LuaIndexExpr, context: SearchContext): Map<ITy, List<String>> {
            return getAllKnownIndexLuaExprType(_indexExpr, context, false)
        }

        private fun getAllKnownIndexLuaExprType(_indexExpr: LuaIndexExpr, context: SearchContext, onlyOne: Boolean): Map<ITy, List<String>> {
            val result = mutableMapOf<ITy, List<String>>()
            var indexExpr: LuaExpr? = _indexExpr
            var indexExprType: ITy?
            val indexExprNames = mutableListOf<String>()
            while (indexExpr != null && indexExpr is LuaIndexExpr && indexExpr.name != null) {
                indexExprType = indexExpr.guessParentType(context)
                var isValid = false
                TyUnion.each(indexExprType, {
                    if (it is ITyClass) {
                        isValid = true
                        return@each
                    }
                })
                if (isValid) {
                    result.put(indexExprType, indexExprNames.reversed())
                    if (onlyOne) {
                        return result
                    }
                }
                indexExpr = PsiTreeUtil.getStubChildOfType(indexExpr, LuaExpr::class.java)
                // 必须不能为null
                if (indexExpr?.name != null) {
                    indexExprNames.add(indexExpr.name!!)
                } else {
                    break
                }
            }
            if (result.isEmpty()) {
                indexExprNames.clear()
            }
            return result
        }

        /**
         * 生成Type的属性当做class的className，即：a.b.c，因为没有a.b类型，因此生成占位类：a.__b
         */
        fun getFiledNameAsClassName(baseClassName: String, indexExprNames: Array<String>, depth: Int = indexExprNames.size): String {
            require(depth <= indexExprNames.size) {
                "depth 不能超出 indexExprNames.size"
            }
            var className = baseClassName
            // 如果depth = 0,，则直接返回baseClassName
            if (depth >= 1) {
                (1..depth).forEach {
                    className += ".__${indexExprNames.get(it - 1)}"
                }
            }
            return className
        }

        /**
         * 获取占位属性名，即：并不是真的有这个属性，是为了该属性有子属性而动态创建的
         */
        fun getFieldHolderName(fieldName: String): String {
            return "__@@$fieldName"
        }
        // end

    }
}

interface LuaIndexExprStub : LuaExprStub<LuaIndexExpr>, LuaClassMemberStub<LuaIndexExpr> {
    val classNames: Array<String>
    val name: String?
    val flags: Int
    val brack: Boolean
    val isAssign: Boolean
}

// for chain: add open
open class LuaIndexExprStubImpl(override val classNames: Array<String>,
                                override val name: String?,
                                override val flags: Int,
                                override val docTy: ITy?,
                                stubElement: StubElement<*>,
                                indexType: LuaIndexExprType)
    : LuaStubBase<LuaIndexExpr>(stubElement, indexType), LuaIndexExprStub {
    override val isDeprecated: Boolean
        get() = BitUtil.isSet(flags, LuaIndexExprType.FLAG_DEPRECATED)

    override val visibility: Visibility
        get() = Visibility.getWithMask(flags)

    override val brack: Boolean
        get() = BitUtil.isSet(flags, LuaIndexExprType.FLAG_BRACK)

    override val isAssign: Boolean
        get() = BitUtil.isSet(flags, LuaIndexExprType.FLAG_ASSIGN)
}

// for chain
/**
 * 代替默认的LuaIndexExprStubImpl，用于对没有上下文的lua提供占位符版本的parentType
 */
class LuaIndexExprHolderImpl(override val classNames: Array<String>,   // 如果indexExprNames为空集合，则存储的是field所属的类的类名，如果不为空，则存储的是根类型的类名
                             val indexExprNames: Array<String>, // 对于每个rootClassName，访问到当前表达式的路径，比如对于t.data.a.b.name就是：['data', 'a', 'b']
                             override val name: String?,
                             override val flags: Int,
                             override val docTy: ITy?,
                             stubElement: StubElement<*>,
                             indexType: LuaIndexExprType)
    : LuaIndexExprStubImpl(classNames, name, flags, docTy, stubElement, indexType)
// end    