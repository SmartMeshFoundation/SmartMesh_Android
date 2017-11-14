package com.lingtuan.firefly.ui;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.util.Utils;

/**
 * Displays the text interface need to pass a key = text
 */
public class ShowTextUI extends BaseActivity{

	private TextView tv;
	private CharSequence showContent;
	
	@Override
	protected void setContentView() {
		setContentView(R.layout.ui_show_text);
	}

	@Override
	protected void findViewById() {
		tv = (TextView) findViewById(R.id.show_text);
	}

	@Override
	protected void setListener() {
		findViewById(R.id.bg).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.exitActivityAndBackAnim(ShowTextUI.this, true);
				
			}
		});
		
	}

	@Override
	protected void initData() {
		if(getIntent() != null && getIntent().getExtras() != null){
			try {
				showContent = getIntent().getExtras().getString("text");
			} catch (Exception e) {
			}
		}
		if(!TextUtils.isEmpty(showContent)){
			
			tv.setText(NextApplication.mSmileyParser.addSmileySpans1(showContent));
			
		}else {
			Utils.exitActivityAndBackAnim(this, true);
		}
		
	}

}
