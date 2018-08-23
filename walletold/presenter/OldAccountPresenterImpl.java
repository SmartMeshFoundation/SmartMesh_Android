package com.lingtuan.firefly.walletold.presenter;

import android.content.Context;
import android.text.TextUtils;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.network.NetRequestUtils;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;
import com.lingtuan.firefly.walletold.contract.OldAccountContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class OldAccountPresenterImpl implements OldAccountContract.Presenter{

    private OldAccountContract.View mView;

    public OldAccountPresenterImpl(OldAccountContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public StorableWallet getStorableWallet() {
        int index = -1;//Which one is selected
        ArrayList<StorableWallet> storableWallets = WalletStorage.getInstance(NextApplication.mContext).get();
        StorableWallet storableWallet = null;
        for (int i = 0 ; i < storableWallets.size(); i++){
            if (storableWallets.get(i).isSelect() ){
                WalletStorage.getInstance(NextApplication.mContext).updateWalletToList(NextApplication.mContext,storableWallets.get(i).getPublicKey(),false);
                index = i;
                storableWallet = storableWallets.get(i);
                if (TextUtils.isEmpty(storableWallet.getWalletImageId()) || !storableWallet.getWalletImageId().startsWith("icon_static_")) {
                    String imgId = Utils.getWalletImg(NextApplication.mContext,i);
                    storableWallet.setWalletImageId(imgId);
                }
                break;
            }
        }
        if (index == -1 && storableWallets.size() > 0){
            storableWallet = storableWallets.get(0);
            if (TextUtils.isEmpty(storableWallet.getWalletImageId()) || !storableWallet.getWalletImageId().startsWith("icon_static_")) {
                String imgId = Utils.getWalletImg(NextApplication.mContext,0);
                storableWallet.setWalletImageId(imgId);
            }
        }
        return storableWallet;
    }

    @Override
    public void getBalance(Context context, String address,final boolean isShowToast) {
        try {
            NetRequestUtils.getInstance().getBalance(context,address, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    mView.onFailure(isShowToast);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    mView.onResponse(response.body().string());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            mView.onFailure(isShowToast);
        }
    }

    @Override
    public void parseJson(String jsonString) {
        if (TextUtils.isEmpty(jsonString)){
            return;
        }
        try {
            JSONObject object = new JSONObject(jsonString);
            int errcod = object.optInt("errcod");
            if (errcod == 0){
                double ethBalance = object.optJSONObject("data").optDouble("eth");
                double smtBalance = object.optJSONObject("data").optDouble("smt");
                double meshBalance = object.optJSONObject("data").optDouble("mesh");
                String  smtMapping = object.optJSONObject("data").optString("smt_mapping");
                mView.resetData(ethBalance,smtBalance,meshBalance,smtMapping);
            }else{
                if(errcod == -2){
                    long difftime = object.optJSONObject("data").optLong("difftime");
                    long tempTime =  MySharedPrefs.readLong(NextApplication.mContext,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_REQTIME);
                    MySharedPrefs.writeLong(NextApplication.mContext,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_REQTIME,difftime + tempTime);
                }
                mView.resetDataError(object.optString("msg"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {

    }
}
