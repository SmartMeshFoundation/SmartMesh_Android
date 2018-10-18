package com.lingtuan.firefly.vo;

import com.google.gson.JsonObject;

import org.json.JSONObject;

public class SystemNotice {

    private String title;
    private String notice;
    private String url;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public SystemNotice parse(JSONObject object){
        if (object == null){
            return null;
        }
        setTitle(object.optString("title"));
        setNotice(object.optString("notice"));
        setUrl(object.optString("url"));
        return this;
    }
}
