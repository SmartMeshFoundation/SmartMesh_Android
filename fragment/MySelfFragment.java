package com.lingtuan.firefly.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseFragment;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.login.JsonUtil;
import com.lingtuan.firefly.login.LoginUtil;
import com.lingtuan.firefly.setting.MyProfileUI;
import com.lingtuan.firefly.setting.SecurityUI;
import com.lingtuan.firefly.setting.SettingUI;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.vo.UserInfoVo;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * My page
 * Created on 2017/8/23.
 */

public class MySelfFragment extends BaseFragment implements View.OnClickListener {

    /**
     * root view
     */
    private View view = null;
    private boolean isDataFirstLoaded;
    private ImageView userImg;//Account management
    private TextView userName,signTv;//name  signature
    private RelativeLayout settingBody;//Setting
    private RelativeLayout securityBody;//security

    private TextView uploadRegisterInfo;

    public MySelfFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        isDataFirstLoaded = true;
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!isDataFirstLoaded && view != null) {
            ((ViewGroup) view.getParent()).removeView(view);
            return view;
        }
        view = inflater.inflate(R.layout.myself_fragment,container,false);
        findViewById();
        setListener();
        initData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserInfo();
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            Utils.updateViewMethod(uploadRegisterInfo,getActivity());
        }
    }

    /**
     * Get the user information
     * */
    private void getUserInfo(){
        if (NextApplication.myInfo == null || getActivity() == null){
            return;
        }
        NetRequestImpl.getInstance().requestUserInfo(NextApplication.myInfo.getLocalId(),new RequestListener() {
            @Override
            public void start() {
            }

            @Override
            public void success(JSONObject response) {

                JSONObject object = response.optJSONObject("data");
                try {
                    object.put("token",NextApplication.myInfo.getToken());
                    object.put("mid",NextApplication.myInfo.getMid());
                    object.put("password",NextApplication.myInfo.getPassword());
                    object.put("localid",NextApplication.myInfo.getLocalId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                MySharedPrefs.write(getActivity(), MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOGIN_USERINFO, object.toString());
                NextApplication.myInfo = new UserInfoVo().readMyUserInfo(getActivity());
                UserInfoVo userInfoVo = new UserInfoVo().parse(object);
                JsonUtil.updateLocalInfo(getActivity(),userInfoVo);

                loadInfoData();
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                loadInfoData();
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!isDataFirstLoaded) {
            return;
        }
        isDataFirstLoaded = false;
    }

    private void findViewById() {
        userImg = (ImageView) view.findViewById(R.id.userImg);
        userName = (TextView) view.findViewById(R.id.userName);
        signTv = (TextView) view.findViewById(R.id.signTv);
        settingBody = (RelativeLayout) view.findViewById(R.id.settingBody);
        securityBody = (RelativeLayout) view.findViewById(R.id.securityBody);

        uploadRegisterInfo = (TextView) view.findViewById(R.id.uploadRegisterInfo);
    }

    private void setListener() {
        userImg.setOnClickListener(this);
        userName.setOnClickListener(this);
        settingBody.setOnClickListener(this);
        securityBody.setOnClickListener(this);
        uploadRegisterInfo.setOnClickListener(this);
    }


    private void initData() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.CHANGE_LANGUAGE);//Update language refresh the page
        filter.addAction(Constants.ACTION_NETWORK_RECEIVER);//Network changes to monitor
        getActivity().registerReceiver(mBroadcastReceiver, filter);
        loadInfoData();

    }

    private void loadInfoData() {
        if (NextApplication.myInfo != null){
            userName.setText(NextApplication.myInfo.getUsername());
            NextApplication.displayCircleImage(userImg,NextApplication.myInfo.getThumb());
            signTv.setText(NextApplication.myInfo.getSightml());
        }
    }

    // Account management, help center, submit feedback, about us
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.userImg:
            case R.id.userName:
                startActivity(new Intent(getActivity(), MyProfileUI.class));
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.settingBody:
                startActivity(new Intent(getActivity(), SettingUI.class));
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.securityBody:
                startActivity(new Intent(getActivity(), SecurityUI.class));
                Utils.openNewActivityAnim(getActivity(),false);
                break;
            case R.id.uploadRegisterInfo:
                LoginUtil.getInstance().initContext(getActivity());
                LoginUtil.getInstance().showRegistDialog(uploadRegisterInfo);
                break;
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           if (intent != null && (Constants.CHANGE_LANGUAGE.equals(intent.getAction()))) {
                Utils.updateViewLanguage(view.findViewById(R.id.myself_bg));
           }else if (intent != null && Constants.ACTION_NETWORK_RECEIVER.equals(intent.getAction())) {
               Utils.updateViewMethod(uploadRegisterInfo,getActivity());
           }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }
}
