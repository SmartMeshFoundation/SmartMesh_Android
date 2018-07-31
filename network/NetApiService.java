package com.lingtuan.firefly.network;


import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Create a business request interface
 * @FormUrlEncoded will automatically adjust the type of the request parameter to application/x-www-form-urlencoded (cannot be used for GET requests)
 * @Field passes a small number of parameters
 * @FieldMap passes multiple parameters
 * @Body Pass more parameters
 *
 * 创建业务请求接口
 * @FormUrlEncoded 将会自动将请求参数的类型调整为application/x-www-form-urlencoded (不能用于GET请求)
 * @Field 传递少量参数
 * @FieldMap 传递多个参数
 * @Body 传递更多参数
 */
public interface NetApiService {

    /**
     * get data interface
     * @param jsonRequest Packaged data  封装的数据
     */
    @FormUrlEncoded
    @POST(UrlConstantsApi.INDEX_URL)
    Observable<ResponseBody> getData(@Field("body") String jsonRequest);


    @Multipart
    @POST(UrlConstantsApi.INDEX_URL)
    Observable<ResponseBody> upload(@Part MultipartBody.Part jsonPart,@Part MultipartBody.Part file);


    /**
     * download file
     * 下载文件
     */
    @Streaming
    @GET
    Observable<ResponseBody> download(@Url String url);

    /**
     * upload files
     * No @Post ("address") method, using dynamic url for easy packaging
     * 上传文件
     * 没有使用@Post（“地址”）方法，使用了动态的url，方便封装
     */
    @Multipart
    @POST
    Observable<ResponseBody> upload(@Url String url, @Part MultipartBody.Part file);

}
