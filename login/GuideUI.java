package com.lingtuan.firefly.login;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.viewpager.CustomViewPager;
import com.lingtuan.firefly.custom.viewpager.FixedSpeedScroller;
import com.lingtuan.firefly.custom.viewpager.ZoomOutPageTransformer;
import com.lingtuan.firefly.ui.MainFragmentUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.xmpp.XmppUtils;

import java.lang.reflect.Field;



/**
 *Implementation guide page
 */
public class GuideUI extends BaseActivity implements ViewPager.OnPageChangeListener {

    private CustomViewPager mPager;
    private GuideAdapter mAdapter = null;
    private TextView regsterBtn;
    private TextView loginBtn;
    private RelativeLayout ryContainer;
    private FixedSpeedScroller scroller;
    private int mPosition;

    private boolean isLogin;

    private long firstTime;


    @Override
    protected void setContentView() {
        setContentView(R.layout.guide_layout);
        isLogin = getIntent().getBooleanExtra("isLogin", false);
    }

    @Override
    protected void findViewById() {
        ryContainer = (RelativeLayout) findViewById(R.id.ryContainer);
        regsterBtn = (TextView) findViewById(R.id.guide_register);
        loginBtn = (TextView) findViewById(R.id.guide_login);
        mPager = (CustomViewPager) findViewById(R.id.pager);
    }

    @Override
    protected void setListener() {
        regsterBtn.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
    }

    @Override
    protected void initData() {

        IntentFilter filter = new IntentFilter(Constants.ACTION_CLOSE_GUID);
        LocalBroadcastManager.getInstance(GuideUI.this).registerReceiver(mBroadcastReceiver, filter);

        regsterBtn.setVisibility(isLogin ? View.GONE : View.VISIBLE);
        loginBtn.setText(isLogin ? getString(R.string.start_smt) : getString(R.string.login));

        /**
         * The event distribution of Viewpager in the container to the Viewpager
         */
        ryContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mPager.dispatchTouchEvent(event);
            }
        });

        mAdapter = new GuideAdapter(this);
        mPager.setAdapter(mAdapter);

        //Set the number of cache number to show
        mPager.setOffscreenPageLimit(4);
        mPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.viewpager_margin));
        //Set switch animation
        mPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mPager.addOnPageChangeListener(this);
        setViewPagerSpeed(250);
    }


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && (Constants.ACTION_CLOSE_GUID.equals(intent.getAction()))) {
               finish();
            }
        }
    };

    /**
     * Set the ViewPager switching speed
     *
     * @param duration
     */
    private void setViewPagerSpeed(int duration) {
        try {
            Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            scroller = new FixedSpeedScroller(GuideUI.this, new AccelerateInterpolator());
            field.set(mPager, scroller);
            scroller.setmDuration(duration);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - firstTime > 2000){
            MyToast.showToast(GuideUI.this,getString(R.string.exit_app));
            firstTime = System.currentTimeMillis();
        }else{
            finish();
            System.exit(0);
        }
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.guide_register:
                startActivity(new Intent(GuideUI.this, RegistUI.class));
                Utils.openNewActivityAnim(GuideUI.this, false);
                break;
            case R.id.guide_login:
                if (isLogin) {
                    XmppUtils.loginXmppForNextApp(GuideUI.this);
                    startActivity(new Intent(GuideUI.this, MainFragmentUI.class));
                    Utils.openNewActivityAnim(GuideUI.this, true);
                } else {
                    startActivity(new Intent(GuideUI.this, LoginUI.class));
                    Utils.openNewActivityAnim(GuideUI.this, false);
                }
                break;

        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mPosition = position;
    }

    @Override
    public void onPageSelected(int position) {
        // when fingers left sliding speed is greater than 2000 viewpager right (note the item + 2)
        if (mPager.getSpeed() < -1800) {
            mPager.setCurrentItem(mPosition + 2);
            mPager.setSpeed(0);
        } else if (mPager.getSpeed() > 1800 && mPosition > 0) {
            //When your finger right sliding speed is greater than 2000 viewpager left (note item 1)
            mPager.setCurrentItem(mPosition - 1);
            mPager.setSpeed(0);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(GuideUI.this).unregisterReceiver(mBroadcastReceiver);
    }
}
