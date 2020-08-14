package com.code.croutefactory

import android.text.TextUtils
import java.io.File

/**
 *  author : balance
 *  date : 2020/8/13 3:02 PM
 *  description :
 */
class RoutePool {
    companion object {
        //组件列表
        private var pools: MutableMap<String, IRoute> = mutableMapOf()
        //组件注入方法
        fun registerComponent(name: String) {
            if (name.isNullOrEmpty()) {
                return
            }
            if (pools.containsKey(name)) {
                throw Exception("has register same components,please change other name")
            }
            var name = name.replace(File.separator, ".")
            try {
                //获取真正的组件名称
                name = name.substring(0, name.length - 6)
                //反射获取组件入口实例
                val cls = Class.forName(name)
                val iRoute = cls.newInstance() as IRoute
                val componentName: String = iRoute.getRouteName()
                if (TextUtils.isEmpty(componentName)) {
                    return
                }
                //添加到组件池子
                pools.put(componentName, iRoute)
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InstantiationException) {
                e.printStackTrace()
            }
        }

        fun excute(cBuilder: CBuilder):Any {
            if (!pools.keys.contains(cBuilder.name)) {
                return false
            }
            //寻找目标组件
            var iRoute = pools.getValue(cBuilder.name!!)
            //调用组件方法
            return iRoute.excute(cBuilder.context!!, cBuilder.action!!, cBuilder.params)
        }
    }
}