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


    /**
     * 增加钱包列表
     * @param storableWallet 钱包
     * */
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


    /**
     * 更新钱包列表 是否可以导出私钥
     * @param address 钱包地址
     * */
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




    /**
     * 删除钱包
     * @param address 钱包地址
     * @param type 0 普通钱包 1 删除钱包
     * */
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
//
//    public void checkForWallets(Context c){
//        // Full wallets
//        File[] wallets = c.getFilesDir().listFiles();
//        if(wallets == null){
//            return;
//        }
//        for(int i=0; i < wallets.length; i++){
//            if(wallets[i].isFile()){
//                if(wallets[i].getName().length() == 40){
//                    add(new FullWallet("0x"+wallets[i].getName(), wallets[i].getName()), c);
//                }
//            }
//        }
//
//        // Watch only
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
//        Map<String, ?> allEntries = preferences.getAll();
//        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
//            if(entry.getKey().length() == 42 && !mapdb.contains(entry.getKey()))
//                add(new WatchWallet(entry.getKey()), c);
//        }
//        if(mapdb.size() > 0)
//            save(c);
//    }

//   public void importingWalletsDetector(NewWalletActivity c){
//       if(!ExternalStorageHandler.hasReadPermission(c)) {
//           ExternalStorageHandler.askForPermissionRead(c);
//           return;
//       }
//       File[] wallets = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Lunary/").listFiles();
//       if(wallets == null){
//           Dialogs.noImportWalletsFound(c);
//           return;
//       }
//       ArrayList<File> foundImports = new ArrayList<File>();
//       for(int i=0; i < wallets.length; i++){
//           if(wallets[i].isFile()){
//               if(wallets[i].getName().startsWith("UTC") && wallets[i].getName().length() >= 40){
//                   foundImports.add(wallets[i]); // Mist naming
//               } else if(wallets[i].getName().length() >= 40 ){
//                   int position = wallets[i].getName().indexOf(".json");
//                   if(position < 0) continue;
//                   String addr = wallets[i].getName().substring(0, position);
//                   if(addr.length() == 40  && !mapdb.contains("0x"+wallets[i].getName())) {
//                       foundImports.add(wallets[i]); // Exported with Lunary
//                   }
//               }
//           }
//       }
//       if(foundImports.size() == 0) {
//           Dialogs.noImportWalletsFound(c);
//           return;
//       }
//       Dialogs.importWallets(c, foundImports);
//    }

//   public void setWalletForExport(String wallet){
//       walletToExport = wallet;
//   }
//
//   public boolean exportWallet(Activity c) {
//       return exportWallet(c,  false);
//   }

//    public void importWallets(Context c, ArrayList<File> toImport) throws Exception {
//        for(int i=0; i < toImport.size(); i++){
//
//            String address = stripWalletName(toImport.get(i).getName());
//            if(address.length() == 40) {
//                copyFile(toImport.get(i), new File(c.getFilesDir(), address));
//                toImport.get(i).delete();
//                WalletStorage.getInstance(c).add(new FullWallet("0x" + address, address), c);
//                AddressNameConverter.getInstance(c).put("0x" + address, "Wallet " + ("0x" + address).substring(0, 6), c);
//
//                Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                Uri fileContentUri = Uri.fromFile(toImport.get(i)); // With 'permFile' being the File object
//                mediaScannerIntent.setData(fileContentUri);
//                c.sendBroadcast(mediaScannerIntent); // With 'this' being the context, e.g. the activity
//
//            }
//        }
//    }

    public static String stripWalletName(String s){
        if(s.lastIndexOf("--") > 0)
            s = s.substring(s.lastIndexOf("--")+2);
        if(s.endsWith(".json"))
            s = s.substring(0, s.indexOf(".json"));
        return s;
    }

//   private boolean exportWallet(Activity c, boolean already){
//       if(walletToExport == null) return false;
//       if(walletToExport.startsWith("0x"))
//           walletToExport = walletToExport.substring(2);
//
//       if(ExternalStorageHandler.hasPermission(c)) {
//           File folder = new File(Environment.getExternalStorageDirectory(), "Lunary");
//           if(!folder.exists()) folder.mkdirs();
//
//           File storeFile = new File(folder, walletToExport+".json");
//           try {
//               copyFile(new File(c.getFilesDir(), walletToExport), storeFile);
//           } catch (IOException e) {
//               return false;
//           }
//
//           // fix, otherwise won't show up via USB
//           Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//           Uri fileContentUri = Uri.fromFile(storeFile); // With 'permFile' being the File object
//           mediaScannerIntent.setData(fileContentUri);
//           c.sendBroadcast(mediaScannerIntent); // With 'this' being the context, e.g. the activity
//           return true;
//       } else if(!already ){
//           ExternalStorageHandler.askForPermission(c);
//           return exportWallet(c, true);
//       } else {
//           return false;
//       }
//   }


//    private void copyFile(File src, File dst) throws IOException {
//        FileChannel inChannel = new FileInputStream(src).getChannel();
//        FileChannel outChannel = new FileOutputStream(dst).getChannel();
//        try {
//            inChannel.transferTo(0, inChannel.size(), outChannel);
//        }
//        finally {
//            if (inChannel != null)
//                inChannel.close();
//            if (outChannel != null)
//                outChannel.close();
//        }
//    }

    /**
     * 获取私钥
     * @param password 钱包密码
     * @param wallet 钱包地址 公钥
     * */
   public Credentials getFullWallet(Context context, String password, String wallet) throws IOException, JSONException, CipherException {
       if(wallet.startsWith("0x"))
           wallet = wallet.substring(2, wallet.length());
       return WalletUtils.loadCredentials(password, new File(context.getFilesDir(), SDCardCtrl.WALLERPATH + "/" + wallet));
   }
    /**
     * 获取KeyStore
     * @param password 钱包密码
     * @param wallet 钱包地址 公钥
     * */
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


//    public synchronized void save(Context context){
//        FileOutputStream fout;
//        try {
//            fout = new FileOutputStream(new File(context.getFilesDir(), "wallets.dat"));
//            ObjectOutputStream oos = new ObjectOutputStream(fout);
//            oos.writeObject(mapdb);
//            oos.close();
//            fout.close();
//        } catch (Exception e) {
//        }
//    }
//
    /**读自己json中的*/
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
