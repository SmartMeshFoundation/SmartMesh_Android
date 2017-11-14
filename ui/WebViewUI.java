package com.lingtuan.firefly.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;

import com.lingtuan.firefly.custom.AdvancedWebView;
import com.lingtuan.firefly.util.MD5Util;
import com.lingtuan.firefly.util.Utils;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;


/**
 * @description All about us page page load
 */
public class WebViewUI extends BaseActivity {

    private AdvancedWebView loadWebView = null;
    private ImageView loadImageView = null;
    private String loadUrl = null;

    private String title = "";
    private String webImageUrl;//Web page icon
    private String webTitle = "";//The page title, just in the WebView page title bar display
    private String shareTitle;//Share the title, sharing out as the title, if use webTitle is empty
    private Dialog mProgressDialog;

    private ImageView rightBtn;
    private ProgressBar loading;
    private boolean finishLoading = false;

    /**
     * Whether the upper right corner of the share button
     */
    private boolean isHiddenRightBtn = false;

    private String yueniDescription;
    private String yueniImg;
    private String yueniLink;

    @Override
    protected void setContentView() {
        setContentView(R.layout.load_aboutus_webview);
        getPassData();
    }

    private void getPassData() {
        if (getIntent() != null) {
            loadUrl = getIntent().getExtras().getString("loadUrl");
            title = getIntent().getExtras().getString("title");
            isHiddenRightBtn = getIntent().getExtras().getBoolean("isHiddenRightBtn");
        }
    }

    @Override
    protected void findViewById() {
        loadWebView = (AdvancedWebView) findViewById(R.id.webView1);
        loadImageView = (ImageView) findViewById(R.id.webImage);
        rightBtn = (ImageView) findViewById(R.id.detail_set);
        loading = (ProgressBar) findViewById(R.id.loading);
    }

    class Utility {
        @JavascriptInterface
        public void setValue(String a, String b, String c, String d) {

        }
//		@JavascriptInterface
//		public String getValue(){
//			return "eos";
//		}
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void setListener() {//Refactoring agent, prevent jump out
        rightBtn.setOnClickListener(this);
        rightBtn.setVisibility(View.VISIBLE);
        loadWebView.getSettings().setJavaScriptEnabled(true);//wsc
        /*Adaptive screen size*/
        loadWebView.getSettings().setUseWideViewPort(true);
//		loadWebView.getSettings().setLoadWithOverviewMode(true);
        /*Special content must be opened*/
        loadWebView.getSettings().setDomStorageEnabled(true);
        loadWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        //Start the cache
        loadWebView.getSettings().setAppCacheEnabled(true);
        //Priority to improve rendering
        loadWebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        loadWebView.setWebChromeClient(new WebChromeClient());
        loadWebView.requestFocus(View.FOCUS_DOWN);

        if (loadUrl.contains("iyueni.com")) {
            loadWebView.addJavascriptInterface(new MyJavaScriptInterface(), "jyueni");
        } else {
            loadWebView.addJavascriptInterface(new MyJavaScriptInterface2(), "jyueni");
        }
        String defaultAgent = loadWebView.getSettings().getUserAgentString();
        String ourAgent = defaultAgent.concat(" firefly").concat(" version=".concat(String.valueOf(Utils.getVersionName(WebViewUI.this))).concat("&"));
        loadWebView.getSettings().setUserAgentString(ourAgent);


        loadWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String Title) {
                super.onReceivedTitle(view, Title);
                webTitle = Title;
                if (TextUtils.isEmpty(title)) {
                    setTitle(Title);
                }
            }


        });
        loadWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                finishLoading = false;
                webImageUrl = null;
                loading.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                loadUrl = url;
                finishLoading = true;
                loading.setVisibility(View.GONE);
                view.loadUrl("javascript:window.jyueni.processHTML(document.getElementsByTagName('img')[0].src);");
//                if (loadUrl.contains("iyueni.com")) {
//                    view.loadUrl("javascript:window.jyueni.processYueNiHTMLDescription(document.getElementsByName('description')[0].content);");
//                    view.loadUrl("javascript:window.jyueni.processYueNiHTMLImage(document.getElementsByName('share_imgUrl')[0].content);");
//                    view.loadUrl("javascript:window.jyueni.processYueNiHTMLLink(document.getElementsByName('share_link')[0].content);");
//                }
            }
        });

        loadWebView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype, long contentLength) {
                if (!TextUtils.isEmpty(url)) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && loadWebView.canGoBack()) {
            loadWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void initData() {
        setTitle(title);
        rightBtn.setVisibility(View.GONE);
        if (TextUtils.isEmpty(loadUrl)) {
            showToast(getString(R.string.pager_error));
        } else {
            if (!loadUrl.startsWith("http://") && !loadUrl.startsWith("https://")&&!loadUrl.startsWith("file:///")) {
                loadUrl = "http://" + loadUrl;
            }
            loadWebView.loadUrl(loadUrl);
        }
    }


    /**
     * Need to empty all data associated with the WebView to prevent infinite loop run out of memory
     */
    @Override
    protected void onDestroy() {
        if (loadWebView != null) {
            loadWebView.removeAllViews();
            loadWebView.clearHistory();
            loadWebView.stopLoading();
            loadWebView.destroy();
            loadWebView = null;
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    class MyJavaScriptInterface2 {
        @JavascriptInterface
        public void processHTML(String imageUrl) {
            webImageUrl = imageUrl;

        }
    }

    class MyJavaScriptInterface {
        @JavascriptInterface
        public void processHTML(String imageUrl) {
            webImageUrl = imageUrl;

        }

        @JavascriptInterface
        public void setTitle(String title) {
            shareTitle = title;
        }

        @JavascriptInterface
        public void setDescription(String description) {
            yueniDescription = description;
        }

        @JavascriptInterface
        public void setImage(String imageUrl) {
            yueniImg = imageUrl;
        }

        @JavascriptInterface
        public void setLink(String link) {
            yueniLink = link;
        }

        @JavascriptInterface
        public String getUid() {
            return NextApplication.myInfo.getLocalId();
        }

        @JavascriptInterface
        public String getToken() {
            return MD5Util.MD5Encode(NextApplication.myInfo.getToken(), null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (loadWebView != null) {
            loadWebView.onActivityResult(requestCode, resultCode, intent);
        }
    }
}
