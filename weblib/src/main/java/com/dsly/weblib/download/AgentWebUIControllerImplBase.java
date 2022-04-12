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
import android.net.http.SslError;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;


/**
 * @author cenxiaozhong
 * @date 2017/12/6
 * @since 3.0.0
 */
public class AgentWebUIControllerImplBase extends AbsAgentWebUIController {

    private AppCompatActivity activity;

    public static AbsAgentWebUIController build(AppCompatActivity activity) {
        return new AgentWebUIControllerImplBase(activity);
    }

    public AgentWebUIControllerImplBase(AppCompatActivity activity){
        this.activity = activity;
    }

    @Override
    public void onForceDownloadAlert(String url, Handler.Callback callback) {
        getDelegate(activity).onForceDownloadAlert(url, callback);
    }

    @Override
    public void onShowMessage(String message, String from) {
        getDelegate(activity).onShowMessage(message, from);
    }

    @Override
    public void onPermissionsDeny(String[] permissions, String permissionType, String action) {
        getDelegate(activity).onPermissionsDeny(permissions, permissionType, action);
    }
}