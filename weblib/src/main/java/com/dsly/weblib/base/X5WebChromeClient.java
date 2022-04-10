/*
Copyright 2017 yangchong211（github.com/yangchong211）

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.dsly.weblib.base;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import com.dsly.weblib.inter.VideoWebListener;
import com.dsly.weblib.utils.X5LogUtils;
import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebView;
import com.dsly.weblib.inter.InterWebListener;
import com.dsly.weblib.utils.X5WebUtils;
import com.dsly.weblib.video.VideoWebChromeClient;

import static android.app.Activity.RESULT_OK;

/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2019/9/10
 *     desc  : 自定义x5的WebChromeClient
 *     revise: 如果自定义WebChromeClient，建议继承该类，后期添加视频播放的处理方法
 *             demo地址：https://github.com/yangchong211/YCWebView
 *
 *             作用：WebViewClient主要辅助WebView执行处理各种响应请求事件的
 * </pre>
 */
public class X5WebChromeClient extends VideoWebChromeClient {

    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadMessageForAndroid5;
    /**
     * 注意h5中调用上传图片，resultCode保持一致性
     */
    public static int FILE_CHOOSER_RESULT_CODE = 1;
    public static int FILE_CHOOSER_RESULT_CODE_5 = 2;
    private InterWebListener webListener;
    private boolean isShowContent = false;
    private Context context;
    private WebView webView;
    public static final int REQUEST_LOCATION = 100;


    /**
     * 设置监听时间，包括常见状态页面切换，进度条变化等
     *
     * @param listener listener
     */
    public void setWebListener(InterWebListener listener) {
        this.webListener = listener;
    }

    /**
     * 设置视频播放监听，主要是比如全频，取消全频，隐藏和现实webView
     *
     * @param videoWebListener listener
     */
    public void setVideoWebListener(VideoWebListener videoWebListener) {
        setVideoListener(videoWebListener);
    }

    /**
     * 构造方法
     *
     * @param context 上下文
     */
    public X5WebChromeClient(WebView webView, Context context) {
        //super(context, webView);
        super(context);
        this.context = context;
        this.webView = webView;
    }

    /**
     * 这个方法是监听加载进度变化的，当加载到百分之八十五的时候，页面一般就出来呢
     * 作用：获得网页的加载进度并显示
     *
     * @param view        view
     * @param newProgress 进度值
     */
    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        if (webListener != null) {
            webListener.startProgress(newProgress);
            int max = 95;
            if (newProgress > max && !isShowContent) {
                webListener.hindProgressBar();
                isShowContent = true;
            }
        }
    }

    /**
     * 这个方法主要是监听标题变化操作的
     *
     * @param view  view
     * @param title 标题
     */
    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        if (title.contains("404") || title.contains("网页无法打开")) {
            if (webListener != null) {
                webListener.showErrorView(X5WebUtils.ErrorMode.STATE_404);
            }
        } else {
            // 设置title
            if (webListener != null) {
                webListener.showTitle(title);
            }
        }
        X5LogUtils.i("-------onReceivedTitle-------" + title + "----" + view.getUrl());
    }

    /**
     * 指定源的网页内容在没有设置权限状态下尝试使用地理位置API。
     * 从API24开始，此方法只为安全的源(https)调用，非安全的源会被自动拒绝
     *
     * @param origin                         origin
     * @param geolocationPermissionsCallback callback
     */
    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissionsCallback geolocationPermissionsCallback) {
        //boolean allow = true;   // 是否允许origin使用定位API
        //boolean retain = false; // 内核是否记住这次制授权
        //geolocationPermissionsCallback.invoke(origin, allow, retain);
        super.onGeolocationPermissionsShowPrompt(origin, geolocationPermissionsCallback);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int code = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (code != PackageManager.PERMISSION_GRANTED && context instanceof Activity) {
                // 还没有定位的授权，需要动态申请。
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
            }
        }
    }

    /**
     * 打开文件夹，扩展浏览器上传文件，3.0++版本
     *
     * @param uploadMsg  msg
     * @param acceptType type
     */
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        openFileChooserImpl(uploadMsg);
    }

    /**
     * 3.0--版本
     *
     * @param uploadMsg msg
     */
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        openFileChooserImpl(uploadMsg);
    }

    /**
     * 打开文件夹
     *
     * @param uploadMsg  msg
     * @param acceptType type
     * @param capture    capture
     */
    @Override
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        X5LogUtils.i("-------openFileChooser-------");
        openFileChooserImpl(uploadMsg);
    }

    /**
     * 为'<input type="file" />'显示文件选择器，返回false使用默认处理
     * For Android > 5.0
     *
     * @param webView           webview
     * @param uploadMsg         msg
     * @param fileChooserParams 参数
     * @return
     */
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> uploadMsg,
                                     FileChooserParams fileChooserParams) {
        X5LogUtils.i("-------onShowFileChooser-------");
        openFileChooserImplForAndroid5(uploadMsg);
        return true;
    }

    /**
     * 接收JavaScript控制台消息
     *
     * @param consoleMessage message
     * @return
     */
    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        String message = consoleMessage.message();
        ConsoleMessage.MessageLevel messageLevel = consoleMessage.messageLevel();
        String sourceId = consoleMessage.sourceId();
        X5LogUtils.i("-------onConsoleMessage-------" + message + "----" + sourceId);
        return super.onConsoleMessage(consoleMessage);
    }

    /**
     * js超时
     *
     * @return boolean
     */
    @Override
    public boolean onJsTimeout() {
        boolean jsTimeout = super.onJsTimeout();
        X5LogUtils.i("-------onJsTimeout----js是否超时---" + jsTimeout);
        return jsTimeout;
    }

    /**
     * 打开文件夹
     *
     * @param uploadMsg msg
     */
    private void openFileChooserImpl(ValueCallback<Uri> uploadMsg) {
        if (context != null && context instanceof Activity) {
            Activity activity = (Activity) context;
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            activity.startActivityForResult(Intent.createChooser(i, "文件选择"), FILE_CHOOSER_RESULT_CODE);
        }
    }

    /**
     * 打开文件夹，Android5.0以上
     *
     * @param uploadMsg msg
     */
    private void openFileChooserImplForAndroid5(ValueCallback<Uri[]> uploadMsg) {
        if (context != null && context instanceof Activity) {
            Activity activity = (Activity) context;
            mUploadMessageForAndroid5 = uploadMsg;
            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("image/*");
            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "图片选择");
            activity.startActivityForResult(chooserIntent, FILE_CHOOSER_RESULT_CODE_5);
        }
    }

    /**
     * 5.0以下 上传图片成功后的回调
     */
    public void uploadMessage(Intent intent, int resultCode) {
        if (null == mUploadMessage) {
            return;
        }
        Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
        mUploadMessage.onReceiveValue(result);
        mUploadMessage = null;
    }

    /**
     * 5.0以上 上传图片成功后的回调
     */
    public void uploadMessageForAndroid5(Intent intent, int resultCode) {
        if (null == mUploadMessageForAndroid5) {
            return;
        }
        Uri result = (intent == null || resultCode != RESULT_OK) ? null : intent.getData();
        if (result != null) {
            mUploadMessageForAndroid5.onReceiveValue(new Uri[]{result});
        } else {
            mUploadMessageForAndroid5.onReceiveValue(new Uri[]{});
        }
        mUploadMessageForAndroid5 = null;
    }
}
