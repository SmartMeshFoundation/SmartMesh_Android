package com.lingtuan.firefly.util;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.Environment;
import android.text.format.Formatter;

import com.lingtuan.firefly.NextApplication;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * SD card auxiliary class
 *
 * @author ck 1124
 */
public class SDFileUtils {
    private static SDFileUtils sdFile = null;

    private String basePath = SDCardCtrl.ROOTPATH;

    public static String audioPath = Environment.getExternalStorageDirectory() + "/.firefly/audio/";
    public static String videoPath = Environment.getExternalStorageDirectory() + "/firefly/video";
    public static String advideoPath = Environment.getExternalStorageDirectory() + "/.firefly/adaudio/";

    public static synchronized SDFileUtils getInstance() {
        if (sdFile == null) {
            sdFile = new SDFileUtils();
        }
        return sdFile;
    }

    private SDFileUtils() {
        createDir(getSDPath() + basePath);
    }

    /**
     * Check whether the SD card is inserted, and returns the path to the SD card
     */
    public static boolean SDCardIsOk() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * The SD path
     *
     * @return /sdcard
     */
    public String getSDPath() {
        // To determine whether a sd card
        if (Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            File sdDir = Environment.getExternalStorageDirectory();// Access to the root directory
            return sdDir.getPath();
        }
        return "/mnt/sdcard";
    }

    /**
     * Create a folder
     *
     * @param dirName
     */
    public void createDir(String dirName) {
        File destDir = new File(dirName);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
    }

    /**
     * The SD card path
     *
     * @return
     */
    public String getSDCardPath() {
        return Environment.getExternalStorageDirectory() + "/";
    }

    public void mkDir(File file) {

        if (file == null) return;

        if (file.getParentFile().exists()) {
            file.mkdir();
        } else {
            mkDir(file.getParentFile());
            file.mkdir();
        }

    }

    /**
     * Create a file on the SD card
     *
     * @param fileName
     * @return
     */
    public File creatSDFile(String fileName) {
        File file = new File(getSDCardPath() + fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return file;
    }

    /**
     * Create a directory in SD card
     *
     * @param dirName
     * @return
     */
    public File createSDDir(String dirName) {
        File dir = new File(getSDCardPath() + dirName);
        dir.mkdir();
        return dir;
    }

    /**
     * Check the folder on your SD card if there is a (relative)
     *
     * @param fileName
     * @return
     */
    public boolean isFileExit(String fileName) {
        File file = new File(getSDCardPath() + fileName);
        return file.exists();
    }

    /**
     *Check the SD card folder exists (absolute path)
     *
     * @param fileName
     * @return
     */
    public boolean isFileExist(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    /**
     * to determine whether a file exists
     * @ param name the file name
     * @return
     */
    public boolean fileExist(String name) {
        File f = new File(getSDCardPath() + name);
        return f.exists();
    }

    /**
     * to the inside of the InputStream data written to the SD card
     * @ param path folder path
     * @ param fileName file name
     * @ param input input stream
     * @return
     */
    public File writeFileToSDCard(String path, String fileName,InputStream input) {
        File file = null;
        OutputStream ops = null;
        try {
            createSDDir(path);
            file = creatSDFile(path + fileName);
            ops = new FileOutputStream(file);
            byte buffer[] = new byte[4 * 1024];
            while ((input.read(buffer) != -1)) {
                ops.write(buffer);
            }
            ops.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != ops) {
                    ops.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * input stream into a picture
     * @ param is the input stream
     * @ param imgPathTemp folder path
     * @ param fileName file name
     * @return
     */
    public File inputToFile(InputStream is, String imgPathTemp, String fileName) {
        createDir(imgPathTemp);
        File file = new File(imgPathTemp, fileName);// Save the file
        try {
            if (!file.exists() && !file.isDirectory()) {
                // Can be judged by file name, here if local have this picture
                FileOutputStream fos = new FileOutputStream(file);
                int data = is.read();
                while (data != -1) {
                    fos.write(data);
                    data = is.read();
                }
                fos.close();
                is.close();
            }
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *According to the byte array, generated file
     */
    public void getFile(byte[] bfile, String imgPathTemp, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        createDir(imgPathTemp);
        File file = new File(imgPathTemp, fileName);// Save the file
        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * data into a file
     * @ param datas data source
     * @ param imgPathTemp folder path
     * @ param fileName file name
     * @return
     */
    public File ByteToFile(byte[] datas, String imgPathTemp, String fileName) {
        createDir(imgPathTemp);
        File file = new File(imgPathTemp, fileName);// Save the file
        try {
            if (!file.exists() && !file.isDirectory()) {
                // Can be judged by file name, here if local have this picture
                FileOutputStream fos = new FileOutputStream(file);
                ByteArrayInputStream bais = new ByteArrayInputStream(datas);
                int data = bais.read();
                while (data != -1) {
                    fos.write(data);
                    data = bais.read();
                }
                fos.close();
                bais.close();
            }
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * write files to the SD card
     * @ param fileName file name
     * @ param message file content
     */
    public void writeFileSdcard(String fileName, String message) {
        try {
            FileOutputStream fout = new FileOutputStream(fileName);
            byte[] bytes = message.getBytes();
            fout.write(bytes);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete the file
     * @param path
     */
    public void deleteFile(String path) {
        File file = new File(path);
        file.delete();
    }

    // Access to system memory available information
    public String getSystemAvaialbeMemorySize(Context ct) {
        // ActivityManager service object
        ActivityManager mActivityManager = (ActivityManager) ct
                .getSystemService(Context.ACTIVITY_SERVICE);
        // Get MemoryInfo object
        MemoryInfo memoryInfo = new MemoryInfo();
        // Get the system memory available, save on MemoryInfo object
        mActivityManager.getMemoryInfo(memoryInfo);
        long memSize = memoryInfo.availMem;

        // Character type conversion
        String availMemStr = formateFileSize(memSize, ct);
        return availMemStr;
    }

    // Call system function, the String conversion long String KB/MB
    private String formateFileSize(long size, Context ct) {
        return Formatter.formatFileSize(ct, size);
    }

    /**
     * Access to the memory card capacity size
     * @param path
     * @return
     */
    public long getRoomSize(String path) {
        File file = new File(path);
        return file.length();
    }

    /**
     * Delete folder and subfolders and files inside
     * @param file
     */
    public static void delete(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }

        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }

            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }

    //Recursive delete all files under the specified path
    public static void deleteAll(File file) {
        try {
            if (file.isFile()) {
                file.delete();
                Utils.notifySystemUpdateFolder(NextApplication.mContext, file);
            } else {

                File[] files = file.listFiles();
                for (File f : files) {
                    deleteAll(f);//The recursive delete every file
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
