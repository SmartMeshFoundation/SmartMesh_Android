package com.lingtuan.firefly.custom;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.lingtuan.firefly.R;


public class LoadMoreListView extends ListView implements AbsListView.OnScrollListener {

	private RelativeLayout mFooterContainer;
	private RefreshListener listener;
	private boolean loadingMoreShow=false;
	private SwipeRefreshLayout swipeLayout;
	private AbsListView.OnScrollListener mOnScrollListener;
	public LoadMoreListView(Context paramContext) {
		super(paramContext);
		init(paramContext);
	}

	public LoadMoreListView(Context paramContext,
							AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		init(paramContext);
	}

	public LoadMoreListView(Context paramContext,
							AttributeSet paramAttributeSet, int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
		init(paramContext);
	}
	public void setSwipeLayout(SwipeRefreshLayout swipeLayout){
		this.swipeLayout=swipeLayout;
	}
    private void init(final Context paramContext) {

    	super.setOnScrollListener(this);
		mFooterContainer=(RelativeLayout) View.inflate(paramContext, R.layout.loading_more_layout, null);
		addFooterView(mFooterContainer);
		resetFooterState(false);
		mFooterContainer.setOnClickListener(null);
	}

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if(swipeLayout!=null)
        {
         int x = (int) ev.getX();
         int y = (int) ev.getY();
         int position = pointToPosition(x, y);
         if (position == 0) {  //viewpager页面保持左右滑动不受影响
        	 swipeLayout.setOnTouchListener(new OnTouchListener() {

 				@Override
 				public boolean onTouch(View v, MotionEvent event) {
 					// TODO Auto-generated method stub
 					return true;
 				}
 			 });
        	 return false;
         }
         else{
        	 swipeLayout.setOnTouchListener(new OnTouchListener() {

  				@Override
  				public boolean onTouch(View v, MotionEvent event) {
  					// TODO Auto-generated method stub
  					return false;
  				}
  			 });
        	 return super.onInterceptTouchEvent(ev);
         }
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void resetFooterState(boolean isShow)
	{
    	loadingMoreShow = isShow;
		if(isShow)
		{
			for(int i=0;i<mFooterContainer.getChildCount();i++)
			{
				mFooterContainer.getChildAt(i).setVisibility(View.VISIBLE);
			}
		}
		else{
			for(int i=0;i<mFooterContainer.getChildCount();i++)
			{
				mFooterContainer.getChildAt(i).setVisibility(View.GONE);
			}

		}
	}
    @Override
	public void onScroll(AbsListView paramAbsListView, int paramInt1,
						 int paramInt2, int paramInt3) {

    	if (this.mOnScrollListener != null) {
			this.mOnScrollListener.onScroll(paramAbsListView, paramInt1,
					paramInt2, paramInt3);
		}

	   if (loadingMoreShow&&paramAbsListView.getLastVisiblePosition() == paramAbsListView.getCount() - 1&&this.listener != null) {
		     listener.loadMore();
  	   }

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		if (this.mOnScrollListener != null) {
			this.mOnScrollListener.onScrollStateChanged(view, scrollState);
		}
	}
	public void setOnScrollListener(
			AbsListView.OnScrollListener paramOnScrollListener) {
		this.mOnScrollListener = paramOnScrollListener;
	}
	
	
	public void setOnRefreshListener(RefreshListener listener)
	{
		this.listener=listener;
	}
	public interface RefreshListener
	{
		void loadMore();
	}
}
