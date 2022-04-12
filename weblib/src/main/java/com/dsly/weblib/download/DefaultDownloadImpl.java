/*
 * Copyright (C)  Justson(https://github.com/Justson/AgentWeb)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dsly.weblib.download;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.CookieManager;
import android.webkit.WebView;

import com.download.library.DownloadImpl;
import com.download.library.DownloadListenerAdapter;
import com.download.library.Extra;
import com.download.library.ResourceRequest;
import com.dsly.weblib.R;
import com.dsly.weblib.utils.WebkitCookieUtils;
import com.dsly.weblib.utils.X5LogUtils;
import com.dsly.weblib.utils.X5WebUtils;
import com.tencent.smtt.sdk.DownloadListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cenxiaozhong
 * @date 2017/5/13
 */
public class DefaultDownloadImpl implements DownloadListener {
    /**
     * Application Context
     */
    protected Context mContext;
    protected ConcurrentHashMap<String, ResourceRequest> mDownloadTasks = new ConcurrentHashMap<>();
    /**
     * Activity
     */
    protected WeakReference<Activity> mActivityWeakReference = null;
    /**
     * AbsAgentWebUIController
     */
    protected WeakReference<AbsAgentWebUIController> mAgentWebUIController;

    private static Handler mHandler = new Handler(Looper.getMainLooper());

    private boolean isInstallDownloader;

    protected DefaultDownloadImpl(AppCompatActivity activity) {
        this.mContext = activity.getApplicationContext();
        this.mActivityWeakReference = new WeakReference<Activity>(activity);
        this.mAgentWebUIController = new WeakReference<AbsAgentWebUIController>(AgentWebUIControllerImplBase.build(activity));
        try {
            DownloadImpl.getInstance(this.mContext);
            isInstallDownloader = true;
        } catch (Throwable throwable) {
            X5LogUtils.e( "implementation 'com.download.library:Downloader:x.x.x'");
            if (X5LogUtils.isDebug()) {
                throwable.printStackTrace();
            }
            isInstallDownloader = false;
        }
    }


    @Override
    public void onDownloadStart(final String url, final String userAgent, final String contentDisposition, final String mimetype, final long contentLength) {
        if (!isInstallDownloader) {
            X5LogUtils.e("unable start download " + url + "; implementation 'com.download.library:Downloader:x.x.x'");
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onDownloadStartInternal(url, userAgent, contentDisposition, mimetype, contentLength);
            }
        });
    }

    protected void onDownloadStartInternal(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        if (null == mActivityWeakReference.get() || mActivityWeakReference.get().isFinishing()) {
            return;
        }
        ResourceRequest resourceRequest = createResourceRequest(url);
        this.mDownloadTasks.put(url, resourceRequest);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> mList = null;
            if ((mList = checkNeedPermission()).isEmpty()) {
                preDownload(url);
            } else {
                Action mAction = Action.createPermissionsAction(mList.toArray(new String[]{}));
                mAction.setPermissionListener(getPermissionListener(url));
                AgentActionFragment.start(mActivityWeakReference.get(), mAction);
            }
        } else {
            preDownload(url);
        }
    }

    protected ResourceRequest createResourceRequest(String url) {
        return DownloadImpl.getInstance(this.mContext).with(url).setEnableIndicator(true).autoOpenIgnoreMD5();
    }

    protected AgentActionFragment.PermissionListener getPermissionListener(final String url) {
        return new AgentActionFragment.PermissionListener() {
            @Override
            public void onRequestPermissionsResult(@NonNull String[] permissions, @NonNull int[] grantResults, Bundle extras) {
                if (checkNeedPermission().isEmpty()) {
                    preDownload(url);
                } else {
                    if (null != mAgentWebUIController.get()) {
                        mAgentWebUIController
                                .get()
                                .onPermissionsDeny(
                                        checkNeedPermission().
                                                toArray(new String[]{}),
                                        AgentWebPermissions.ACTION_STORAGE, "Download");
                    }
                    X5LogUtils.e("储存权限获取失败~");
                }

            }
        };
    }

    protected List<String> checkNeedPermission() {
        List<String> deniedPermissions = new ArrayList<>();
        if (!X5WebUtils.hasPermission(mActivityWeakReference.get(), AgentWebPermissions.STORAGE)) {
            deniedPermissions.addAll(Arrays.asList(AgentWebPermissions.STORAGE));
        }
        return deniedPermissions;
    }

    protected void preDownload(String url) {
        // 移动数据
        if (!isForceRequest(url) &&
                X5WebUtils.checkNetworkType(mContext) > 1) {
            showDialog(url);
            return;
        }
        performDownload(url);
    }

    protected boolean isForceRequest(String url) {
        ResourceRequest resourceRequest = mDownloadTasks.get(url);
        if (null != resourceRequest) {
            return resourceRequest.getDownloadTask().isForceDownload();
        }
        return false;
    }

    protected void forceDownload(final String url) {
        ResourceRequest resourceRequest = mDownloadTasks.get(url);
        resourceRequest.setForceDownload(true);
        performDownload(url);
    }

    protected void showDialog(final String url) {
        Activity mActivity;
        if (null == (mActivity = mActivityWeakReference.get()) || mActivity.isFinishing()) {
            return;
        }
        AbsAgentWebUIController mAgentWebUIController;
        if (null != (mAgentWebUIController = this.mAgentWebUIController.get())) {
            mAgentWebUIController.onForceDownloadAlert(url, createCallback(url));
        }
    }

    protected Handler.Callback createCallback(final String url) {
        return new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                forceDownload(url);
                return true;
            }
        };
    }

    protected void performDownload(String url) {
        try {
            X5LogUtils.e("performDownload:" + url + " exist:" + DownloadImpl.getInstance(this.mContext).exist(url));
            // 该链接是否正在下载
            if (DownloadImpl.getInstance(mContext).exist(url)) {
                if (null != mAgentWebUIController.get()) {
                    mAgentWebUIController.get().onShowMessage(
                            mActivityWeakReference.get()
                                    .getString(R.string.agentweb_download_task_has_been_exist), "preDownload");
                }
                return;
            }
            ResourceRequest resourceRequest = mDownloadTasks.get(url);
            resourceRequest.addHeader("Cookie", WebkitCookieUtils.getCookie(url));
            taskEnqueue(resourceRequest);
        } catch (Throwable ignore) {
            if (X5LogUtils.isDebug()) {
                ignore.printStackTrace();
            }
        }
    }

    protected void taskEnqueue(ResourceRequest resourceRequest) {
        resourceRequest.enqueue(new DownloadListenerAdapter() {
            @Override
            public boolean onResult(Throwable throwable, Uri path, String url, Extra extra) {
                mDownloadTasks.remove(url);
                return super.onResult(throwable, path, url, extra);
            }
        });
    }

    public static DefaultDownloadImpl create(@NonNull AppCompatActivity activity) {
        return new DefaultDownloadImpl(activity);
    }
}
