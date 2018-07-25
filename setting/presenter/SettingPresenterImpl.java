package com.lingtuan.firefly.setting.presenter;

import android.os.Bundle;
import android.text.TextUtils;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.setting.contract.SettingContract;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;

import org.json.JSONObject;

import java.util.Locale;

public class SettingPresenterImpl implements SettingContract.Presenter{

    private SettingContract.View mView;

    public SettingPresenterImpl(SettingContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public String getUserAgreement() {
        String result = "";
        String language = MySharedPrefs.readString(NextApplication.mContext,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_LANGUAFE);
        if (TextUtils.isEmpty(language)){
            Locale locale = new Locale(Locale.getDefault().getLanguage());
            if (TextUtils.equals(locale.getLanguage(),"zh")){
                result = Constants.USE_AGREE_ZH;
            }else{
                result = Constants.USE_AGREE_EN;
            }
        }else{
            if (TextUtils.equals(language,"zh")){
                result = Constants.USE_AGREE_ZH;
            }else{
                result = Constants.USE_AGREE_EN;
            }
        }
        return result;
    }

    @Override
    public void logOutMethod() {
        NetRequestImpl.getInstance().logout(new RequestListener() {
            @Override
            public void start() {
                mView.start();
            }

            @Override
            public void success(JSONObject response) {
                mView.success();
            }

            @Override
            public void error(int errorCode, String errorMsg) {
               mView.error(errorCode,errorMsg,true);
            }
        });
    }

    @Override
    public void checkVersion() {
        NetRequestImpl.getInstance().versionUpdate(new RequestListener() {
            @Override
            public void start() {
                mView.start();
            }

            @Override
            public void success(JSONObject response) {
                LoadingDialog.close();
                String version = response.optString("version");
                String describe = response.optString("describe");
                String url = response.optString("url");
                int coerce = response.optInt("coerce",0);
                MySharedPrefs.write(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_UPDATE_VERSION,version);
                Bundle data = new Bundle();
                if (coerce == 0){// Don't force
                    data.putInt("type", 0);}
                else{
                    data.putInt("type", 1);
                }
                data.putString("version", version);
                data.putString("describe", describe);
                data.putString("url", url);
                mView.sendBroadcast(data);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.error(errorCode,errorMsg,false);
            }
        });
    }


}
