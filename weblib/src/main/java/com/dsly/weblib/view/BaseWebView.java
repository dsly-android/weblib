package com.dsly.weblib.view;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;

import com.dsly.weblib.js.JsAccessEntrace;
import com.dsly.weblib.js.JsAccessEntraceImpl;
import com.dsly.weblib.js.JsInterfaceHolder;
import com.dsly.weblib.js.JsInterfaceHolderImpl;
import com.dsly.weblib.utils.X5WebUtils;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.GeolocationPermissions;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebIconDatabase;
import com.tencent.smtt.sdk.WebStorage;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewDatabase;

import java.util.Map;

/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2019/9/10
 *     desc  : 自定义WebView的base类
 *     revise: demo地址：https://github.com/yangchong211/YCWebView
 * </pre>
 */
public class BaseWebView extends WebView {

    protected JsInterfaceHolder mJsInterfaceHolder;
    protected JsAccessEntrace mJsAccessEntrace;

    public BaseWebView(Context context) {
        this(context, null);
    }

    public BaseWebView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BaseWebView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();
    }

    private void init() {
        mJsInterfaceHolder = JsInterfaceHolderImpl.getJsInterfaceHolder(this);
        mJsAccessEntrace = JsAccessEntraceImpl.getInstance(this);
    }

    /**
     * 缓存清除
     * 针对性删除
     */
    public void clearCache() {
        //清除cookie
        CookieManager.getInstance().removeAllCookies(null);
        //清除storage相关缓存
        WebStorage.getInstance().deleteAllData();
        //清除用户密码信息
        WebViewDatabase.getInstance(getContext()).clearUsernamePassword();
        //清除httpauth信息
        WebViewDatabase.getInstance(getContext()).clearHttpAuthUsernamePassword();
        //清除表单数据
        WebViewDatabase.getInstance(getContext()).clearFormData();
        //清除页面icon图标信息
        WebIconDatabase.getInstance().removeAllIcons();
        //删除地理位置授权，也可以删除某个域名的授权（参考接口类）
        GeolocationPermissions.getInstance().clearAll();
    }

    /**
     * 一次性删除所有缓存
     *
     * @param isClearCookie 是否清除cookie操作
     */
    public void clearAllWebViewCache(boolean isClearCookie) {
        //清除cookie
        QbSdk.clearAllWebViewCache(getContext(), isClearCookie);
    }

    /**
     * 刘海屏适配
     *
     * @param displayCutoutEnable 是否适配
     */
    public void setDisplayCutoutEnable(boolean displayCutoutEnable) {
        // 对于刘海屏机器如果webview被遮挡会自动padding
        if (getSettingsExtension() != null) {
            getSettingsExtension().setDisplayCutoutEnable(displayCutoutEnable);
        }
    }

    /**
     * 设置无痕模式
     *
     * @param visitedLinks true表示无痕模式
     */
    public void setShouldTrackVisitedLinks(boolean visitedLinks) {
        //无痕模式
        if (getSettingsExtension() != null) {
            getSettingsExtension().setShouldTrackVisitedLinks(visitedLinks);
        }
    }

    /**
     * 强制缩放
     *
     * @param scaleEnabled true表示强制缩放
     */
    public void setForcePinchScaleEnabled(boolean scaleEnabled) {
        //对于无法缩放的页面当用户双指缩放时会提示强制缩放，再次操作将触发缩放功能
        if (getSettingsExtension() != null) {
            getSettingsExtension().setForcePinchScaleEnabled(scaleEnabled);
        }
    }

    /**
     * 前进后退缓存
     *
     * @param cacheEnable true表示缓存
     */
    public void setContentCacheEnable(boolean cacheEnable) {
        //开启后前进后退将不再重新加载页面，默认关闭，开启方法如下
        if (getSettingsExtension() != null) {
            getSettingsExtension().setContentCacheEnable(cacheEnable);
        }
    }

    /**
     * 夜间模式
     *
     * @param dayOrNight true(日间模式)
     */
    @Override
    public void setDayOrNight(boolean dayOrNight) {
        // enable:true(日间模式)，enable：false（夜间模式）
        if (getSettingsExtension() != null) {
            getSettingsExtension().setDayOrNight(dayOrNight);
        }
    }

    /**
     * 设置是否允许抓包
     * APP自身必须调用WebView.setWebContentsDebuggingEnabled(true); 才会允许被DevTools调试
     *
     * @param isOpen 默认允许
     */
    public void setFidderOpen(boolean isOpen) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(isOpen);
        }
    }

    /**
     * 重新加载
     */
    @Override
    public void reload() {
        if (X5WebUtils.isMainThread()) {
            super.reload();
        } else {
            Handler handler = getHandler();
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        BaseWebView.super.reload();
                    }
                });
            }
        }
    }

    /**
     * 页面可见开启js交互
     */
    public void resume() {
        //生命周期
        this.getSettings().setJavaScriptEnabled(true);
    }

    /**
     * 页面不可见关闭js交互
     */
    public void stop() {
        //生命周期
        this.getSettings().setJavaScriptEnabled(false);
    }

    /**
     * This method can be called in any thread, and if it is not called in the main thread,
     * it will be automatically distributed to the main thread.
     *
     * @param script
     */
    @Override
    public void evaluateJavascript(final String script, final ValueCallback<String> callback) {
        if (script == null || script.length() == 0) {
            return;
        }
        if (X5WebUtils.isMainThread()) {
            BaseWebView.super.evaluateJavascript(script, callback);
        } else {
            Handler handler = getHandler();
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        BaseWebView.super.evaluateJavascript(script, callback);
                    }
                });
            }
        }
    }

    /**
     * 这个方法可以在任何线程中调用，如果在主线程中没有调用它，它将自动分配给主线程。通过handler实现不同线程
     *
     * @param url url
     */
    @Override
    public void loadUrl(final String url) {
        if (url == null || url.length() == 0) {
            return;
        }
        if (X5WebUtils.isMainThread()) {
            super.loadUrl(url);
        } else {
            Handler handler = getHandler();
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        BaseWebView.super.loadUrl(url);
                    }
                });
            }
        }
    }

    @Override
    public void loadUrl(final String url, final Map<String, String> additionalHttpHeaders) {
        if (url == null || url.length() == 0) {
            return;
        }
        if (X5WebUtils.isMainThread()) {
            super.loadUrl(url, additionalHttpHeaders);
        } else {
            Handler handler = getHandler();
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        BaseWebView.super.loadUrl(url, additionalHttpHeaders);
                    }
                });
            }
        }
    }

    public JsInterfaceHolder getJsInterfaceHolder() {
        return mJsInterfaceHolder;
    }

    public JsAccessEntrace getJsAccessEntrace() {
        return mJsAccessEntrace;
    }
}
