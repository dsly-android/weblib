package com.dsly.weblib.utils;

import android.content.Context;
import android.os.Build;

import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;

import java.io.File;
import java.util.ArrayList;

/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2019/9/10
 *     desc  : cookie工具类
 *     revise: demo地址：https://github.com/yangchong211/YCWebView
 * </pre>
 */
public final class WebkitCookieUtils {

    static final String FILE_CACHE_PATH = "agentweb-cache";
    static final String AGENTWEB_CACHE_PATCH = File.separator + "agentweb-cache";
    /**
     * 缓存路径
     */
    static String AGENTWEB_FILE_PATH;

    /**
     * 同步cookie
     * 建议调用webView.loadUrl(url)之前一句调用此方法就可以给WebView设置Cookie
     *
     * @param url        地址
     * @param cookieList 需要添加的Cookie值,以键值对的方式:key=value
     */
    public static void syncCookie(Context context, String url, ArrayList<String> cookieList) {
        //获取对象
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        //移除
        cookieManager.removeSessionCookie();
        //添加cookie操作
        if (cookieList != null && cookieList.size() > 0) {
            for (String cookie : cookieList) {
                cookieManager.setCookie(url, cookie);
            }
        }
        String cookies = cookieManager.getCookie(url);
        X5LogUtils.d("WebkitCookieUtils-------" + cookies);
        flush(context);
    }

    /**
     * 清除cookie操作，所有的
     *
     * @param context 上下文
     */
    public static void removeCookie(Context context) {
        CookieSyncManager.createInstance(context);
        CookieSyncManager.getInstance().startSync();
        CookieManager.getInstance().removeSessionCookie();
    }

    /**
     * 获取url的cookie操作
     *
     * @param url url
     * @return
     */
    public static String getCookie(String url) {
        CookieManager cookieManager = CookieManager.getInstance();
        if (cookieManager == null) {
            return null;
        } else {
            return cookieManager.getCookie(url);
        }
    }


    /**
     * 移除指定url关联的所有cookie
     *
     * @param url url链接
     */
    public static void remove(Context context, String url) {
        CookieManager cm = CookieManager.getInstance();
        for (String cookie : cm.getCookie(url).split("; ")) {
            cm.setCookie(url, cookie.split("=")[0] + "=");
        }
        flush(context);
    }

    /**
     * sessionOnly 为true表示移除所有会话cookie，否则移除所有cookie
     *
     * @param sessionOnly 是否移除会话cookie
     */
    public static void remove(Context context, boolean sessionOnly) {
        CookieManager cm = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (sessionOnly) {
                cm.removeSessionCookies(null);
            } else {
                cm.removeAllCookies(null);
            }
        } else {
            if (sessionOnly) {
                cm.removeSessionCookie();
            } else {
                cm.removeAllCookie();
            }
        }
        flush(context);
    }

    /**
     * 写入磁盘
     *
     * @param context
     */
    private static void flush(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().flush();
        } else {
            //初始化
            CookieSyncManager.createInstance(context);
            CookieSyncManager.getInstance().sync();
        }
    }
}
