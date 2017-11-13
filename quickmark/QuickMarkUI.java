package com.lingtuan.firefly.quickmark;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.contact.DiscussGroupJoinUI;
import com.lingtuan.firefly.ui.WebViewUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.UserBaseVo;
import com.lingtuan.firefly.xmpp.XmppUtils;

/**
 * Flicking a results page needs a key = result, value = String
 */
@SuppressLint("SetJavaScriptEnabled")
public class QuickMarkUI extends BaseActivity {

	private WebView loadWebView = null ;
	
	private ProgressBar progressBar1 = null ;
	
	private TextView mContent;
	
	@Override
	protected void setContentView() {
		setContentView(R.layout.quick_mark_result);
	}

	@Override
	protected void findViewById() {
		loadWebView = (WebView)findViewById(R.id.webView1);
	    loadWebView.getSettings().setJavaScriptEnabled(true);
		progressBar1 = (ProgressBar)findViewById(R.id.progressBar1);
		mContent = (TextView) findViewById(R.id.text);
	}

	@Override
	protected void setListener() {
		loadWebView.setWebViewClient(new WebViewClient(){
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				mHandler.sendEmptyMessage(0);
				super.onPageFinished(view, url);
			}
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				/**Is our host, opened by oneself, not our own, opened by the system*/
				view.loadUrl(url);
				return false;
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode,
										String description, String failingUrl) {
//				loadWebView.setVisibility(View.GONE);
				mHandler.sendEmptyMessage(0);
				super.onReceivedError(view, errorCode, description, failingUrl);
			}
			
		});
		
		loadWebView.setDownloadListener(new DownloadListener() {
		      public void onDownloadStart(String url, String userAgent,
										  String contentDisposition, String mimetype,
										  long contentLength) {
		        Intent i = new Intent(Intent.ACTION_VIEW);
		        i.setData(Uri.parse(url));
		        startActivity(i);
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
		
		setTitle(getString(R.string.qm_qm));
		String result = getIntent().getStringExtra("result");
		if(TextUtils.isEmpty(result)){
			showToast(getString(R.string.parse_error));
			Utils.exitActivityAndBackAnim(this, true);
			return;
		}else{
			Intent intent = new Intent(QuickMarkUI.this, WebViewUI.class);
			intent.putExtra("loadUrl", result);
			startActivity(intent);
			Utils.openNewActivityAnim(QuickMarkUI.this, true);
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		
	}
	
	/**Need to empty all data associated with the WebView to prevent infinite loop run out of memory*/
	@SuppressWarnings("deprecation")
	@Override
	protected void onDestroy() {
		if(loadWebView != null && loadWebView.getChildCount() != 0 ){
			loadWebView.removeAllViews();
			loadWebView.clearView();
			loadWebView.clearHistory();
			loadWebView = null ; 
		}
		super.onDestroy();
	}
	

	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg!=null ){
				if(msg.what == 0){
					progressBar1.setVisibility(View.GONE);
				}else{
					progressBar1.setVisibility(View.VISIBLE);
				}
			}
		}
	};
	
}
