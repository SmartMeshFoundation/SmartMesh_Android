package com.lingtuan.firefly.discover;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.adapter.MainFoundAdapter;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.offline.AppNetService;
import com.lingtuan.firefly.offline.vo.WifiPeopleVO;
import com.lingtuan.firefly.ui.AlertActivity;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MyDialogFragment;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.UserBaseVo;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class DiscoverUI extends BaseActivity implements AdapterView.OnItemClickListener, RadarViewGroup.IRadarClickListener {

    //Return key hidden
    @BindView(R.id.app_back)
    ImageView mBack;
    @BindView(R.id.app_right)
    ImageView app_right;
    @BindView(R.id.app_right_btn)
    ImageView selectImg;//screening
    @BindView(R.id.foundBg)
    LinearLayout foundBg;
    @BindView(R.id.offlineList)
    ListView offlineList;
    @BindView(R.id.radar)
    RadarViewGroup radarViewGroup;
    @BindView(R.id.empty_text)
    TextView emptyTextView;
    @BindView(R.id.empty_like_rela)
    RelativeLayout emptyRela;
    @BindView(R.id.app_title_rela)
    RelativeLayout appTitleBg;
    @BindView(R.id.titleLine)
    View titleLine;
    @BindView(R.id.app_title)
    TextView appTitle;

    private ArrayList<WifiPeopleVO> mDatas;
    private MainFoundAdapter mAdapter;
    private boolean isShowList;//Whether the display lis

    private boolean isBound = false;


    @Override
    protected void setContentView() {
        setContentView(R.layout.discover_fragment_layout);
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {

        shoSmartMeshDialog();

        // -1 default  0 close  1 open
        int openSmartMesh = MySharedPrefs.readInt1(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_NO_NETWORK_COMMUNICATION + NextApplication.myInfo.getLocalId());
        if (openSmartMesh == 1){
            isBound = bindService(new Intent(this, AppNetService.class), serviceConn, Activity.BIND_AUTO_CREATE);
        }

        IntentFilter filter = new IntentFilter(Constants.OFFLINE_MEMBER_LIST);//message Session record distribution
        filter.addAction(Constants.OPEN_SMARTMESH_NETWORE);//bind service
        filter.addAction(Constants.CLOSE_SMARTMESH_NETWORE);//unbind service
        registerReceiver(mBroadcastReceiver, filter);

        mBack.setVisibility(View.VISIBLE);
        app_right.setVisibility(View.VISIBLE);
        mBack.setImageResource(R.drawable.icon_white_back);
        app_right.setImageResource(R.drawable.icon_found_white);
        selectImg.setImageResource(R.drawable.icon_select);
        appTitleBg.setBackgroundResource(R.color.colorFound);
        titleLine.setBackgroundResource(R.color.colorFound);
        appTitle.setTextColor(getResources().getColor(R.color.textColor));
        appTitle.setText(getString(R.string.main_discover));
        Utils.setStatusBar(DiscoverUI.this,2);

        int width = getResources().getDisplayMetrics().widthPixels*5/6;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, width);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        radarViewGroup.setLayoutParams(layoutParams);
        radarViewGroup.setiRadarClickListener(this);

        mAdapter = new MainFoundAdapter(DiscoverUI.this, null);
        offlineList.setAdapter(mAdapter);
    }

    private void shoSmartMeshDialog() {

        if (NextApplication.myInfo == null){
            return;
        }
        // -1 default  0 close  1 open
        int openSmartMesh = MySharedPrefs.readInt1(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_NO_NETWORK_COMMUNICATION + NextApplication.myInfo.getLocalId());
        if (openSmartMesh == -1){
            Intent intent = new Intent(DiscoverUI.this, AlertActivity.class);
            intent.putExtra("type", 3);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }

    }


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && (Constants.OFFLINE_MEMBER_LIST.equals(intent.getAction()))){
                mDatas = (ArrayList<WifiPeopleVO>) intent.getSerializableExtra("onLineMember");
                mAdapter.resetSource(mDatas);
                if (isShowList){
                    checkListEmpty();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        radarViewGroup.updateDatas(mDatas);
                    }
                }, 500);
            }else if (intent != null && Constants.OPEN_SMARTMESH_NETWORE.equals(intent.getAction())) {
                int openSmartMesh = MySharedPrefs.readInt1(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_NO_NETWORK_COMMUNICATION + NextApplication.myInfo.getLocalId());
                if (openSmartMesh == 1){
                    isBound = bindService(new Intent(DiscoverUI.this, AppNetService.class), serviceConn,Activity.BIND_AUTO_CREATE);
                }
            }else if (intent != null && Constants.CLOSE_SMARTMESH_NETWORE.equals(intent.getAction())) {
                int openSmartMesh = MySharedPrefs.readInt1(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_NO_NETWORK_COMMUNICATION + NextApplication.myInfo.getLocalId());
                if (openSmartMesh == 0 && serviceConn != null){
                    try {
                        if (isBound){
                            unbindService(serviceConn);
                            isBound = false;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    // The Activity and netService2 connection
    private ServiceConnection serviceConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Binding service success
            AppNetService.NetServiceBinder binder = (AppNetService.NetServiceBinder) service;
            mDatas = (ArrayList<WifiPeopleVO>) binder.getService().getwifiPeopleList();
            radarViewGroup.updateDatas(mDatas);
        }
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (radarViewGroup != null){
            radarViewGroup.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (radarViewGroup != null){
            radarViewGroup.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        if (NextApplication.myInfo != null){
            int openSmartMesh = MySharedPrefs.readInt1(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_NO_NETWORK_COMMUNICATION + NextApplication.myInfo.getLocalId());
            if (openSmartMesh == 1 && serviceConn != null){
                try {
                    if (isBound){
                        unbindService(serviceConn);
                        isBound = false;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void onRadarItemClick(int position) {
        WifiPeopleVO wifiPeopleVO = mDatas.get(position);
        UserBaseVo info = new UserBaseVo();
        info.setLocalId(wifiPeopleVO.getLocalId());
        info.setMid(wifiPeopleVO.getMid());
        info.setUsername(wifiPeopleVO.getUserName());
        info.setNote(wifiPeopleVO.getNote());
        info.setThumb(wifiPeopleVO.getThumb());
        info.setGender(wifiPeopleVO.getGender());
        info.setSightml(wifiPeopleVO.getSightml());
        info.setAge(wifiPeopleVO.getAge());
        info.setFriendLog(wifiPeopleVO.getFriendLog());
        info.setOffLineFound(true);
        Utils.intentFriendUserInfo(DiscoverUI.this,info,false);
    }

    @OnClick({R.id.app_right,R.id.app_right_btn})
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.app_right:
                setDiscoverTheme();
                break;
            case R.id.app_right_btn://screening
                selectGenderMethod();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void setDiscoverTheme(){
        isShowList = !isShowList;
        if (isShowList){
            appTitleBg.setBackgroundResource(R.color.colorPrimary);
            titleLine.setBackgroundResource(R.color.tab_sep_line);
            mBack.setImageResource(R.drawable.icon_back);
            Utils.setStatusBar(DiscoverUI.this,0);
            appTitle.setTextColor(getResources().getColor(R.color.black));
            app_right.setImageResource(R.drawable.icon_found_black);
            selectImg.setVisibility(View.VISIBLE);
            foundBg.setBackgroundResource(0);
            radarViewGroup.setVisibility(View.INVISIBLE);
            if (radarViewGroup != null){
                radarViewGroup.onPause();
            }
            offlineList.setVisibility(View.VISIBLE);
            mAdapter.resetSource(mDatas);
            checkListEmpty();
        }else{
            appTitleBg.setBackgroundResource(R.color.colorFound);
            titleLine.setBackgroundResource(R.color.colorFound);
            mBack.setImageResource(R.drawable.icon_white_back);
            Utils.setStatusBar(DiscoverUI.this,2);
            appTitle.setTextColor(getResources().getColor(R.color.textColor));
            app_right.setImageResource(R.drawable.icon_found_white);
            selectImg.setVisibility(View.GONE);
            foundBg.setBackgroundResource(R.drawable.found_bg);
            radarViewGroup.setVisibility(View.VISIBLE);
            if (radarViewGroup != null){
                radarViewGroup.onResume();
            }
            offlineList.setVisibility(View.GONE);
            emptyRela.setVisibility(View.GONE);
        }
    }

    /**
     * Screening of gender box
     * */
    private void selectGenderMethod() {
        MyDialogFragment mdf = new MyDialogFragment(MyDialogFragment.DIALOG_LIST, R.array.sex_list_all);
        mdf.setItemClickCallback(new MyDialogFragment.ItemClickCallback() {
            @Override
            public void itemClickCallback(int which) {
                if (which == 0) { // male
                    updateDatas("1");
                } else if (which == 1) { // female
                    updateDatas("2");
                }else{ // all
                    if (mDatas != null){
                        mAdapter.resetSource(mDatas);
                    }
                }
            }
        });
        mdf.show(getSupportFragmentManager(), "mdf");
    }


    /**
     * Screening of gender method
     * */
    private void updateDatas(String gender){
        if (mDatas != null ){
            ArrayList<WifiPeopleVO> selectDatas = new ArrayList<>();
            if (TextUtils.equals("1",gender)){
                for (int i = 0 ; i < mDatas.size() ; i++){
                    if (TextUtils.equals("1",mDatas.get(i).getGender())){
                        selectDatas.add(mDatas.get(i));
                    }
                }
            }else{
                for (int i = 0 ; i < mDatas.size() ; i++){
                    if (!TextUtils.equals("1",mDatas.get(i).getGender())){
                        selectDatas.add(mDatas.get(i));
                    }
                }
            }
            mAdapter.resetSource(selectDatas);
        }
    }


    /**
     * To test whether the current list is empty
     */
    private void checkListEmpty() {
        if(mDatas == null || mDatas.size() == 0){
            emptyRela.setVisibility(View.VISIBLE);
            emptyTextView.setText(R.string.offline_no_people);
        }else{
            emptyRela.setVisibility(View.GONE);
        }
    }


    @OnItemClick(R.id.offlineList)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        WifiPeopleVO wifiPeopleVO = mDatas.get(position);
        UserBaseVo info = new UserBaseVo();
        info.setLocalId(wifiPeopleVO.getLocalId());
        info.setMid(wifiPeopleVO.getMid());
        info.setUsername(wifiPeopleVO.getUserName());
        info.setNote(wifiPeopleVO.getNote());
        info.setThumb(wifiPeopleVO.getThumb());
        info.setGender(wifiPeopleVO.getGender());
        info.setSightml(wifiPeopleVO.getSightml());
        info.setAge(wifiPeopleVO.getAge());
        info.setFriendLog(wifiPeopleVO.getFriendLog());
        info.setOffLineFound(true);
        Utils.intentFriendUserInfo(this,info,false);
    }
}
