package com.tang.intellij.lua.codeInsight

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider

/**
 *
 * Created by tangzx on 2017/2/11.
 */
class LuaLiveTemplatesProvider : DefaultLiveTemplatesProvider {

    override fun getDefaultLiveTemplateFiles() = DEFAULT_TEMPLATES

    override fun getHiddenLiveTemplateFiles(): Array<String>? = emptyArray()

    companion object {
        private val DEFAULT_TEMPLATES = arrayOf("/liveTemplates/lua")
    }
}
