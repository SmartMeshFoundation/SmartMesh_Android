package com.lingtuan.firefly.custom.contact;

import android.content.Context;
import android.util.AttributeSet;

import com.lingtuan.firefly.R;

public class ContactListViewImpl extends ContactListView{

	public ContactListViewImpl(Context context, AttributeSet attrs){
		super(context, attrs);
	}

	public void createScroller(Context context){
		mScroller = new IndexScroller(getContext(), this);
		mScroller.setAutoHide(autoHide);
		// style 1
		 mScroller.setShowIndexContainer(false);
		 mScroller.setIndexPaintColor(context.getResources().getColor(R.color.textColorHint));
		// style 2
//		mScroller.setShowIndexContainer(true);
//		mScroller.setIndexPaintColor(Color.WHITE);
		if (autoHide){
			mScroller.hide();
		}else{
			mScroller.show();
		}
	}
}
