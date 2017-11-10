package com.lingtuan.firefly.login;

import android.content.Context;
import android.text.TextUtils;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.vo.UserInfoVo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created on 2017/10/16.
 */

public class JsonUtil {

    /**
     * Read local information
     * */
    public static ArrayList<UserInfoVo> readLocalInfo(Context mContext){
        String localInfo = MySharedPrefs.readString(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOCAL_USERINFO);
        ArrayList<String> localidList = new ArrayList<>();
        ArrayList<UserInfoVo> localInfoList = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(localInfo);
            if (array.length() > 0){
                for (int i = 0 ; i < array.length() ; i++){
                    localidList.add(array.optString(i));
                }
            }
            for (int j = 0 ; j < localidList.size(); j++){
                String jsonString  = MySharedPrefs.readString(mContext, MySharedPrefs.FILE_USER, localidList.get(j));
                JSONObject jsonObject = new JSONObject(jsonString);
                UserInfoVo infoVo = new UserInfoVo().parse(jsonObject.optJSONObject(localidList.get(j)));
                localInfoList.add(infoVo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return localInfoList;
    }


    /**
     * Save the local login information
     * @param userInfoVo The user information
     * */
    public static void writeLocalInfo(Context mContext,UserInfoVo userInfoVo){
        String localInfo = MySharedPrefs.readString(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOCAL_USERINFO);
        try {
            JSONArray array;
            if (!TextUtils.isEmpty(localInfo)){
                array = new JSONArray(localInfo);
            }else{
                array = new JSONArray();
            }
            if (array.length() > 0){
                for (int i = 0 ; i < array.length() ; i++){
                    if (TextUtils.equals(userInfoVo.getLocalId(),array.optString(i))){
                        return;
                    }
                }
            }
            array.put(array.length(),userInfoVo.getLocalId());
            MySharedPrefs.write(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOCAL_USERINFO,array.toString());

            JSONObject jsonObject = new JSONObject();
            JSONObject data = new JSONObject();
            data.put(JsonConstans.Token,userInfoVo.getToken());
            data.put(JsonConstans.Mid,userInfoVo.getMid());
            data.put(JsonConstans.Password,userInfoVo.getPassword());

            data.put(JsonConstans.Username,userInfoVo.getUserName());
            data.put(JsonConstans.Localid,userInfoVo.getLocalId());
            jsonObject.put(userInfoVo.getLocalId(),data);
            MySharedPrefs.write(mContext, MySharedPrefs.FILE_USER, userInfoVo.getLocalId(),jsonObject.toString());

            MySharedPrefs.write(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOGIN_USERINFO, data.toString());
            NextApplication.myInfo = new UserInfoVo().readMyUserInfo(mContext);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save the local login information
     * @param userInfoVo The user information
     * */
    public static void updateLocalInfo(Context mContext,UserInfoVo userInfoVo){
        String localInfo = MySharedPrefs.readString(mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOCAL_USERINFO);
        try {
            JSONArray array;
            if (!TextUtils.isEmpty(localInfo)){
                array = new JSONArray(localInfo);
            }else{
                array = new JSONArray();
            }
            if (array.length() > 0){
                for (int i = 0 ; i < array.length() ; i++){
                    if (TextUtils.equals(userInfoVo.getLocalId(),array.optString(i))){
                        JSONObject jsonObject = new JSONObject();
                        JSONObject data = new JSONObject();
                        data.put(JsonConstans.Token,userInfoVo.getToken());
                        data.put(JsonConstans.Mid,userInfoVo.getMid());
                        data.put(JsonConstans.Password,userInfoVo.getPassword());

                        data.put(JsonConstans.Localid,userInfoVo.getLocalId());
                        data.put(JsonConstans.Username,userInfoVo.getUserName());
                        data.put(JsonConstans.Age,userInfoVo.getAge());
                        data.put(JsonConstans.Gender,userInfoVo.getGender());
                        data.put(JsonConstans.Sightml,userInfoVo.getSightml());
                        data.put(JsonConstans.Pic,userInfoVo.getPic());
                        data.put(JsonConstans.BirthCity,userInfoVo.getAddress());
                        jsonObject.put(userInfoVo.getLocalId(),data);
                        MySharedPrefs.write(mContext, MySharedPrefs.FILE_USER, userInfoVo.getLocalId(),jsonObject.toString());
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
