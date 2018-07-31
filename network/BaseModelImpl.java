package com.lingtuan.firefly.network;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Network request tool class
 * Create a Retrofit example and complete the appropriate configuration
 * baseUrl network request URL is relatively fixed address
 * The addConverterFactory method indicates what converter is needed to parse the return value
 * 网络请求工具类
 * 创建一个Retrofit的示例，并完成相应的配置
 * baseUrl网络请求URL相对固定的地址
 * addConverterFactory方法表示需要用什么转换器来解析返回值
 */
public class BaseModelImpl {

    private static NetApiService blueService;

    public static NetApiService getInstance(){
        if (blueService == null) {
            synchronized (BaseModelImpl.class) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(UrlConstantsApi.BASE_URL)
                        //Increase the return value to Gson support (returned by entity class)  增加返回值为Gson的支持(以实体类返回)
                        .addConverterFactory(GsonConverterFactory.create())
                        //Increase support for return value Observable<T>  增加返回值为Observable<T>的支持
                        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                        .build();
                blueService = retrofit.create(NetApiService.class);
            }
        }
        return blueService;
    }
}
