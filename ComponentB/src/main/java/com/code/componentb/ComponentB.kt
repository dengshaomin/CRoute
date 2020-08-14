package com.code.componentb

import android.content.Context
import com.code.croutefactory.IRoute
import com.code.routeconstants.ComponentB

/**
 *  author : balance
 *  date : 2020/8/14 2:13 PM
 *  description :
 */
class ComponentB :IRoute {
    override fun getRouteName(): String {
        return ComponentB.name
    }

    override fun excute(context: Context, action: String, params: MutableMap<String, String>): Any {
        return true
    }
}