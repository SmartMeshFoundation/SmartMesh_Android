package com.lingtuan.firefly.chat.vo;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;


import com.lingtuan.firefly.util.FileSizeUtils;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created on 2016/6/6.
 */
public class FileChildVo implements Parcelable {
    private String fileType;//The file type
    private boolean selectState;//selected
    private String name;//The file name
    private String size;//Formatted string type of the file size
    private long reallySize;//Long been formatted file size (mainly used to calculate the total how many files selected)
    private String time;//File modification time
    private String filePath;//The file path
    private int type;// 0 cross 1 upload fails on 2 uploaded successfully cancel the upload || 74 download not 5 6 7 download failed download successfully 8 cancel the download

    private String fileUrl;//Remote file path
    private String uploadUserName;//The uploader
    private boolean isdel;//Whether can delete 0 can't be 1 (administrators, group manager can delete all, file upload can erase your file)
    private String fid;//file id;
    private String sourceName;//Collect list file from XXX
    private int sourceType;// 	Type 0 message file group 1 2 files were deleted the administrator group
    private long createTime;//Upload file sorting
    private String gid;//group id
    private int percent;//File upload download progress
    private long expireTime;//Expiration time
    private boolean isUploadFile;//Whether the upload
    private long dateline;//The current timestamp server system
    private int isCollect;//Is collection  0 no collection 1 have to collect
    private long markTime;//File collection time

    public FileChildVo() {
    }

    protected FileChildVo(Parcel in) {
        fileType = in.readString();
        selectState = in.readByte() != 0;
        name = in.readString();
        size = in.readString();
        reallySize = in.readLong();
        time = in.readString();
        filePath = in.readString();
        type = in.readInt();
        fileUrl = in.readString();
        uploadUserName = in.readString();
        isdel = in.readByte() != 0;
        fid = in.readString();
        sourceName = in.readString();
        sourceType = in.readInt();
        createTime = in.readLong();
        gid = in.readString();
        percent = in.readInt();
        expireTime = in.readLong();
        isUploadFile = in.readByte() != 0;
        dateline = in.readLong();
        isCollect = in.readInt();
        markTime = in.readLong();
    }

    public static final Creator<FileChildVo> CREATOR = new Creator<FileChildVo>() {
        @Override
        public FileChildVo createFromParcel(Parcel in) {
            return new FileChildVo(in);
        }

        @Override
        public FileChildVo[] newArray(int size) {
            return new FileChildVo[size];
        }
    };

    public long getMarkTime() {
        return markTime;
    }

    public void setMarkTime(long markTime) {
        this.markTime = markTime;
    }

    public long getDateline() {
        return dateline;
    }

    public void setDateline(long dateline) {
        this.dateline = dateline;
    }

    public int getIsCollect() {
        return isCollect;
    }

    public void setIsCollect(int isCollect) {
        this.isCollect = isCollect;
    }

    public boolean isUploadFile() {
        return isUploadFile;
    }

    public void setUploadFile(boolean uploadFile) {
        isUploadFile = uploadFile;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public int getSourceType() {
        return sourceType;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public void setSourceType(int sourceType) {
        this.sourceType = sourceType;
    }


    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public boolean isdel() {
        return isdel;
    }

    public void setIsdel(boolean isdel) {
        this.isdel = isdel;
    }

    public String getUploadUserName() {
        return uploadUserName;
    }

    public void setUploadUserName(String uploadUserName) {
        this.uploadUserName = uploadUserName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getReallySize() {
        return reallySize;
    }

    public void setReallySize(long reallySize) {
        this.reallySize = reallySize;
    }

    public boolean isSelectState() {
        return selectState;
    }

    public void setSelectState(boolean selectState) {
        this.selectState = selectState;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public FileChildVo parse(JSONObject object) {
        if (object == null) {
            return null;
        }

        setReallySize(object.optLong("filesize"));
        setSize(FileSizeUtils.formatFileSize(getReallySize()));
        setFileUrl(object.optString("fileurl"));
        setUploadUserName(object.optString("username"));
        setIsdel(object.optInt("isdel") == 1);
        setFid(object.optString("fid"));
        setSourceType(object.optInt("filetype"));
        setExpireTime(object.optLong("expiretime"));
        setIsCollect(object.optInt("isCollect"));
        setSourceName(object.optString("source"));
        setMarkTime(object.optLong("collecttime"));
        if (!TextUtils.isEmpty(getFileUrl())) {
            String[] pathName = getFileUrl().split("/");
            String[] fileName = pathName[pathName.length - 1].split("_");
            StringBuffer sb = new StringBuffer();
            for (int i = 2; i < fileName.length; i++) {
                sb.append(fileName[i]);
            }
            try {
                setName(URLDecoder.decode(sb.toString(), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fileType);
        dest.writeByte((byte) (selectState ? 1 : 0));
        dest.writeString(name);
        dest.writeString(size);
        dest.writeLong(reallySize);
        dest.writeString(time);
        dest.writeString(filePath);
        dest.writeInt(type);
        dest.writeString(fileUrl);
        dest.writeString(uploadUserName);
        dest.writeByte((byte) (isdel ? 1 : 0));
        dest.writeString(fid);
        dest.writeString(sourceName);
        dest.writeInt(sourceType);
        dest.writeLong(createTime);
        dest.writeString(gid);
        dest.writeInt(percent);
        dest.writeLong(expireTime);
        dest.writeByte((byte) (isUploadFile ? 1 : 0));
        dest.writeLong(dateline);
        dest.writeInt(isCollect);
        dest.writeLong(markTime);
    }
}
