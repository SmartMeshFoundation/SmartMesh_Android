
package com.lingtuan.firefly.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.adapter.MainFoundAdapter;
import com.lingtuan.firefly.base.BaseFragment;
import com.lingtuan.firefly.discover.RadarViewGroup;
import com.lingtuan.firefly.login.LoginUtil;
import com.lingtuan.firefly.offline.AppNetService;
import com.lingtuan.firefly.offline.vo.WifiPeopleVO;
import com.lingtuan.firefly.ui.AlertActivity;
import com.lingtuan.firefly.ui.MainFragmentUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MyDialogFragment;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.UserBaseVo;

import java.util.ArrayList;

/**
 *Find the page
 */
public class MainFoundFragmentUI extends BaseFragment implements RadarViewGroup.IRadarClickListener, View.OnClickListener, AdapterView.OnItemClickListener {

    private View view;
    private boolean isDataFirstLoaded;

    //The title
    private TextView mTitle;
    //Return key hidden
    private ImageView mBack;

    private ImageView app_right;
    private ImageView selectImg;//screening

    private LinearLayout foundBg;

    private ListView offlineList;
    private boolean isShowList;//Whether the display list

    private RadarViewGroup radarViewGroup;


    private ArrayList<WifiPeopleVO> mDatas;

    private MainFoundAdapter mAdapter;

    private TextView emptyTextView;
    private RelativeLayout emptyRela;

    private RelativeLayout appTitleBg;
    private View titleLine;
    private TextView appTitle;


    private TextView uploadRegisterInfo;//Synchronous registration information

    public MainFoundFragmentUI() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        isDataFirstLoaded = true;
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        if (!isDataFirstLoaded && view != null) {
            ((ViewGroup) view.getParent()).removeView(view);
            return view;
        }
        view = inflater.inflate(R.layout.main_found_layout, container, false);
        initView();
        initData();
        setListener();
        return view;
    }

    private void setListener() {
        app_right.setOnClickListener(this);
        selectImg.setOnClickListener(this);
        uploadRegisterInfo.setOnClickListener(this);
        offlineList.setOnItemClickListener(this);
    }

    private void initView() {
        appTitleBg = (RelativeLayout) view.findViewById(R.id.app_title_rela);
        titleLine = view.findViewById(R.id.titleLine);
        appTitle = (TextView) view.findViewById(R.id.app_title);
        mBack = (ImageView) view.findViewById(R.id.app_back);
        app_right = (ImageView) view.findViewById(R.id.app_right);
        selectImg = (ImageView) view.findViewById(R.id.app_right_btn);

        uploadRegisterInfo = (TextView) view.findViewById(R.id.uploadRegisterInfo);

        offlineList = (ListView) view.findViewById(R.id.offlineList);
        foundBg = (LinearLayout) view.findViewById(R.id.foundBg);
        mTitle = (TextView) view.findViewById(R.id.app_title);
        radarViewGroup = (RadarViewGroup) view.findViewById(R.id.radar);

        emptyRela = (RelativeLayout) view.findViewById(R.id.empty_like_rela);
        emptyTextView = (TextView) view.findViewById(R.id.empty_text);
    }

    private void initData() {

        shoSmartMeshDialog();

        // -1 default  0 close  1 open
        int openSmartMesh = MySharedPrefs.readInt1(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_NO_NETWORK_COMMUNICATION + NextApplication.myInfo.getLocalId());
        if (openSmartMesh == 1){
            getActivity().bindService(new Intent(getActivity(), AppNetService.class), serviceConn,Activity.BIND_AUTO_CREATE);
        }

        IntentFilter filter = new IntentFilter(Constants.OFFLINE_MEMBER_LIST);//message Session record distribution
        filter.addAction(Constants.CHANGE_LANGUAGE);//Update language refresh the page
        filter.addAction(Constants.ACTION_NETWORK_RECEIVER);//Network monitoring
        filter.addAction(Constants.OPEN_SMARTMESH_NETWORE);//bind service
        filter.addAction(Constants.CLOSE_SMARTMESH_NETWORE);//unbind service
        getActivity().registerReceiver(mBroadcastReceiver, filter);

        mBack.setVisibility(View.GONE);
        app_right.setVisibility(View.VISIBLE);
        app_right.setImageResource(R.drawable.icon_found_white);
        selectImg.setImageResource(R.drawable.icon_select);
        appTitleBg.setBackgroundResource(R.color.colorFound);
        titleLine.setBackgroundResource(R.color.colorFound);
        appTitle.setTextColor(getResources().getColor(R.color.textColor));
        mTitle.setText(getString(R.string.main_discover));
        int width = getActivity().getResources().getDisplayMetrics().widthPixels*5/6;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, width);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        radarViewGroup.setLayoutParams(layoutParams);
        radarViewGroup.setiRadarClickListener(this);

        mAdapter = new MainFoundAdapter(getActivity(), null);
        offlineList.setAdapter(mAdapter);

        Utils.updateViewMethod(uploadRegisterInfo,getActivity());

    }

    private void shoSmartMeshDialog() {

        if (NextApplication.myInfo == null){
            return;
        }
        int version =android.os.Build.VERSION.SDK_INT;
        if(version < 16){
            return;
        }
        // -1 default  0 close  1 open
        int openSmartMesh = MySharedPrefs.readInt1(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_NO_NETWORK_COMMUNICATION + NextApplication.myInfo.getLocalId());
        if (openSmartMesh == -1){
            Intent intent = new Intent(getActivity(), AlertActivity.class);
            intent.putExtra("type", 3);
            startActivity(intent);
            getActivity().overridePendingTransition(0, 0);
        }

    }


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && (Constants.CHANGE_LANGUAGE.equals(intent.getAction()))) {
                mTitle.setText(R.string.main_contact);
                if (isShowList){
                    checkListEmpty();
                }
            }else if (intent != null && (Constants.OFFLINE_MEMBER_LIST.equals(intent.getAction()))){
                mDatas = (ArrayList<WifiPeopleVO>) intent.getSerializableExtra("onLineMember");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        radarViewGroup.updateDatas(mDatas);
                    }
                }, 500);
            }else if (intent != null && Constants.ACTION_NETWORK_RECEIVER.equals(intent.getAction())) {
                Utils.updateViewMethod(uploadRegisterInfo,getActivity());
            }else if (intent != null && Constants.OPEN_SMARTMESH_NETWORE.equals(intent.getAction())) {
                int openSmartMesh = MySharedPrefs.readInt1(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_NO_NETWORK_COMMUNICATION + NextApplication.myInfo.getLocalId());
                if (openSmartMesh == 1){
                    getActivity().bindService(new Intent(getActivity(), AppNetService.class), serviceConn,Activity.BIND_AUTO_CREATE);
                }
            }else if (intent != null && Constants.CLOSE_SMARTMESH_NETWORE.equals(intent.getAction())) {
                int openSmartMesh = MySharedPrefs.readInt1(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_NO_NETWORK_COMMUNICATION + NextApplication.myInfo.getLocalId());
                if (openSmartMesh == 0 && serviceConn != null){
                    getActivity().unbindService(serviceConn);
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

//    @Override
//    public void onPause() {
//        super.onResume();
//        if (radarViewGroup != null){
//            radarViewGroup.onPause();
//        }
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mBroadcastReceiver);
        if (NextApplication.myInfo != null){
            int openSmartMesh = MySharedPrefs.readInt1(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_NO_NETWORK_COMMUNICATION + NextApplication.myInfo.getLocalId());
            if (openSmartMesh == 1 && serviceConn != null){
                getActivity().unbindService(serviceConn);
            }
        }
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            Utils.updateViewMethod(uploadRegisterInfo,getActivity());
            if (radarViewGroup != null){
                radarViewGroup.onResume();
            }
        }else{
            if (radarViewGroup != null){
                radarViewGroup.onPause();
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!isDataFirstLoaded) {
            return;
        }
        isDataFirstLoaded = false;
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
        Utils.intentFriendUserInfo(getActivity(),info,false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.app_right:
                isShowList = !isShowList;
                if (isShowList){
                    appTitleBg.setBackgroundResource(R.color.colorPrimary);
                    titleLine.setBackgroundResource(R.color.tab_sep_line);
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
                    Utils.updateViewMethod(uploadRegisterInfo,getActivity());
                }else{
                    appTitleBg.setBackgroundResource(R.color.colorFound);
                    titleLine.setBackgroundResource(R.color.colorFound);
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
                break;
            case R.id.uploadRegisterInfo:
                LoginUtil.getInstance().initContext(getActivity());
                LoginUtil.getInstance().showRegistDialog(uploadRegisterInfo);
                break;
            case R.id.app_right_btn://screening
                selectGenderMethod();
                break;
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
        mdf.show(getFragmentManager(), "mdf");
    }


    /**
     * Screening of gender method
     * */
    private void updateDatas(String gender){
        if (mDatas != null ){
            ArrayList<WifiPeopleVO> selectDatas = new ArrayList<>();
            for (int i = 0 ; i < mDatas.size() ; i++){
                if (TextUtils.equals(gender,mDatas.get(i).getGender())){
                    selectDatas.add(mDatas.get(i));
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


    @Override
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
        Utils.intentFriendUserInfo(getActivity(),info,false);
    }
}
