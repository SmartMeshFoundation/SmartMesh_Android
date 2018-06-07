package com.lingtuan.firefly.wallet.util;

import android.content.Context;
import android.text.TextUtils;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.raiden.RaidenNet;
import com.lingtuan.firefly.raiden.RaidenUrl;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.SDCardCtrl;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class WalletStorage {
    
    private ArrayList<StorableWallet> mapdb;
    private ArrayList<StorableWallet> mapdbLogin;
    private static WalletStorage instance;
    private String walletToExport; // Used as temp if users wants to export but still needs to grant write permission


    public static WalletStorage getInstance(Context context) {
        if (instance == null) {
            synchronized (WalletStorage.class) {
                if (instance == null) {
                    instance = new WalletStorage(context);
                }
            }
        }
        return instance;
    }

    public synchronized void destroy() {
        instance = null;
    }
    
    private WalletStorage(Context context) {
        try {
            mapdb = new ArrayList<>();
            load(context);
        } catch (Exception e) {
            e.printStackTrace();
            
        }
    }
    
    public synchronized boolean add(StorableWallet storableWallet, Context context) {
        for (int i = 0; i < mapdb.size(); i++) {
            if (mapdb.get(i).getPublicKey().equals(storableWallet.getPublicKey())) {
                return true;
            }
        }
        mapdb.add(storableWallet);
        addWalletToList(context, storableWallet);
        return true;
    }
    
    /**
     * update wallet list
     *
     * @param context        context
     * @param storableWallet wallet
     */
    public synchronized boolean addWalletList(StorableWallet storableWallet, Context context) {
        if (mapdb != null) {
            mapdb.clear();
        } else {
            mapdb = new ArrayList<>();
        }
        try {
            load(context);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        boolean hasWallet = false;
        for (int i = 0; i < mapdb.size(); i++) {
            if (mapdb.get(i).getPublicKey().equals(storableWallet.getPublicKey())) {
                mapdb.get(i).setSelect(true);
                hasWallet = true;
            } else {
                mapdb.get(i).setSelect(false);
            }
        }
        if (hasWallet) {
            return true;
        }
        storableWallet.setSelect(true);
        mapdb.add(storableWallet);
        addWalletToList(context, storableWallet);
        return true;
    }
    
    /**
     * update wallet list
     *
     * @param publicKey wallet
     */
    public synchronized void updateMapDb(String publicKey) {
        if (mapdb == null) {
            return;
        }
        for (int i = 0; i < mapdb.size(); i++) {
            if (TextUtils.equals(mapdb.get(i).getPublicKey(), publicKey)) {
                mapdb.get(i).setSelect(true);
            } else {
                mapdb.get(i).setSelect(false);
            }
        }
    }
    
    /**
     * check wallet exists
     *
     * @param address address
     */
    public synchronized boolean checkExists(String address) {
        for (int i = 0; i < mapdb.size(); i++) {
            if (mapdb.get(i).getPublicKey().equalsIgnoreCase(address)) {
                return true;
            }
        }
        return false;
    }
    
    public synchronized ArrayList<StorableWallet> get() {
        return mapdb;
    }
    
    
    /**
     * increase the purse lists
     *
     * @ param storableWallet wallet
     */
    public void addWalletToList(Context context, StorableWallet storableWallet) {
        String walletList = MySharedPrefs.readWalletList(context);
        try {
            JSONObject json;
            if (TextUtils.isEmpty(walletList)) {
                json = new JSONObject();
            } else {
                json = new JSONObject(walletList);
            }
            JSONArray array = json.optJSONArray("data");
            if (array == null) {
                array = new JSONArray();
            }
            JSONObject newWallet = new JSONObject();
            newWallet.put(Constants.WALLET_NAME, storableWallet.getWalletName());
            newWallet.put(Constants.WALLET_ADDRESS, storableWallet.getPublicKey());
            newWallet.put(Constants.WALLET_EXTRA, storableWallet.getCanExportPrivateKey());
            newWallet.put(Constants.WALLET_BACKUP, storableWallet.isBackup());
            newWallet.put(Constants.WALLET_SELECT, storableWallet.isSelect());
            newWallet.put(Constants.WALLET_ICON, storableWallet.getImgId());
            array.put(newWallet);
            json.put("data", array);
            int walletMode = MySharedPrefs.readInt(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN);
            if (walletMode == 0 && NextApplication.myInfo != null) {
                MySharedPrefs.write(context, MySharedPrefs.FILE_WALLET, NextApplication.myInfo.getLocalId(), json.toString());
                addWalletAllToList(context, storableWallet);
            } else if (walletMode == 1) {
                MySharedPrefs.write(context, MySharedPrefs.FILE_WALLET, MySharedPrefs.KEY_ALL_WALLET, json.toString());
                addWalletModeToList(context, storableWallet);
            } else {
                MySharedPrefs.write(context, MySharedPrefs.FILE_WALLET, MySharedPrefs.KEY_WALLET, json.toString());
                addWalletAllToList(context, storableWallet);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * increase the purse lists
     *
     * @ param storableWallet wallet
     */
    public void addWalletAllToList(Context context, StorableWallet storableWallet) {
        String walletList = MySharedPrefs.readWalletModeAllList(context);
        try {
            JSONObject json;
            if (TextUtils.isEmpty(walletList)) {
                json = new JSONObject();
            } else {
                json = new JSONObject(walletList);
            }
            JSONArray array = json.optJSONArray("data");
            if (array == null) {
                array = new JSONArray();
            }
            for (int i = 0; i < array.length(); i++) {
                String address = array.optJSONObject(i).optString(Constants.WALLET_ADDRESS);
                if (TextUtils.equals(address, storableWallet.getPublicKey())) {
                    return;
                }
            }
            JSONObject newWallet = new JSONObject();
            newWallet.put(Constants.WALLET_NAME, storableWallet.getWalletName());
            newWallet.put(Constants.WALLET_ADDRESS, storableWallet.getPublicKey());
            newWallet.put(Constants.WALLET_EXTRA, storableWallet.getCanExportPrivateKey());
            newWallet.put(Constants.WALLET_BACKUP, storableWallet.isBackup());
            newWallet.put(Constants.WALLET_SELECT, storableWallet.isSelect());
            newWallet.put(Constants.WALLET_ICON, storableWallet.getImgId());
            array.put(newWallet);
            json.put("data", array);
            MySharedPrefs.write(context, MySharedPrefs.FILE_WALLET, MySharedPrefs.KEY_ALL_WALLET, json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * increase the purse lists
     *
     * @ param storableWallet wallet
     */
    public void addWalletModeToList(Context context, StorableWallet storableWallet) {
        String walletList = MySharedPrefs.readWalletModeList(context);
        try {
            JSONObject json;
            if (TextUtils.isEmpty(walletList)) {
                json = new JSONObject();
            } else {
                json = new JSONObject(walletList);
            }
            JSONArray array = json.optJSONArray("data");
            if (array == null) {
                array = new JSONArray();
            }
            for (int i = 0; i < array.length(); i++) {
                String address = array.optJSONObject(i).optString(Constants.WALLET_ADDRESS);
                if (TextUtils.equals(address, storableWallet.getPublicKey())) {
                    return;
                }
            }
            JSONObject newWallet = new JSONObject();
            newWallet.put(Constants.WALLET_NAME, storableWallet.getWalletName());
            newWallet.put(Constants.WALLET_ADDRESS, storableWallet.getPublicKey());
            newWallet.put(Constants.WALLET_EXTRA, storableWallet.getCanExportPrivateKey());
            newWallet.put(Constants.WALLET_BACKUP, storableWallet.isBackup());
            newWallet.put(Constants.WALLET_SELECT, storableWallet.isSelect());
            newWallet.put(Constants.WALLET_ICON, storableWallet.getImgId());
            array.put(newWallet);
            json.put("data", array);
            MySharedPrefs.write(context, MySharedPrefs.FILE_WALLET, MySharedPrefs.KEY_WALLET, json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * update purse list whether can export the private key
     * update purse list whether backup
     *
     * @param isCopy true update back  false uodate select
     * @ param address wallet address
     */
    public synchronized void updateWalletToList(Context context, String address, boolean isCopy) {
        String walletList;
        int walletMode = MySharedPrefs.readInt(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN);
        if (walletMode == 0 && NextApplication.myInfo != null) {
            walletList = MySharedPrefs.readString(context, MySharedPrefs.FILE_WALLET, NextApplication.myInfo.getLocalId());
        } else if (walletMode == 1) {
            walletList = MySharedPrefs.readString(context, MySharedPrefs.FILE_WALLET, MySharedPrefs.KEY_ALL_WALLET);
        } else {
            walletList = MySharedPrefs.readString(context, MySharedPrefs.FILE_WALLET, MySharedPrefs.KEY_WALLET);
        }
        try {
            JSONObject json;
            if (TextUtils.isEmpty(walletList)) {
                json = new JSONObject();
            } else {
                json = new JSONObject(walletList);
            }
            JSONArray array = json.optJSONArray("data");
            if (array == null) {
                array = new JSONArray();
            }
            JSONArray newArray = new JSONArray();
            for (int i = 0; i < array.length(); i++) {
                JSONObject newWallet = new JSONObject();
                newWallet.put(Constants.WALLET_ADDRESS, array.optJSONObject(i).optString(Constants.WALLET_ADDRESS));
                newWallet.put(Constants.WALLET_NAME, array.optJSONObject(i).optString(Constants.WALLET_NAME));
                newWallet.put(Constants.WALLET_SELECT, array.optJSONObject(i).optString(Constants.WALLET_SELECT));
                newWallet.put(Constants.WALLET_EXTRA, array.optJSONObject(i).optString(Constants.WALLET_EXTRA));
                newWallet.put(Constants.WALLET_BACKUP, array.optJSONObject(i).optString(Constants.WALLET_BACKUP));
                newWallet.put(Constants.WALLET_ICON, array.optJSONObject(i).optInt(Constants.WALLET_ICON));
                if (isCopy) {
                    if (address.equals(array.optJSONObject(i).optString(Constants.WALLET_ADDRESS))) {
                        newWallet.put(Constants.WALLET_EXTRA, 0);
                        newWallet.put(Constants.WALLET_BACKUP, true);
                    }
                } else {
                    if (address.equals(array.optJSONObject(i).optString(Constants.WALLET_ADDRESS))) {
                        newWallet.put(Constants.WALLET_SELECT, true);
                    } else {
                        newWallet.put(Constants.WALLET_SELECT, false);
                    }
                }
                newArray.put(newWallet);
            }
            json.put("data", newArray);
            if (walletMode == 0 && NextApplication.myInfo != null) {
                MySharedPrefs.write(context, MySharedPrefs.FILE_WALLET, NextApplication.myInfo.getLocalId(), json.toString());
            } else if (walletMode == 1) {
                MySharedPrefs.write(context, MySharedPrefs.FILE_WALLET, MySharedPrefs.KEY_ALL_WALLET, json.toString());
            } else {
                MySharedPrefs.write(context, MySharedPrefs.FILE_WALLET, MySharedPrefs.KEY_WALLET, json.toString());
            }
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * to delete the wallet
     *
     * @ param address wallet address
     * @ param type 0 ordinary purse 1 to delete the wallet
     */
    public void removeWallet(String address, int type, Context context) {
        int position = -1;
        for (int i = 0; i < mapdb.size(); i++) {
            if (mapdb.get(i).getPublicKey().equalsIgnoreCase(address)) {
                position = i;
                break;
            }
        }
        if (position >= 0) {
            if (type == 0) {
                new File(context.getFilesDir(), SDCardCtrl.WALLERPATH + "/" + address.substring(2, address.length())).delete();
            }
            delWalletList(context, address);
            mapdb.remove(position);
        }
        
    }
    
    /**
     * delete wallet
     *
     * @param context       context
     * @param walletAddress wallet address
     */
    public void delWalletList(Context context, String walletAddress) {
        String walletList;
        int walletMode = MySharedPrefs.readInt(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_WALLET_PATTERN);
        if (walletMode == 0 && NextApplication.myInfo != null) {
            walletList = MySharedPrefs.readString(context, MySharedPrefs.FILE_WALLET, NextApplication.myInfo.getLocalId());
        } else if (walletMode == 1) {
            walletList = MySharedPrefs.readString(context, MySharedPrefs.FILE_WALLET, MySharedPrefs.KEY_ALL_WALLET);
        } else {
            walletList = MySharedPrefs.readString(context, MySharedPrefs.FILE_WALLET, MySharedPrefs.KEY_WALLET);
        }
        try {
            JSONObject json;
            if (TextUtils.isEmpty(walletList)) {
                json = new JSONObject();
            } else {
                json = new JSONObject(walletList);
            }
            JSONArray array = json.optJSONArray("data");
            if (array == null) {
                array = new JSONArray();
            }
            JSONArray newArray = new JSONArray();
            for (int i = 0; i < array.length(); i++) {
                if (!walletAddress.equals(array.optJSONObject(i).optString(Constants.WALLET_ADDRESS))) {
                    JSONObject newWallet = new JSONObject();
                    newWallet.put(Constants.WALLET_ADDRESS, array.optJSONObject(i).optString(Constants.WALLET_ADDRESS));
                    newWallet.put(Constants.WALLET_NAME, array.optJSONObject(i).optString(Constants.WALLET_NAME));
                    newWallet.put(Constants.WALLET_EXTRA, array.optJSONObject(i).optInt(Constants.WALLET_EXTRA));
                    newWallet.put(Constants.WALLET_BACKUP, array.optJSONObject(i).optBoolean(Constants.WALLET_BACKUP));
                    newWallet.put(Constants.WALLET_SELECT, array.optJSONObject(i).optBoolean(Constants.WALLET_SELECT));
                    newWallet.put(Constants.WALLET_ICON, array.optJSONObject(i).optInt(Constants.WALLET_ICON));
                    newArray.put(newWallet);
                }
            }
            json.put("data", newArray);
            if (walletMode == 0 && NextApplication.myInfo != null) {
                MySharedPrefs.write(context, MySharedPrefs.FILE_WALLET, NextApplication.myInfo.getLocalId(), json.toString());
            } else if (walletMode == 1) {
                MySharedPrefs.write(context, MySharedPrefs.FILE_WALLET, MySharedPrefs.KEY_ALL_WALLET, json.toString());
            } else if (walletMode == 2) {
                MySharedPrefs.write(context, MySharedPrefs.FILE_WALLET, MySharedPrefs.KEY_WALLET, json.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    public static String stripWalletName(String s) {
        if (s.lastIndexOf("--") > 0) s = s.substring(s.lastIndexOf("--") + 2);
        if (s.endsWith(".json")) s = s.substring(0, s.indexOf(".json"));
        return s;
    }
    
    
    /**
     * access to the private key
     *
     * @ param password purse password
     * @ param wallet wallet address public key
     */
    public Credentials getFullWallet(Context context, String password, String wallet) throws IOException, JSONException, CipherException {
        if (wallet.startsWith("0x")) wallet = wallet.substring(2, wallet.length());
        return WalletUtils.loadCredentials(password, new File(context.getFilesDir(), SDCardCtrl.WALLERPATH + "/" + RaidenUrl.getInatance().initLocalKeyStore(wallet)));
    }
    
    /**
     * get the KeyStore
     *
     * @ param password   purse password
     * @ param wallet     wallet address public key
     */
    public String getWalletKeyStore(Context context, String password, String wallet) throws IOException, JSONException, CipherException {
        if (wallet.startsWith("0x")) wallet = wallet.substring(2, wallet.length());
        WalletUtils.loadCredentials(password, new File(context.getFilesDir(), SDCardCtrl.WALLERPATH + "/" + RaidenUrl.getInatance().initLocalKeyStore(wallet)));
        File file = new File(context.getFilesDir(), SDCardCtrl.WALLERPATH + "/" + RaidenUrl.getInatance().initLocalKeyStore(wallet));
        InputStream in = new FileInputStream(file);
        int flen = (int) file.length();
        byte[] strBuffer = new byte[flen];
        in.read(strBuffer, 0, flen);
        return new String(strBuffer);
    }
    
    /**
     * Read in json
     */
    @SuppressWarnings("unchecked")
    public synchronized void load(Context context) throws IOException, ClassNotFoundException {
        String walletList = MySharedPrefs.readWalletList(context);
        if (TextUtils.isEmpty(walletList)) {
            return;
        }
        try {
            JSONObject object = new JSONObject(walletList);
            JSONArray walletArray = object.optJSONArray("data");
            int hasSelect = 0;
            int index = 0;
            for (int i = 0; i < walletArray.length(); i++) {
                JSONObject walletObj = walletArray.optJSONObject(i);
                if (walletObj.optBoolean(Constants.WALLET_SELECT)) {
                    hasSelect++;
                    if (index == 0) {
                        index = i;
                    }
                }
            }
            
            for (int i = 0; i < walletArray.length(); i++) {
                JSONObject walletObj = walletArray.optJSONObject(i);
                StorableWallet storableWallet = new StorableWallet();
                storableWallet.setPublicKey(walletObj.optString(Constants.WALLET_ADDRESS));
                storableWallet.setWalletName(walletObj.optString(Constants.WALLET_NAME));
                storableWallet.setCanExportPrivateKey(walletObj.optInt(Constants.WALLET_EXTRA));
                storableWallet.setBackup(walletObj.optBoolean(Constants.WALLET_BACKUP));
                storableWallet.setImgId(walletObj.optInt(Constants.WALLET_ICON));
                if (hasSelect == 1) {
                    storableWallet.setSelect(walletObj.optBoolean(Constants.WALLET_SELECT));
                } else {
                    if (i == index) {
                        storableWallet.setSelect(true);
                    } else {
                        storableWallet.setSelect(false);
                    }
                }
                File destination = new File(new File(context.getFilesDir(), SDCardCtrl.WALLERPATH), RaidenUrl.getInatance().initLocalKeyStore(storableWallet.getPublicKey()));
                if (!destination.exists()) {
                    storableWallet.setWalletType(1);
                }
                mapdb.add(storableWallet);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    public synchronized ArrayList<StorableWallet> getAll() {
        return mapdbLogin;
    }
    
    /**
     * Read in json
     *
     * @param context context
     */
    @SuppressWarnings("unchecked")
    public synchronized ArrayList<StorableWallet> loadAll(Context context) {
        String walletList = MySharedPrefs.readWalletModeAllList(context);
        if (TextUtils.isEmpty(walletList)) {
            return null;
        }
        if (mapdbLogin == null) {
            mapdbLogin = new ArrayList<>();
        } else {
            mapdbLogin.clear();
        }
        try {
            JSONObject object = new JSONObject(walletList);
            JSONArray walletArray = object.optJSONArray("data");
            int hasSelect = 0;
            int index = 0;
            for (int i = 0; i < walletArray.length(); i++) {
                JSONObject walletObj = walletArray.optJSONObject(i);
                if (walletObj.optBoolean(Constants.WALLET_SELECT)) {
                    hasSelect++;
                    if (index == 0) {
                        index = i;
                    }
                }
            }
            
            for (int i = 0; i < walletArray.length(); i++) {
                JSONObject walletObj = walletArray.optJSONObject(i);
                StorableWallet storableWallet = new StorableWallet();
                storableWallet.setPublicKey(walletObj.optString(Constants.WALLET_ADDRESS));
                storableWallet.setWalletName(walletObj.optString(Constants.WALLET_NAME));
                storableWallet.setCanExportPrivateKey(walletObj.optInt(Constants.WALLET_EXTRA));
                storableWallet.setBackup(walletObj.optBoolean(Constants.WALLET_BACKUP));
                storableWallet.setSelect(walletObj.optBoolean(Constants.WALLET_SELECT));
                storableWallet.setImgId(walletObj.optInt(Constants.WALLET_ICON));
                if (hasSelect == 1) {
                    storableWallet.setSelect(walletObj.optBoolean(Constants.WALLET_SELECT));
                } else {
                    if (i == index) {
                        storableWallet.setSelect(true);
                    } else {
                        storableWallet.setSelect(false);
                    }
                }
                File destination = new File(new File(context.getFilesDir(), SDCardCtrl.WALLERPATH), RaidenUrl.getInatance().initLocalKeyStore(storableWallet.getPublicKey()));
                if (!destination.exists()) {
                    storableWallet.setWalletType(1);
                }
                mapdbLogin.add(storableWallet);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mapdbLogin;
    }
    
    /**
     * Read in json
     *
     * @param context context
     */
    @SuppressWarnings("unchecked")
    public synchronized void reLoad(Context context) throws IOException, ClassNotFoundException {
        String walletList = MySharedPrefs.readWalletList(context);
        String walletModeList = MySharedPrefs.readWalletModeList(context);
        if (TextUtils.isEmpty(walletList) && TextUtils.isEmpty(walletModeList)) {
            return;
        }
        if (mapdb != null) {
            mapdb.clear();
        } else {
            mapdb = new ArrayList<>();
        }
        
        if (!TextUtils.isEmpty(walletList) && !TextUtils.isEmpty(walletModeList)) {
            try {
                JSONObject object = new JSONObject(walletList);
                JSONArray walletArray = object.optJSONArray("data");
                addWalletArray(context, walletArray, false, true);
                JSONObject objectMode = new JSONObject(walletModeList);
                JSONArray walletModeArray = objectMode.optJSONArray("data");
                boolean hasExists = false;
                for (int i = 0; i < walletModeArray.length(); i++) {
                    JSONObject walletObj = walletModeArray.optJSONObject(i);
                    for (int j = 0; j < mapdb.size(); j++) {
                        hasExists = false;
                        if (mapdb.get(j).getPublicKey().equals(walletObj.optString(Constants.WALLET_ADDRESS))) {
                            if (walletObj.optBoolean(Constants.WALLET_SELECT)) {
                                mapdb.get(j).setSelect(true);
                            }
                            hasExists = true;
                            break;
                        }
                    }
                    if (hasExists) {
                        continue;
                    }
                    StorableWallet storableWallet = new StorableWallet();
                    storableWallet.setPublicKey(walletObj.optString(Constants.WALLET_ADDRESS));
                    storableWallet.setWalletName(walletObj.optString(Constants.WALLET_NAME));
                    storableWallet.setCanExportPrivateKey(walletObj.optInt(Constants.WALLET_EXTRA));
                    storableWallet.setBackup(walletObj.optBoolean(Constants.WALLET_BACKUP));
                    storableWallet.setSelect(walletObj.optBoolean(Constants.WALLET_SELECT));
                    storableWallet.setSelect(walletObj.optBoolean(Constants.WALLET_ICON));
                    File destination = new File(new File(context.getFilesDir(), SDCardCtrl.WALLERPATH), RaidenUrl.getInatance().initLocalKeyStore(storableWallet.getPublicKey()));
                    if (!destination.exists()) {
                        storableWallet.setWalletType(1);
                    }
                    addWalletToList(NextApplication.mContext, storableWallet);
                    mapdb.add(storableWallet);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (TextUtils.isEmpty(walletList) && !TextUtils.isEmpty(walletModeList)) {
            try {
                JSONObject object = new JSONObject(walletModeList);
                JSONArray walletArray = object.optJSONArray("data");
                addWalletArray(context, walletArray, true, false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (!TextUtils.isEmpty(walletList) && TextUtils.isEmpty(walletModeList)) {
            try {
                JSONObject object = new JSONObject(walletList);
                JSONArray walletArray = object.optJSONArray("data");
                addWalletArray(context, walletArray, false, false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        addAddressToServer(mapdb);
    }
    
    /**
     * Read in json
     *
     * @param context context
     */
    @SuppressWarnings("unchecked")
    public synchronized void reLoadUserWallet(Context context) throws IOException, ClassNotFoundException {
        String walletList = MySharedPrefs.readWalletList(context);
        if (mapdb != null) {
            mapdb.clear();
        } else {
            mapdb = new ArrayList<>();
        }
        if (TextUtils.isEmpty(walletList)) {
            return;
        }
        try {
            JSONObject object = new JSONObject(walletList);
            JSONArray walletArray = object.optJSONArray("data");
            addWalletArray(context, walletArray, false, false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * load wallet list
     *
     * @param context     context
     * @param walletArray wallet json array
     */
    private void addWalletArray(Context context, JSONArray walletArray, boolean needAdd, boolean reSetSelect) {
        if (walletArray == null) {
            return;
        }
        int hasSelect = 0;
        int index = 0;
        for (int i = 0; i < walletArray.length(); i++) {
            JSONObject walletObj = walletArray.optJSONObject(i);
            if (walletObj.optBoolean(Constants.WALLET_SELECT)) {
                hasSelect++;
                if (index == 0) {
                    index = i;
                }
            }
        }
        
        for (int i = 0; i < walletArray.length(); i++) {
            JSONObject walletObj = walletArray.optJSONObject(i);
            StorableWallet storableWallet = new StorableWallet();
            storableWallet.setPublicKey(walletObj.optString(Constants.WALLET_ADDRESS));
            storableWallet.setWalletName(walletObj.optString(Constants.WALLET_NAME));
            storableWallet.setCanExportPrivateKey(walletObj.optInt(Constants.WALLET_EXTRA));
            storableWallet.setBackup(walletObj.optBoolean(Constants.WALLET_BACKUP));
            storableWallet.setImgId(walletObj.optInt(Constants.WALLET_ICON));
            if (reSetSelect) {
                storableWallet.setSelect(false);
            } else {
                if (hasSelect == 1) {
                    storableWallet.setSelect(walletObj.optBoolean(Constants.WALLET_SELECT));
                } else {
                    if (i == index) {
                        storableWallet.setSelect(true);
                    } else {
                        storableWallet.setSelect(false);
                    }
                }
            }
            File destination = new File(new File(context.getFilesDir(), SDCardCtrl.WALLERPATH), RaidenUrl.getInatance().initLocalKeyStore(storableWallet.getPublicKey()));
            if (!destination.exists()) {
                storableWallet.setWalletType(1);
            }
            if (needAdd) {
                addWalletToList(NextApplication.mContext, storableWallet);
            }
            mapdb.add(storableWallet);
        }
    }
    
    
    /**
     * update wallet to all
     *
     * @param context context
     */
    public synchronized void firstLoadAllWallet(Context context) {
        String walletList = MySharedPrefs.readUserWalletList(context);
        if (TextUtils.isEmpty(walletList)) {
            return;
        }
        MySharedPrefs.write(context, MySharedPrefs.FILE_WALLET, MySharedPrefs.KEY_ALL_WALLET, walletList);
        String versionCode = Utils.getVersionCode(context) + "";
        MySharedPrefs.write(context, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_IS_FIRST_WALLET_USE, versionCode);
    }
    
    /**
     * add address to server
     *
     * @param mapdb wallet list
     */
    private void addAddressToServer(ArrayList<StorableWallet> mapdb) {
        if (mapdb == null || mapdb.size() <= 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mapdb.size(); i++) {
            String address = mapdb.get(i).getPublicKey();
            if (!address.startsWith("0x")) {
                address = "0x" + address;
            }
            sb.append(address).append(",");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        NetRequestImpl.getInstance().addAddress(sb.toString(), new RequestListener() {
            @Override
            public void start() {
                
            }
            
            @Override
            public void success(JSONObject response) {
                
            }
            
            @Override
            public void error(int errorCode, String errorMsg) {
                
            }
        });
    }
    
}
