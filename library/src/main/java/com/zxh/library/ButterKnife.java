package com.zxh.library;

import android.app.Activity;

public class ButterKnife {
    public static void bind(Activity activity){
       String className=activity.getClass().getName()+"$$ViewBinder";
        try {
            Class<?> clazz = Class.forName(className);
            ViewBinder viewBinder = (ViewBinder) clazz.newInstance();
            viewBinder.bind(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
