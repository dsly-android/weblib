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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.widget.EditText;
import android.widget.Toast;

import com.dsly.weblib.R;
import com.dsly.weblib.utils.ToastUtils;


/**
 * @author cenxiaozhong
 * @date 2017/12/8
 * @since 3.0.0
 */
public class DefaultUIController extends AbsAgentWebUIController {

	private Resources mResources = null;
	private AppCompatActivity mActivity;

	public DefaultUIController(AppCompatActivity activity){
		mActivity = activity;
	}

	@Override
	public void onForceDownloadAlert(String url, final Handler.Callback callback) {
		onForceDownloadAlertInternal(callback);
	}

	private void onForceDownloadAlertInternal(final Handler.Callback callback) {
		Activity mActivity;
		if ((mActivity = this.mActivity) == null || mActivity.isFinishing()) {
			return;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			if (mActivity.isDestroyed()) {
				return;
			}
		}
		AlertDialog mAlertDialog = null;
		mAlertDialog = new AlertDialog.Builder(mActivity)
				.setTitle(mResources.getString(R.string.agentweb_tips))
				.setMessage(mResources.getString(R.string.agentweb_honeycomblow))
				.setNegativeButton(mResources.getString(R.string.agentweb_download), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (dialog != null) {
							dialog.dismiss();
						}
						if (callback != null) {
							callback.handleMessage(Message.obtain());
						}
					}
				})//
				.setPositiveButton(mResources.getString(R.string.agentweb_cancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						if (dialog != null) {
							dialog.dismiss();
						}
					}
				}).create();
		mAlertDialog.show();
	}

	@Override
	public void onShowMessage(String message, String from) {
		if (!TextUtils.isEmpty(from) && from.contains("performDownload")) {
			return;
		}
		ToastUtils.showShort(message);
	}

	@Override
	public void onPermissionsDeny(String[] permissions, String permissionType, String action) {
//		AgentWebUtils.toastShowShort(mActivity.getApplicationContext(), "权限被冻结");
	}
}
