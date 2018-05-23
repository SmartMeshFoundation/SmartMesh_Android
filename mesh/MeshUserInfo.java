package com.lingtuan.firefly.mesh;

import android.net.nsd.NsdServiceInfo;
import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by H on 2018/4/2.
 * Description:
 */

public class MeshUserInfo implements Serializable {
    
    private NsdServiceInfo service;
    private String avatarPath;
    private String name;
    private String localId;
    private String ip;
    private int port;
    private boolean isFriend;
    private String nikeName;
    private String sendData;
    private int type = 1;//1 text  2 image
    private boolean isResolved;
    private int resolvedFailedNum;
    
    private String walletAddress;
    
    public int getResolvedFailedNum() {
        return resolvedFailedNum;
    }
    
    public void setResolvedFailedNum(int resolvedFailedNum) {
        this.resolvedFailedNum = resolvedFailedNum;
    }
    
    public boolean isResolved() {
        return isResolved;
    }
    
    public void setResolved(boolean resolved) {
        isResolved = resolved;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public NsdServiceInfo getService() {
        return service;
    }
    
    public void setService(NsdServiceInfo service) {
        this.service = service;
    }
    
    public String getAvatarPath() {
        return avatarPath;
    }
    
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }
    
    public String getName() {
        return name;
    }
    
    public String getNikeName() {
        return TextUtils.isEmpty(nikeName) ? name : nikeName;
    }
    
    public void setNikeName(String nikeName) {
        this.nikeName = nikeName;
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
    
    public String getWalletAddress() {
        return walletAddress;
    }
    
    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
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
    
    public boolean isFriend() {
        return isFriend;
    }
    
    public void setFriend(boolean friend) {
        isFriend = friend;
    }
    
    public String getSendData() {
        return sendData;
    }
    
    public void setSendData(String sendData) {
        this.sendData = sendData;
    }
}
