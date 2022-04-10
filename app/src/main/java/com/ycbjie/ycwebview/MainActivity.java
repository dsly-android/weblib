package com.ycbjie.ycwebview;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.tv_2_1).setOnClickListener(this);
        findViewById(R.id.tv_2_3).setOnClickListener(this);
        findViewById(R.id.tv_2_4).setOnClickListener(this);
        findViewById(R.id.tv_3).setOnClickListener(this);
        findViewById(R.id.tv_5).setOnClickListener(this);
        findViewById(R.id.tv_5_2).setOnClickListener(this);
        findViewById(R.id.tv_6_1).setOnClickListener(this);
        findViewById(R.id.tv_6_2).setOnClickListener(this);
        findViewById(R.id.tv_7_1).setOnClickListener(this);
        findViewById(R.id.tv_8).setOnClickListener(this);
        findViewById(R.id.tv_9).setOnClickListener(this);
        findViewById(R.id.tv_9_2).setOnClickListener(this);
        findViewById(R.id.tv_10).setOnClickListener(this);
        findViewById(R.id.tv_11).setOnClickListener(this);
        findViewById(R.id.tv_12).setOnClickListener(this);
        findViewById(R.id.tv_13).setOnClickListener(this);
        findViewById(R.id.tv_14_2).setOnClickListener(this);
        findViewById(R.id.tv_14_3).setOnClickListener(this);
        findViewById(R.id.tv_14_5).setOnClickListener(this);

        checkReadPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, 100);
        checkReadPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 101);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_2_1:
                startActivity(new Intent(this,SecondActivity.class));
                break;
            case R.id.tv_2_3:
                startActivity(new Intent(this, CacheWebViewActivity1.class));
                break;
            case R.id.tv_2_4:
                startActivity(new Intent(this, CacheWebViewActivity2.class));
                break;
            case R.id.tv_3:
                startActivity(new Intent(this,ThreeActivity.class));
                break;
            case R.id.tv_5:
                startActivity(new Intent(this,FiveActivity.class));
                break;
            case R.id.tv_5_2:
                startActivity(new Intent(this,FiveActivity2.class));
                break;
            case R.id.tv_6_1:
                startActivity(new Intent(this,SixActivity.class));
                break;
            case R.id.tv_6_2:
                startActivity(new Intent(this,SixActivity2.class));
                break;
            case R.id.tv_7_1:
                startActivity(new Intent(this,NativeActivity.class));
                break;
            case R.id.tv_8:
                startActivity(new Intent(this,EightActivity.class));
                break;
            case R.id.tv_9:
                startActivity(new Intent(this,FileDisplayActivity.class));
                break;
            case R.id.tv_9_2:
                startActivity(new Intent(this,DeepLinkActivity.class));
                break;
            case R.id.tv_10:
                startActivity(new Intent(this, TenActivity.class));
                break;
            case R.id.tv_11:
                openLink(this,"https://juejin.im/user/5939433efe88c2006afa0c6e/posts");
                break;
            case R.id.tv_12:
                startActivity(new Intent(this, DownActivity.class));
                break;
            case R.id.tv_13:
                startActivity(new Intent(this, ScrollViewActivity.class));
                break;
            case R.id.tv_14_2:
                startActivity(new Intent(this, WebViewActivity2.class));
                break;
            case R.id.tv_14_3:
                startActivity(new Intent(this, ThreeActivity2.class));
                break;
            case R.id.tv_14_5:
                //使用webView设置白名单操作
                startActivity(new Intent(this, WebViewWhiteActivity.class));
                break;
            default:
                break;
        }
    }

    /**
     * 使用浏览器打开链接
     */
    public void openLink(Context context, String content) {
        if (!TextUtils.isEmpty(content) && content.startsWith("http")) {
            Uri issuesUrl = Uri.parse(content);
            Intent intent = new Intent(Intent.ACTION_VIEW, issuesUrl);
            context.startActivity(intent);
        }
    }


    /**
     * 判断是否有某项权限
     * @param string_permission                 权限
     * @param request_code                      请求码
     * @return
     */
    public boolean checkReadPermission(Context context, String string_permission, int request_code) {
        boolean flag = false;
        int permission = ContextCompat.checkSelfPermission(context, string_permission);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            //已有权限
            flag = true;
        } else {
            //申请权限
            ActivityCompat.requestPermissions((Activity) context, new String[]{string_permission}, request_code);
        }
        return flag;
    }

}
