package com.lingtuan.firefly.custom;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Layout;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.MotionEvent;

import com.lingtuan.firefly.util.Utils;

public class CustomLinkMovementMethod extends LinkMovementMethod {
	private static Context movementContext;
	private static CustomLinkMovementMethod linkMovementMethod = new CustomLinkMovementMethod();
	public static boolean isChattingLongClick = false;
	public static boolean isClickLink = false;
	public boolean onTouchEvent(android.widget.TextView widget,
			android.text.Spannable buffer, android.view.MotionEvent event) {
		int action = event.getAction();
		
		if(isChattingLongClick&&action == MotionEvent.ACTION_UP){//Long time not to enter
			isChattingLongClick = false;
			return false;
		}
		
		if (action == MotionEvent.ACTION_UP) {
			int x = (int) event.getX();
			int y = (int) event.getY();

			x -= widget.getTotalPaddingLeft();
			y -= widget.getTotalPaddingTop();

			x += widget.getScrollX();
			y += widget.getScrollY();

			Layout layout = widget.getLayout();
			int line = layout.getLineForVertical(y);
			int off = layout.getOffsetForHorizontal(line, x);

			URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);
			if (link.length != 0) {
				String url = link[0].getURL();
				if (url.contains("https") || url.contains("http") || url.contains("www")) {
					Utils.clickUrl(url,movementContext);
				} else if (url.contains("tel")) {
					try {
						if(!TextUtils.isEmpty(url)){
							Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							movementContext.startActivity(intent);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (url.contains("mailto")) {
					try {
						if(!TextUtils.isEmpty(url)){
							Intent data=new Intent(Intent.ACTION_SENDTO);
							data.setData(Uri.parse(url));
							movementContext.startActivity(data);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
				isClickLink=true;
				return true;
			}
			
		}
		return super.onTouchEvent(widget, buffer, event);
	}

	public static android.text.method.MovementMethod getInstance(Context c) {
		movementContext = c;
		return linkMovementMethod;
	}
}