package com.ycbjie.ycwebview;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.tencent.smtt.sdk.ValueCallback;
import com.dsly.weblib.inter.DefaultWebListener;
import com.dsly.weblib.utils.X5WebUtils;
import com.dsly.weblib.view.X5WebView;
import com.dsly.weblib.widget.WebProgress;

import org.json.JSONObject;

/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2019/9/17
 *     desc  : webView页面
 *     revise: 暂时先用假数据替代
 * </pre>
 */
public class NativeActivity extends AppCompatActivity implements View.OnClickListener {

    private X5WebView mWebView;
    private WebProgress progress;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() ==
                KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (mWebView.pageCanGoBack()) {
                //退出网页
                return mWebView.pageGoBack();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.resume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mWebView != null) {
            mWebView.stop();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_view);
        initView();
        initData();
    }


    public void initData() {
        findViewById(R.id.callJsNoParamsButton).setOnClickListener(this);
        findViewById(R.id.callJsOneParamsButton).setOnClickListener(this);
        findViewById(R.id.callJsMoreParamsButton).setOnClickListener(this);
        findViewById(R.id.jsJavaCommunicationButton).setOnClickListener(this);

        mWebView.getJsInterfaceHolder().addJavaObject("android", new AndroidInterface(this));
    }

    public void initView() {
        mWebView = findViewById(R.id.web_view);
        progress = findViewById(R.id.progress);
        progress.show();
        progress.setColor(this.getResources().getColor(R.color.colorAccent));

        mWebView.getX5WebChromeClient().setWebListener(interWebListener);
        mWebView.getX5WebViewClient().setWebListener(interWebListener);
        mWebView.setInitialScale(250);

        mWebView.loadUrl("file:///android_asset/js_interaction/hello.html");
    }

    private DefaultWebListener interWebListener = new DefaultWebListener() {
        @Override
        public void hindProgressBar() {
            progress.hide();
        }

        @Override
        public void showErrorView(@X5WebUtils.ErrorType int type) {
            switch (type) {
                //没有网络
                case X5WebUtils.ErrorMode.NO_NET:
                    break;
                //404，网页无法打开
                case X5WebUtils.ErrorMode.STATE_404:

                    break;
                //onReceivedError，请求网络出现error
                case X5WebUtils.ErrorMode.RECEIVED_ERROR:

                    break;
                //在加载资源时通知主机应用程序发生SSL错误
                case X5WebUtils.ErrorMode.SSL_ERROR:

                    break;
                default:
                    break;
            }
        }

        @Override
        public void startProgress(int newProgress) {
            progress.setWebProgress(newProgress);
        }

        @Override
        public void showTitle(String title) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //这个是处理回调逻辑
        mWebView.getX5WebChromeClient().uploadMessageForAndroid5(data, resultCode);
    }

    private void test() {
        //夜间模式，enable:true(日间模式)，enable：false（夜间模式）
        mWebView.setDayOrNight(true);
        //前进后退缓存，true表示缓存
        mWebView.setContentCacheEnable(true);
        //对于无法缩放的页面当用户双指缩放时会提示强制缩放，再次操作将触发缩放功能
        mWebView.setForcePinchScaleEnabled(true);
        //设置无痕模式
        mWebView.setShouldTrackVisitedLinks(true);
        //刘海屏适配
        mWebView.setDisplayCutoutEnable(true);
        //一次性删除所有缓存
        mWebView.clearAllWebViewCache(true);
        //缓存清除，针对性删除
        mWebView.clearCache();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.callJsNoParamsButton:
                mWebView.getJsAccessEntrace().quickCallJs("callByAndroid");
                break;
            case R.id.callJsOneParamsButton:
                mWebView.getJsAccessEntrace().quickCallJs("callByAndroidParam", "Hello ! Agentweb");
                break;
            case R.id.callJsMoreParamsButton:
                mWebView.getJsAccessEntrace().quickCallJs("callByAndroidMoreParams", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        Log.i("Info", "value:" + value);
                    }
                }, getJson(), "say:", " Hello! Agentweb");
                break;
            case R.id.jsJavaCommunicationButton:
                mWebView.getJsAccessEntrace().quickCallJs("callByAndroidInteraction", "你好Js");
                break;
            default:
                break;
        }
    }

    private String getJson() {
        String result = "";
        try {
            JSONObject mJSONObject = new JSONObject();
            mJSONObject.put("id", 1);
            mJSONObject.put("name", "Agentweb");
            mJSONObject.put("age", 18);
            result = mJSONObject.toString();
        } catch (Exception e) {

        }

        return result;
    }
}
