package com.lenovo.feizai.parking.activity;

import android.content.Intent;
import android.os.Build;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.lenovo.feizai.parking.R;
import com.lenovo.feizai.parking.base.BaseActivity;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author feizai
 * @date 2021/5/8 0008 下午 4:21:27
 */
public class WebActivity extends BaseActivity {

    @BindView(R.id.web)
    WebView webView;

    public WebActivity() {
        super(R.layout.activity_web);
    }

    @Override
    protected void initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptCanOpenWindowsAutomatically(true);//设置js可以直接打开窗口，如window.open()，默认为false
        settings.setJavaScriptEnabled(true);//是否允许执行js，默认为false。设置true时，会提醒可能造成XSS漏洞
        settings.setSupportZoom(true);//是否可以缩放，默认true
//        settings.setBuiltInZoomControls(true);//是否显示缩放按钮，默认false
        settings.setUseWideViewPort(true);//设置此属性，可任意比例缩放。大视图模式
        settings.setLoadWithOverviewMode(true);//和setUseWideViewPort(true)一起解决网页自适应问题
//        settings.setAppCacheEnabled(true);//是否使用缓存
        settings.setDomStorageEnabled(true);//DOM Storage
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://map.baidu.com/zt/client/privacy/index.html");
        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN){
                    if(keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()){
                        //表示按返回键时的操作
                        webView.goBack();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @OnClick(R.id.back)
    public void back() {
        finish();
    }

}
