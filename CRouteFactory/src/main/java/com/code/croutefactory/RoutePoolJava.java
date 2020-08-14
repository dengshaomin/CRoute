package com.code.croutefactory;

import android.text.TextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * author : balance
 * date : 2020/8/13 11:10 PM
 * description :
 */
public class RoutePoolJava {
    private static Map pools = new HashMap();

    static {

    }

    public static void registerComponent(String name) {
        if (TextUtils.isEmpty(name)) {
            return;
        }

        if (pools.containsKey(name)) {
            throw new RuntimeException("has register same components,please change other name");
        }
        name = name.replace(File.separator, ".");
        try {
            name = name.substring(0, name.length() - 6);
            Class cls = Class.forName(name);
            IRoute iRoute = (IRoute) cls.newInstance();
            String componentName = iRoute.getRouteName();
            if (TextUtils.isEmpty(componentName)) {
                return;
            }
            pools.put(componentName, iRoute);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

    }

    public static Object excute(CBuilder cBuilder) {
        if (!pools.containsKey(cBuilder.getName())) {
            return null;
        }
        IRoute iRoute = (IRoute) pools.get(cBuilder.getName());
        return iRoute.excute(cBuilder.getContext(), cBuilder.getAction(), cBuilder.getParams());
    }
}
