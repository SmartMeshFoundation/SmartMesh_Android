package com.lingtuan.firefly.vo;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class ImageVo extends ImageResizer implements Serializable {

    private static final long serialVersionUID = 1L;
    private String thumb;
    private String pic;
    private String picid;
    private int status;//State, 0 - approval, 2-1 - has passed, have been rejected
    private int type = 0;//Skills to upload pictures when the default is time represents the picture 1 + number of pictures

    private int width;
    private int height;

    private String name = null; // Referee head for the application in the list of names

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumb() {
        return thumb;
    }

    public String getPicid() {
        return picid;
    }

    public void setPicid(String picid) {
        this.picid = picid;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ImageVo parse(JSONObject obj) {
        if (obj == null) {
            return null;
        }
        try {

            if (TextUtils.isEmpty(obj.optString("thumb"))) {
                JSONObject sizeObj = obj.optJSONObject("size");
                if (sizeObj != null) {
                    setReqWidth(sizeObj.optInt("width") + "");
                    setReqHeight(sizeObj.optInt("height") + "");
                    setReqCropType("2"); // Didn't understand why this place 1 when the picture is wrong, when 2 pictures are right
                }
                setThumb(buildThumb(obj.optString("pic")));
            } else {
                setThumb(obj.optString("thumb"));
            }

//			setThumb(TextUtils.isEmpty(obj.optString("thumb")) ? buildThumb(obj.optString("pic")) : obj.optString("thumb"));
            setPic(obj.optString("pic"));
            setPicid(obj.optString("picid"));
            JSONObject sizeObj = obj.optJSONObject("size");
            if (sizeObj != null) {
                setWidth(sizeObj.optInt("width"));
                setHeight(sizeObj.optInt("height"));
            }
            setStatus(obj.optInt("status"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * @param obj
     * @return Recommended in the registration process data, note: return only PIC don't return to the thumb
     */
    public ImageVo parseRegister(JSONObject obj) {
        if (obj == null) {
            return null;
        }
        try {
            setThumb(obj.optString("pic"));
            setPic(obj.optString("pic"));
            setPicid(obj.optString("picid"));
            setName(obj.optString("username"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public JSONObject getJsonObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("thumb", getThumb());
            obj.put("pic", getThumb());
            obj.put("picid", getThumb());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
