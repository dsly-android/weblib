package com.dsly.weblib.tools;

import android.content.Context;
import android.content.MutableContextWrapper;
import android.support.v4.util.Pools;

import com.tencent.smtt.sdk.WebView;

/**
 * A simple webview instance pool.
 * Reduce webview initialization time about 100ms.
 * my test env: vivo-x23, android api: 8.1
 * at 2019/11/4
 */
public class WebViewPool {

    private static final int MAX_POOL_SIZE = 2;
    private static final Pools.Pool<WebView> sPool = new Pools.SynchronizedPool<>(MAX_POOL_SIZE);

    public static void prepare(Context context) {
        release(acquire(context.getApplicationContext()));
    }

    public static WebView acquire(Context context) {
        WebView webView = sPool.acquire();
        if (webView == null) {
            MutableContextWrapper wrapper = new MutableContextWrapper(context);
            webView = new WebView(wrapper);
        } else {
            MutableContextWrapper wrapper = (MutableContextWrapper) webView.getContext();
            wrapper.setBaseContext(context);
        }
        webView.clearHistory();
        return webView;
    }

    public static void release(WebView webView) {
        if (webView == null) {
            return;
        }
//        webView.stopLoading();
//        webView.clearCache(true);
//        webView.clearHistory();
        MutableContextWrapper wrapper = (MutableContextWrapper) webView.getContext();
        wrapper.setBaseContext(wrapper.getApplicationContext());
        boolean isRecycle = sPool.release(webView);
        if (!isRecycle){
            webView.destroy();
        }
    }
}
