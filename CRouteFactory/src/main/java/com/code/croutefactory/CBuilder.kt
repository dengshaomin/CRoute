package com.code.croutefactory

import android.content.Context

/**
 *  author : balance
 *  date : 2020/8/13 3:21 PM
 *  description :
 */
class CBuilder {
    var context: Context? = null
    var name: String? = null
    var action: String? = null
    var params: MutableMap<String, String> = mutableMapOf()

    companion object {
        fun create(context: Context): CBuilder {
            var cBuilder = CBuilder()
            cBuilder.context = context
            return cBuilder
        }
    }

    fun params(params: MutableMap<String, String>): CBuilder {
        if (!params.isNullOrEmpty())
            this.params.putAll(params)
        return this
    }

    fun name(name: String): CBuilder {
        this.name = name
        return this
    }

    fun action(action: String): CBuilder {
        this.action = action
        return this
    }

    fun build(): Any {
        return RoutePoolJava.excute(this)
    }
}