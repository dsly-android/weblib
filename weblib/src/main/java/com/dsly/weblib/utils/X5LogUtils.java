package com.dsly.weblib.utils;

import android.util.Log;

/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2017/10/21
 *     desc  : log工具
 *     revise:
 * </pre>
 */
public final class X5LogUtils {

    private static final String TAG = "X5LogUtils";
    private static boolean isDebug = true;

    /**
     * 设置是否开启日志
     * @param isDebug                 是否开启日志
     */
    public static void setIsDebug(boolean isDebug) {
        X5LogUtils.isDebug = isDebug;
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static void d(String message) {
        if(isDebug){
            Log.d(TAG, message);
        }
    }

    public static void i(String message) {
        if(isDebug){
            Log.i(TAG, message);
        }
    }

    public static void e(String message) {
        if(isDebug){
            Log.e(TAG, message);
        }
    }

    public static void e(String message, Throwable throwable) {
        if(isDebug){
            Log.e(TAG, message, throwable);
        }
    }

}
