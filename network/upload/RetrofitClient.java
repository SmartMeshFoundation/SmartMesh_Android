package com.lingtuan.firefly.network.upload;

import com.lingtuan.firefly.network.NetApiService;
import com.lingtuan.firefly.network.UrlConstantsApi;

import java.io.File;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static RetrofitClient mInstance;
    private static Retrofit retrofit;


    private RetrofitClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(UrlConstantsApi.BASE_URL)
                .client(OkHttpManager.getInstance())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

    }

    public static RetrofitClient getInstance() {
        if (mInstance == null) {
            synchronized (RetrofitClient.class) {
                if (mInstance == null) {
                    mInstance = new RetrofitClient();
                }
            }
        }
        return mInstance;
    }

    private <T> T create(Class<T> clz) {
        return retrofit.create(clz);
    }

    NetApiService api() {
        return RetrofitClient.getInstance().create(NetApiService.class);
    }

    /**
     * 单上传文件的封装
     *
     * @param url                完整的接口地址
     * @param file               需要上传的文件
     * @param fileUploadObserver 上传回调
     */
    public void upLoadFile(String url, File file, FileUploadObserver<ResponseBody> fileUploadObserver) {
        UploadFileRequestBody uploadFileRequestBody = new UploadFileRequestBody(file, fileUploadObserver);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), uploadFileRequestBody);
        create(NetApiService.class)
                .upload(url, part)
                .subscribeOn(rx.schedulers.Schedulers.io())
                .unsubscribeOn(rx.schedulers.Schedulers.io())
                .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(fileUploadObserver);
    }


}
