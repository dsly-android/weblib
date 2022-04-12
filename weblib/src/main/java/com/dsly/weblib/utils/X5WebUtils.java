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
package com.dsly.weblib.utils;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.AppOpsManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.os.EnvironmentCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebView;

import com.dsly.weblib.R;
import com.dsly.weblib.cache.WebViewCacheDelegate;
import com.dsly.weblib.cache.WebViewCacheWrapper;
import com.dsly.weblib.download.AbsAgentWebUIController;
import com.dsly.weblib.tools.WebViewException;
import com.dsly.weblib.view.X5WebView;
import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;
import com.dsly.weblib.cache.WebCacheType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2019/9/10
 *     desc  : WebView工具类
 *     revise: demo地址：https://github.com/yangchong211/YCWebView
 * </pre>
 */
public final class X5WebUtils {

    /**
     * 全局上下文
     */
    private static Application application;

    /**
     * 是否可以长按下载图片
     */
    private static boolean mIsLongClick = true;

    /**
     * 是否使用缓存
     */
    private static boolean mUseCustomCache = false;

    /**
     * 不能直接new，否则抛个异常
     */
    private X5WebUtils() throws WebViewException {
        throw new WebViewException(1, "u can't instantiate me...");
    }

    /**
     * 初始化腾讯x5浏览器webView，建议在application初始化
     *
     * @param context 上下文
     */
    public static void init(final Context context,boolean isLongClick,boolean useCustomCache) {
        if (context instanceof Application) {
            application = (Application) context;
            //初始化X5内核,通过服务去加载,避免ANR
            HashMap map = new HashMap<String, Object>();
            map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
            // 通过服务去下载
            map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
            QbSdk.initTbsSettings(map);
            // 非wifi也支持下载
            QbSdk.setDownloadWithoutWifi(true);
            //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
            QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
                @Override
                public void onViewInitFinished(boolean arg0) {
                    //x5內核初始化完成的回调，为true表示x5内核加载成功
                    //否则表示x5内核加载失败，会自动切换到系统内核。
                    X5LogUtils.i("app" + " onViewInitFinished is " + arg0);
                }

                @Override
                public void onCoreInitFinished() {
                    X5LogUtils.i("app" + "onCoreInitFinished ");
                }
            };
            //x5内核初始化接口
            QbSdk.initX5Environment(context, cb);
            mIsLongClick = isLongClick;
            mUseCustomCache = useCustomCache;
        } else {
            throw new UnsupportedOperationException("context must be application...");
        }
    }

    /**
     * 初始化缓存
     *
     * @param application 上下文
     */
    public static void initCache(Application application, String path) {
        if (path == null || path.length() == 0) {
            path = "CacheWebView";
        }
        //1.创建委托对象
        WebViewCacheDelegate webViewCacheDelegate = WebViewCacheDelegate.getInstance();
        //2.创建调用处理器对象，实现类
        WebViewCacheWrapper.Builder builder = new WebViewCacheWrapper.Builder(application);
        //设置缓存路径，默认getCacheDir，名称CacheWebViewCache
        builder.setCachePath(new File(application.getCacheDir().toString(), path))
                //设置缓存大小，默认100M
                .setCacheSize(1024 * 1024 * 100)
                //设置本地路径
                //.setAssetsDir("yc")
                //设置http请求链接超时，默认20秒
                .setConnectTimeoutSecond(20)
                //设置http请求链接读取超时，默认20秒
                .setReadTimeoutSecond(20)
                //设置缓存为正常模式，默认模式为强制缓存静态资源
                .setCacheType(WebCacheType.FORCE);
        webViewCacheDelegate.init(builder);
    }

    /**
     * 获取全局上下文
     *
     * @return 上下文
     */
    public static Application getApplication() {
        return application;
    }

    public static boolean isLongClick() {
        return mIsLongClick;
    }

    public static boolean isUseCustomCache() {
        return mUseCustomCache;
    }

    /**
     * 判断当前url是否在白名单中
     *
     * @param arrayList 白名单集合
     * @param url       url
     * @return
     */
    public static boolean isWhiteList(ArrayList<String> arrayList, String url) {
        if (url == null) {
            return false;
        }
        if (arrayList == null || arrayList.size() == 0) {
            return false;
        }
        //重要提醒：建议只使用https协议通信，避免中间人攻击
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return false;
        }
        //提取host
        String host = "";
        try {
            //提取host，如果需要校验Path可以通过url.getPath()获取
            host = Uri.parse(url).getHost();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < arrayList.size(); i++) {
            if (host != null && host.equals(arrayList.get(i))) {
                //是咱们自己的host
                return true;
            }
        }
        //不在白名单内
        return false;
    }


    /**
     * 判断网络是否连接
     * <p>需添加权限
     * {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />}</p>
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isConnected(Context context) {
        if (context == null) {
            return false;
        }
        NetworkInfo info = getActiveNetworkInfo(context);
        return info != null && info.isConnected();
    }

    /**
     * 获取活动网络信息
     * <p>需添加权限
     * {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />}</p>
     *
     * @return NetworkInfo
     */
    @SuppressLint("MissingPermission")
    private static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return null;
        }
        return manager.getActiveNetworkInfo();
    }

    /**
     * Return whether the activity is alive.
     *
     * @param context The context.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isActivityAlive(final Context context) {
        return isActivityAlive(getActivityByContext(context));
    }

    /**
     * Return the activity by context.
     *
     * @param context The context.
     * @return the activity by context.
     */
    public static Activity getActivityByContext(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        }
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }


    /**
     * Return whether the activity is alive.
     *
     * @param activity The activity.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isActivityAlive(final Activity activity) {
        return activity != null && !activity.isFinishing()
                && (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !activity.isDestroyed());
    }


    /**
     * 注解限定符
     */
    @IntDef({ErrorMode.NO_NET, ErrorMode.STATE_404, ErrorMode.RECEIVED_ERROR, ErrorMode.SSL_ERROR,
            ErrorMode.TIME_OUT, ErrorMode.STATE_500, ErrorMode.ERROR_PROXY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ErrorType {
    }

    /**
     * 异常状态模式
     * NO_NET                       没有网络
     * STATE_404                    404，网页无法打开
     * RECEIVED_ERROR               onReceivedError，请求网络出现error
     * SSL_ERROR                    在加载资源时通知主机应用程序发生SSL错误
     * TIME_OUT                     网络连接超时
     * STATE_500                    服务器异常
     * ERROR_PROXY                  代理异常
     */
    @Retention(RetentionPolicy.SOURCE)
    public @interface ErrorMode {
        int NO_NET = 1001;
        int STATE_404 = 1002;
        int RECEIVED_ERROR = 1003;
        int SSL_ERROR = 1004;
        int TIME_OUT = 1005;
        int STATE_500 = 1006;
        int ERROR_PROXY = 1007;
    }

    /**
     * 判断是否为重定向url
     *
     * @param url 原始链接
     * @return True                 为重定向链接
     */
    public static boolean shouldSkipUrl(@Nullable String url) {
        if (TextUtils.isEmpty(url)) {
            return true;
        }
        Uri uri = Uri.parse(url);
        final String host = uri.getHost();
        //skip redirect
        if (!TextUtils.isEmpty(getKdtUnionUrl(uri))) {
            return true;
        }
        //skip 'about:blank'
        if (TextUtils.isEmpty(host)) {
            return true;
        }
        return false;
    }

    @Nullable
    private static String getKdtUnionUrl(@NonNull Uri uri) {
        return uri.isOpaque() ? null : uri.getQueryParameter("redirect_uri");
    }

    public static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    public static boolean isJson(String target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        }
        boolean tag = false;
        try {
            if (target.startsWith("[")) {
                new JSONArray(target);
            } else {
                new JSONObject(target);
            }
            tag = true;
        } catch (JSONException ignore) {
            tag = false;
        }
        return tag;
    }

    public static boolean isEmptyCollection(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static File createImageFile(Context context) {
        File mFile = null;
        try {
            String timeStamp =
                    new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
            String imageName = String.format("aw_%s.jpg", timeStamp);
            mFile = createFileByName(context, imageName, true);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return mFile;
    }

    public static File createFileByName(Context context, String name, boolean cover) throws IOException {
        String path = getAgentWebFilePath(context);
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        File mFile = new File(path, name);
        if (mFile.exists()) {
            if (cover) {
                mFile.delete();
                mFile.createNewFile();
            }
        } else {
            mFile.createNewFile();
        }
        return mFile;
    }

    public static String getAgentWebFilePath(Context context) {
        if (!TextUtils.isEmpty(WebkitCookieUtils.AGENTWEB_FILE_PATH)) {
            return WebkitCookieUtils.AGENTWEB_FILE_PATH;
        }
        String dir = getDiskExternalCacheDir(context);
        File mFile = new File(dir, WebkitCookieUtils.FILE_CACHE_PATH);
        try {
            if (!mFile.exists()) {
                mFile.mkdirs();
            }
        } catch (Throwable throwable) {
            X5LogUtils.i("create dir exception");
        }
        X5LogUtils.i("path:" + mFile.getAbsolutePath() + "  path:" + mFile.getPath());
        return WebkitCookieUtils.AGENTWEB_FILE_PATH = mFile.getAbsolutePath();
    }

    static String getDiskExternalCacheDir(Context context) {
        File mFile = context.getExternalCacheDir();
        if (Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(mFile))) {
            return mFile.getAbsolutePath();
        }
        return null;
    }

    public static Intent getIntentCaptureCompat(Context context, File file) {
        Intent mIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri mUri = getUriFromFile(context, file);
        mIntent.addCategory(Intent.CATEGORY_DEFAULT);
        mIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
        return mIntent;
    }

    public static Intent getIntentVideoCompat(Context context, File file){
        Intent mIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        Uri mUri = getUriFromFile(context, file);
        mIntent.addCategory(Intent.CATEGORY_DEFAULT);
        mIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
        return mIntent;
    }

    public static Uri getUriFromFile(Context context, File file) {
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = getUriFromFileForN(context, file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    static Uri getUriFromFileForN(Context context, File file) {
        Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".AgentWebFileProvider", file);
        return fileUri;
    }

    public static File createVideoFile(Context context){
        File mFile = null;
        try {
            String timeStamp =
                    new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
            String imageName = String.format("aw_%s.mp4", timeStamp);  //默认生成mp4
            mFile = createFileByName(context, imageName, true);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return mFile;
    }

    public static boolean hasPermission(@NonNull Context context, @NonNull String... permissions) {
        return hasPermission(context, Arrays.asList(permissions));
    }

    public static boolean hasPermission(@NonNull Context context, @NonNull List<String> permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String permission : permissions) {
            int result = ContextCompat.checkSelfPermission(context, permission);
            if (result == PackageManager.PERMISSION_DENIED) {
                return false;
            }
            String op = AppOpsManagerCompat.permissionToOp(permission);
            if (TextUtils.isEmpty(op)) {
                continue;
            }
            result = AppOpsManagerCompat.noteProxyOp(context, op, context.getPackageName());
            if (result != AppOpsManagerCompat.MODE_ALLOWED) {
                return false;
            }
        }
        return true;
    }

    public static int checkNetworkType(Context context) {
        int netType = 0;
        //连接管理对象
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //获取NetworkInfo对象
        @SuppressLint("MissingPermission") NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        switch (networkInfo.getType()) {
            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_WIMAX:
            case ConnectivityManager.TYPE_ETHERNET:
                return 1;
            case ConnectivityManager.TYPE_MOBILE:
                switch (networkInfo.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_LTE:  // 4G
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                        return 2;
                    case TelephonyManager.NETWORK_TYPE_UMTS: // 3G
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        return 3;
                    case TelephonyManager.NETWORK_TYPE_GPRS: // 2G
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        return 4;
                    default:
                        return netType;
                }

            default:
                return netType;
        }
    }

}
