package com.lingtuan.firefly.wallet.util;

import android.content.Context;
import android.text.TextUtils;

import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.SDCardCtrl;
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
    private static WalletStorage instance;
    private String walletToExport; // Used as temp if users wants to export but still needs to grant write permission

    public static WalletStorage getInstance(Context context){
        if(instance == null)
            instance = new WalletStorage(context);
        return instance;
    }
    public synchronized void destroy(){
        instance = null;
    }
    private WalletStorage(Context context){
        try {
            mapdb = new ArrayList<>();
            load(context);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public synchronized boolean add(StorableWallet storableWallet, Context context){
        mapdb.add(storableWallet);
        addWalletToList(context,storableWallet);
        return true;
    }

    public synchronized boolean checkExists(String addresse){
        for(int i=0; i < mapdb.size(); i++){
            if(mapdb.get(i).getPublicKey().equalsIgnoreCase(addresse))
            {
                return true;
            }
        }
        return false;
    }

    public synchronized ArrayList<StorableWallet> get(){
        return mapdb;
    }

    public void addWalletToList(Context context,StorableWallet storableWallet){
        String walletList = MySharedPrefs.readWalletList(context);
        try {
            JSONObject json;
            if (TextUtils.isEmpty(walletList)){
                json = new JSONObject();
            }else{
                json = new JSONObject(walletList);
            }
            JSONArray array = json.optJSONArray("data");
            if (array == null){
                array = new JSONArray();
            }
            JSONObject newWallet = new JSONObject();
            newWallet.put(Constants.WALLET_NAME,storableWallet.getWalletName());
            newWallet.put(Constants.WALLET_ADDRESS,storableWallet.getPublicKey());
            newWallet.put(Constants.WALLET_EXTRA,storableWallet.getCanExportPrivateKey());
            array.put(newWallet);
            json.put("data",array);
            MySharedPrefs.write(context,MySharedPrefs.FILE_WALLET,MySharedPrefs.KEY_WALLET,json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateWalletToList(Context context,String address){

        String walletList = MySharedPrefs.readString(context, MySharedPrefs.FILE_WALLET, MySharedPrefs.KEY_WALLET);
        try {
            JSONObject json;
            if (TextUtils.isEmpty(walletList)){
                json = new JSONObject();
            }else{
                json = new JSONObject(walletList);
            }
            JSONArray array = json.optJSONArray("data");
            if (array == null){
                array = new  JSONArray();
            }
            JSONArray newArray = new JSONArray();
            for(int i=0;i<array.length();i++)
            {
                JSONObject newWallet = new JSONObject();
                newWallet.put(Constants.WALLET_ADDRESS,array.optJSONObject(i).optString(Constants.WALLET_ADDRESS));
                newWallet.put(Constants.WALLET_NAME,array.optJSONObject(i).optString(Constants.WALLET_NAME));
                if(address.equals(array.optJSONObject(i).optString(Constants.WALLET_ADDRESS)))
                {
                    newWallet.put(Constants.WALLET_EXTRA,0);
                }
                newArray.put(newWallet);
            }
            json.put("data",newArray);
            MySharedPrefs.write(context,MySharedPrefs.FILE_WALLET,MySharedPrefs.KEY_WALLET,json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void removeWallet(String address, int type,Context context){
        int position = -1;
        for(int i=0; i < mapdb.size(); i++) {
            if (mapdb.get(i).getPublicKey().equalsIgnoreCase(address)) {
                position = i;
                break;
            }
        }
        if(position >= 0) {
            if (type == 0){
                new File(context.getFilesDir(), SDCardCtrl.WALLERPATH + "/" + address.substring(2, address.length())).delete();
            }
            delWalletList(context,address);
            mapdb.remove(position);
        }

    }
    public void delWalletList(Context context,String walletAddress){
        String walletList = MySharedPrefs.readString(context, MySharedPrefs.FILE_WALLET, MySharedPrefs.KEY_WALLET);
        try {
            JSONObject json;
            if (TextUtils.isEmpty(walletList)){
                json = new JSONObject();
            }else{
                json = new JSONObject(walletList);
            }
            JSONArray array = json.optJSONArray("data");
            if (array == null){
                array = new  JSONArray();
            }

            JSONArray newArray = new JSONArray();

            for(int i=0;i<array.length();i++)
            {
                if(!walletAddress.equals(array.optJSONObject(i).optString(Constants.WALLET_ADDRESS)))
                {
                    JSONObject newWallet = new JSONObject();
                    newWallet.put(Constants.WALLET_ADDRESS,array.optJSONObject(i).optString(Constants.WALLET_ADDRESS));
                    newWallet.put(Constants.WALLET_NAME,array.optJSONObject(i).optString(Constants.WALLET_NAME));
                    newWallet.put(Constants.WALLET_EXTRA,array.optJSONObject(i).optInt(Constants.WALLET_EXTRA));
                    newArray.put(newWallet);
                }
            }

            json.put("data",newArray);
            MySharedPrefs.write(context,MySharedPrefs.FILE_WALLET,MySharedPrefs.KEY_WALLET,json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public static String stripWalletName(String s){
        if(s.lastIndexOf("--") > 0)
            s = s.substring(s.lastIndexOf("--")+2);
        if(s.endsWith(".json"))
            s = s.substring(0, s.indexOf(".json"));
        return s;
    }

   public Credentials getFullWallet(Context context, String password, String wallet) throws IOException, JSONException, CipherException {
       if(wallet.startsWith("0x"))
           wallet = wallet.substring(2, wallet.length());
       return WalletUtils.loadCredentials(password, new File(context.getFilesDir(), SDCardCtrl.WALLERPATH + "/" + wallet));
   }
  
    public String getWalletKeyStore(Context context, String password, String wallet) throws IOException, JSONException, CipherException {
        if(wallet.startsWith("0x"))
            wallet = wallet.substring(2, wallet.length());
        WalletUtils.loadCredentials(password, new File(context.getFilesDir(), SDCardCtrl.WALLERPATH + "/" + wallet));
        File file = new File(context.getFilesDir(), SDCardCtrl.WALLERPATH + "/" + wallet);
        InputStream in = new FileInputStream(file);
        int flen = (int)file.length();
        byte[] strBuffer = new byte[flen];
        in.read(strBuffer, 0, flen);
        return new String(strBuffer);
    }


    @SuppressWarnings("unchecked")
    public synchronized void load(Context context) throws IOException, ClassNotFoundException {
        String walletList = MySharedPrefs.readWalletList(context);
        if (TextUtils.isEmpty(walletList)){
            return;
        }
        try {
            JSONObject object = new JSONObject(walletList);
            JSONArray walletArray  = object.optJSONArray("data");
            for(int i=0;i<walletArray.length();i++){
                JSONObject walletObj = walletArray.optJSONObject(i);
                StorableWallet storableWallet = new StorableWallet();
                storableWallet.setPublicKey(walletObj.optString(Constants.WALLET_ADDRESS));
                storableWallet.setWalletName(walletObj.optString(Constants.WALLET_NAME));
                storableWallet.setCanExportPrivateKey(walletObj.optInt(Constants.WALLET_EXTRA));
                if (i == 0){
                    storableWallet.setSelect(true);
                }
                File destination = new File( new File(context.getFilesDir(), SDCardCtrl.WALLERPATH ), storableWallet.getPublicKey());
                if(!destination.exists())
                {
                    storableWallet.setWalletType(1);
                }
                mapdb.add(storableWallet);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
