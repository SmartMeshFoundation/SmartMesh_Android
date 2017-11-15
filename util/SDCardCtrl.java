package com.lingtuan.firefly.util;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * Folder control class
 */
public class SDCardCtrl {

    /**
     * The log cat Tag
     */
    public static final String TAG = "SDCheck";

    /**
     * ROOTPATH
     */
    public static String ROOTPATH = "/.firefly";

    /**
     * UPLOADPATH
     */
    public static String UPLOADPATH = "/upload";

    /**
     * QRCODEPATH
     */
    public static String QRCODEPATH = "/firefly/qrcode";

    /**
     * Wallet path
     * */
    public static String WALLERPATH = "/ethereum/keystore";

    /**
     * GethDroid path
     * */
    public static String DROIDPATH = "/ethereum/GethDroid/";

    /**
     * Ethereum path
     * */
    public static String ETHEREUM = "/ethereum";

    /**
     * ERRORLOGPATH
     */
    public static String ERRORLOGPATH = "/ErrorLog";

    /**
     * OFFLINE
     */
    public static String OFFLINE = "/offLine";

    public static String DOWNLOAD = "/firefly/download";
    /**
     * OFFLINE
     */
    public static String FILE = "/file";


    /**
     * TEMPATH
     */
    public static String TEMPATH = "/tempPath";

    /**
     * ChatPATH
     */
    public static String CHATPATH = "/chat";

    /**
     * IMAGEPATH
     */
    public static String IMAGEPATH = "/images";

    public static String VIDEO = "/video";


    public static String AUDIO = "/audio";

    /**
     * FACE
     */
    public static String FACE = "/face";

    /**
     * CHAT_FILE
     */
    public static String CHAT_FILE_PATH = "/file";


    /**
     * @return ROOTPATH
     */
    public static String getCtrlCPath() {
        return ROOTPATH;
    }

    public static String getErrorLogPath() {
        return ERRORLOGPATH;
    }

    /**
     * @return OFFLINE
     */
    public static String getOfflinePath() {
        return OFFLINE;
    }


    public static String getOfflineDownloadPath() {
        return DOWNLOAD;
    }

    /**
     * @return UPLOADPATH
     */
    public static String getUploadPath() {
        return UPLOADPATH;
    }

    /**
     * @return NEXTDYNAMICPATH
     */
    public static String getChatImagePath() {
        return IMAGEPATH;
    }
    /**
     * @return FILE
     */
    public static String getFilePath() {
        return FILE;
    }

    /**
     * @return AUDIO
     */
    public static String getAudioPath() {
        return AUDIO;
    }

    /**
     * @return TEMPATH
     */
    public static String getTempPath() {
        return TEMPATH;
    }

    public static String getQrcodePath() {
        return QRCODEPATH;
    }

    /**
     * @return Is or not exist SD card
     */
    public static boolean sdCardIsExist() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * <Build data file for this application >
     */
    public static void initPath(Context context) {
        String ROOT;
        if (sdCardIsExist()) {
            ROOT = Environment.getExternalStorageDirectory().getPath();
        } else {
            ROOT = "/mnt/sdcard";
        }
        if (ROOTPATH.equals("/.firefly")) {
            ROOTPATH = ROOT + ROOTPATH;
            ERRORLOGPATH = ROOTPATH + ERRORLOGPATH;
            UPLOADPATH = ROOTPATH + UPLOADPATH;
            QRCODEPATH = ROOT + QRCODEPATH;
            DOWNLOAD = ROOT + DOWNLOAD;
            TEMPATH = ROOTPATH + TEMPATH;
            OFFLINE = ROOTPATH + OFFLINE;
            FILE = ROOTPATH + FILE;
            CHATPATH = ROOTPATH + CHATPATH;
            IMAGEPATH = ROOTPATH + IMAGEPATH;
            VIDEO = ROOTPATH + VIDEO;
            AUDIO = ROOTPATH + AUDIO;
            FACE = ROOTPATH + FACE;
            CHAT_FILE_PATH = CHATPATH + CHAT_FILE_PATH;
        }
        SDFileUtils.getInstance().createDir(ROOTPATH);
        SDFileUtils.getInstance().createDir(ERRORLOGPATH);
        SDFileUtils.getInstance().createDir(UPLOADPATH);
        SDFileUtils.getInstance().createDir(QRCODEPATH);
        SDFileUtils.getInstance().createDir(TEMPATH);
        SDFileUtils.getInstance().createDir(OFFLINE);
        SDFileUtils.getInstance().createDir(CHATPATH);
        SDFileUtils.getInstance().createDir(IMAGEPATH);
        SDFileUtils.getInstance().createDir(VIDEO);
        SDFileUtils.getInstance().createDir(AUDIO);
        SDFileUtils.getInstance().createDir(FACE);
        SDFileUtils.getInstance().createDir(CHAT_FILE_PATH);
        SDFileUtils.getInstance().createDir(context.getFilesDir().getAbsolutePath()+ETHEREUM);
        SDFileUtils.getInstance().createDir(context.getFilesDir().getAbsolutePath()+WALLERPATH);
        SDFileUtils.getInstance().createDir(context.getFilesDir().getAbsolutePath() + DROIDPATH);
    }

    /**
     * Check the SDcard useful space
     *
     * @return
     */
    public static boolean checkSpace() {
        String path = Environment.getExternalStorageDirectory().getPath();
        StatFs statFs = new StatFs(path);
        int blockSize = statFs.getBlockSize() / 1024;
        int blockCount = statFs.getBlockCount();
        int usedBlocks = statFs.getAvailableBlocks() / 1024;
        int totalSpace = blockCount * blockSize / 1024;
        int avaliableSpace = totalSpace - usedBlocks;
        return avaliableSpace >= 64;
    }

    /**
     * to copy a single file
     * @ param oldPath String the original file path Such as: c: / FQF. TXT
     * @ param newPath String copied after the path Such as: f: / FQF. TXT
     * @return boolean
     */
    public static void copyFile(String oldPath, String newPath) {
        try {
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {  //File exists
                InputStream inStream = new FileInputStream(oldPath);  //Read the original file
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                fs.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * <Save the App crash info to sdcard>
     */
    public static String saveCrashInfoToFile(String excepMsg) {
        if (TextUtils.isEmpty(excepMsg)) {
            return "";
        }
        String errorlog = getErrorLogPath();
        FileWriter fw = null;
        PrintWriter pw = null;
        File logFile = null;
        try {
            StringBuilder logSb = new StringBuilder();
            logSb.append("crashlog");
            logSb.append("(");
            logSb.append(Utils.getSimpDate());
            logSb.append(")");
            logSb.append(".txt");
            logFile = new File(errorlog, logSb.toString());
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            fw = new FileWriter(logFile, true);
            pw = new PrintWriter(fw);
            pw.write(excepMsg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.flush();
                pw.close();
            }
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                }
            }
        }
        return logFile == null ? "" : logFile.getAbsolutePath();
    }
}
