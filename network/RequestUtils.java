package com.lingtuan.firefly.network;

import android.accounts.NetworkErrorException;
import android.app.NotificationManager;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.login.LoginUI;
import com.lingtuan.firefly.mesh.MeshService;
import com.lingtuan.firefly.network.upload.FileUploadObserver;
import com.lingtuan.firefly.network.upload.UploadFileRequestBody;
import com.lingtuan.firefly.offline.AppNetService;
import com.lingtuan.firefly.service.XmppService;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.xmpp.XmppUtils;

import org.json.JSONObject;

import java.io.File;
import java.nio.channels.NoConnectionPendingException;
import java.util.concurrent.TimeoutException;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RequestUtils {

    private static RequestUtils instance;

    public static RequestUtils getInstance(){
        if(instance == null ){
            synchronized (RequestUtils.class) {
                instance = new RequestUtils();
            }
        }
        return instance;
    }

    /**
     * load data
     * @param jsonRequest json body
     * @param listener call back
     * */
    public  void getData(final JSONObject jsonRequest, final RequestListener listener){
        boolean needReturn = requestStart(jsonRequest,listener);
        if (needReturn){
            return;
        }

        BaseModelImpl.getInstance().getData(jsonRequest.toString())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        requestError(e,listener);
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        requestSuccess(responseBody,listener);
                    }
                });
    }


    /**
     * upload file
     * @param jsonRequest json body
     * @param addPartName part name
     * @param messageId message id
     * @param path file path
     * @param listener call back
     * */
    public void upload(final JSONObject jsonRequest, String addPartName, final String path, final String messageId, final RequestListener listener){
        boolean needReturn = requestStart(jsonRequest,listener);
        if (needReturn){
            return;
        }

        FileUploadObserver<ResponseBody> fileUploadObserver = new FileUploadObserver<ResponseBody>() {
            @Override
            public void onUpLoadSuccess(ResponseBody responseBody) {
                requestSuccess(responseBody,listener);
            }

            @Override
            public void onUpLoadFail(Throwable e) {
                requestError(e,listener);
            }

            @Override
            public void onProgress(int progress) {
                if (!TextUtils.isEmpty(messageId)){
                    Utils.intentImagePrecent(NextApplication.mContext, messageId,progress);
                }
            }
        };

        File file = new File(path);
        UploadFileRequestBody uploadFileRequestBody = new UploadFileRequestBody(file, fileUploadObserver);
        MultipartBody.Part part = MultipartBody.Part.createFormData(addPartName, file.getName(), uploadFileRequestBody);
        MultipartBody.Part jsonPart = MultipartBody.Part.createFormData("body", jsonRequest.toString());
        BaseModelImpl.getInstance().upload(jsonPart,part)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fileUploadObserver);
    }


    /**
     * request start
     * @param jsonRequest json body
     * @param listener call back
     * */
    private boolean requestStart(JSONObject jsonRequest,RequestListener listener){
        if(listener != null){
            listener.start();
        }

        if (jsonRequest == null){
            if(listener != null){
                listener.error(404,"");
            }
            return true;
        }

        if (!Utils.isConnectNet(NextApplication.mContext)){
            if(listener != null){
                listener.error(404,NextApplication.mContext.getString(R.string.net_unavailable));
            }
            return true;
        }
        return false;
    }

    /**
     * request error
     * @param e Throwable exception
     * @param listener call back
     * */
    private void requestError(Throwable e,RequestListener listener){
        if (listener != null){
            if(e instanceof TimeoutException){
                listener.error(404, NextApplication.mContext.getString(R.string.net_request_timeout));
            }else if(e instanceof NetworkErrorException){
                listener.error(404,NextApplication.mContext.getString(R.string.net_unavailable));
            }else{
                if (e.getMessage().contains("Failed to connect") || e.getMessage().contains("failed to connect")){
                    listener.error(404,NextApplication.mContext.getString(R.string.net_unavailable));
                }else if (e.getMessage().contains("timeout")){
                    listener.error(404, NextApplication.mContext.getString(R.string.net_request_timeout));
                }else{
                    listener.error(404,"");
                }
            }
        }
    }

    /**
     * request offline
     * */
    private void requestOffLine(){
        try {
            MySharedPrefs.clearUserInfo(NextApplication.mContext);
            FinalUserDataBase.getInstance().close();
            WalletStorage.getInstance(NextApplication.mContext).destroy();
            MyToast.showToast(NextApplication.mContext, NextApplication.mContext.getString(R.string.login_state_failure));
            //Empty the notification
            NotificationManager notificationManager = (NotificationManager) NextApplication.mContext.getSystemService(NextApplication.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();

            //Exit the XMPP service
            XmppUtils.getInstance().destroy();
            Intent xmppservice = new Intent(NextApplication.mContext, XmppService.class);
            NextApplication.mContext.stopService(xmppservice);
            NextApplication.mContext.stopService(new Intent(NextApplication.mContext, MeshService.class));
            //Exit without social network service
            int version =android.os.Build.VERSION.SDK_INT;
            if(version >= 16){
                Intent offlineservice = new Intent(NextApplication.mContext, AppNetService.class);
                NextApplication.mContext.stopService(offlineservice);
            }
            BaseActivity.exit();
            NextApplication.myInfo=null;
            Intent intent = new Intent(NextApplication.mContext,LoginUI.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            NextApplication.mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * request success
     * @param responseBody response body
     * @param listener call back
     * */
    private void requestSuccess(ResponseBody responseBody,RequestListener listener){
        try {
            String jsonString = responseBody.string();
            JSONObject response = new JSONObject(jsonString);
            int code = response.optInt("errcode");
            if(listener != null){
                if(code == 0){
                    listener.success(response);
                }else{
                    if(code == 1010101){
                        requestOffLine();
                    }else{
                        listener.error( code, response.optString("msg"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroy(){
        instance = null;
    }

}
