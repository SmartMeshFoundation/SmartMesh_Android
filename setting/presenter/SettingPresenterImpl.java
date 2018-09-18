package com.lingtuan.firefly.setting.presenter;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.language.LanguageType;
import com.lingtuan.firefly.language.MultiLanguageUtil;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.network.NetRequestImpl;
import com.lingtuan.firefly.setting.contract.SettingContract;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MySharedPrefs;

import org.json.JSONObject;

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
        int language = MultiLanguageUtil.getInstance().getLanguageType();
        if (LanguageType.LANGUAGE_CHINESE_SIMPLIFIED == language){
            result = Constants.USE_AGREE_ZH;
        }else{
            result = Constants.USE_AGREE_EN;
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
                String url = response.optString("url");
                MySharedPrefs.write(NextApplication.mContext,MySharedPrefs.FILE_USER,MySharedPrefs.KEY_UPDATE_VERSION,version);
                mView.updateVersion(version,url);
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.error(errorCode,errorMsg,false);
            }
        });
    }


}
