package com.lingtuan.firefly.mesh;

import org.json.JSONObject;

/**
 * Created by H 
 * Description:
 */

public class DevicesSourceVo {
    private String name;
    private String localId;
    private String ip;
    private int port;
    private String platform = "android";
    
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("name", name);
            obj.put("localId", localId);
            obj.put("ip", ip);
            obj.put("port", port);
            obj.put("platform", platform);
        } catch (Exception e) {
            return null;
        }
        
        return obj;
    }
    
    public DevicesSourceVo parse(JSONObject obj) {
        try {
            setName(obj.optString("name"));
            setLocalId(obj.optString("localId"));
            setIp(obj.optString("ip"));
            setPort(obj.optInt("port"));
            setPlatform(obj.optString("platform"));
        } catch (Exception e) {
        }
        return this;
    }
    
    public String getPlatform() {
        return platform;
    }
    
    public void setPlatform(String platform) {
        this.platform = platform;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getLocalId() {
        return localId;
    }
    
    public void setLocalId(String localId) {
        this.localId = localId;
    }
    
    public String getIp() {
        return ip;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
}
