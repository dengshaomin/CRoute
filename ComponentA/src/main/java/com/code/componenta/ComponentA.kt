package com.code.componenta

import android.content.Context
import android.content.Intent
import android.widget.Switch
import com.code.croutefactory.IRoute
import com.code.routeconstants.ComponentA

/**
 *  author : balance
 *  date : 2020/8/13 3:59 PM
 *  description :
 */
class ComponentA : IRoute {
    override fun getRouteName(): String {
        return ComponentA.name
    }

    override fun excute(context: Context, action: String, params: MutableMap<String, String>): Any {
        when (action) {
            ComponentA.action1 -> {
                context.startActivity(Intent(context, ComponentAActivity::class.java))
                return true
            }
            ComponentA.action2 -> {
                return ComponentAActivity.getResult(params)
            }
        }
        return true
    }
}