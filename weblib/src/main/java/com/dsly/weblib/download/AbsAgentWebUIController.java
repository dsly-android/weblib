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
import android.app.Dialog;
import android.net.http.SslError;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;


/**
 * 该类统一控制了与用户交互的界面
 *
 * @author cenxiaozhong
 * @since 3.0.0
 */
public abstract class AbsAgentWebUIController {

    protected AbsAgentWebUIController mAgentWebUIControllerDelegate;

    protected AbsAgentWebUIController create(AppCompatActivity activity) {
        return new DefaultUIController(activity);
    }

    protected AbsAgentWebUIController getDelegate(AppCompatActivity activity) {
        AbsAgentWebUIController mAgentWebUIController = this.mAgentWebUIControllerDelegate;
        if (mAgentWebUIController == null) {
            this.mAgentWebUIControllerDelegate = mAgentWebUIController = create(activity);
        }
        return mAgentWebUIController;
    }

    /**
     * 强制下载弹窗
     *
     * @param url      当前下载地址。
     * @param callback 用户操作回调回调
     */
    public abstract void onForceDownloadAlert(String url, Handler.Callback callback);

    /**
     * @param message 消息
     * @param intent  说明message的来源，意图
     */
    public abstract void onShowMessage(String message, String intent);

    /**
     * 当权限被拒回调该方法
     *
     * @param permissions
     * @param permissionType
     * @param action
     */
    public abstract void onPermissionsDeny(String[] permissions, String permissionType, String action);
}