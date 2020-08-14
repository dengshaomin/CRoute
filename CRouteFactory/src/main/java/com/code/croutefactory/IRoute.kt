package com.code.croutefactory

import android.content.Context

/**
 *  author : balance
 *  date : 2020/8/13 3:03 PM
 *  description :
 */
interface IRoute {
    fun getRouteName(): String
    fun excute(context: Context, action: String, params: MutableMap<String, String>):Any
}