package com.lingtuan.firefly.setting.contract;

import android.net.Uri;

import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;

public interface MyProfileContract {

    interface Presenter extends BasePresenter {

        /**
         * edit user info
         * @param imgPath        user image
         * @param username       user name
         * @param sightml         user sightml
         * @param gender          user gender
         * @param birthcity      user birth city
         * */
        void editUserInfo(String imgPath,String username,String sightml,String gender,String birthcity);

        /**
         * get user image path
         * @param selectedImage  user image uri
         * */
        String getImagePath(Uri selectedImage);

    }

    interface View extends BaseView<Presenter> {

        /**
         * edit user info success
         * */
        void editUserInfoSuccess();

        /**
         * edit user info error
         * @param errorCode  error code
         * @param errorMsg   error message
         * */
        void editUserInfoError(int errorCode, String errorMsg);

    }
}
