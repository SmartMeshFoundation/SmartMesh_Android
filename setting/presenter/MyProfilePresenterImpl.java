package com.lingtuan.firefly.setting.presenter;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.network.NetRequestImpl;
import com.lingtuan.firefly.setting.MyProfileUI;
import com.lingtuan.firefly.setting.contract.MyProfileContract;
import com.lingtuan.firefly.util.BitmapUtils;
import com.lingtuan.firefly.util.SDCardCtrl;

import org.json.JSONObject;

import java.io.File;

public class MyProfilePresenterImpl implements MyProfileContract.Presenter{

    private MyProfileContract.View mView;

    public MyProfilePresenterImpl(MyProfileContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void editUserInfo(final String imgPath, final String username, final String sightml, final String gender, final String birthcity) {
        NetRequestImpl.getInstance().editUserInfo(null, username, sightml, gender, birthcity, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                try {
                    NextApplication.myInfo.updateJsonUserInfo(username, sightml, gender, birthcity,NextApplication.mContext);
                    NextApplication.myInfo.setUsername(username);
                    NextApplication.myInfo.setSightml(sightml);
                    NextApplication.myInfo.setGender(gender);
                    NextApplication.myInfo.setAddress(birthcity);
                }catch (Exception e){
                    e.printStackTrace();
                }
                mView.editUserInfoSuccess();

            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.editUserInfoError(errorCode,errorMsg);
            }
        });
    }

    @Override
    public String getImagePath(Uri selectedImage) {
        try {
            String picturePath = null;
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = NextApplication.mContext.getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            if (c != null) {
                if (c.moveToNext()) {
                    c.moveToFirst();
                    int columnIndex = c.getColumnIndex(filePathColumns[0]);
                    picturePath = c.getString(columnIndex);
                }
                c.close();
            } else {
                picturePath = selectedImage.toString().replace("file://", "");
            }
            return picturePath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
