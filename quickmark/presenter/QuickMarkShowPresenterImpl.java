package com.lingtuan.firefly.quickmark.presenter;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.quickmark.contract.QuickMarkShowContract;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.util.WalletStorage;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import java.util.ArrayList;
import java.util.Hashtable;

public class QuickMarkShowPresenterImpl implements QuickMarkShowContract.Presenter{

    private QuickMarkShowContract.View mView;

    public QuickMarkShowPresenterImpl(QuickMarkShowContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public Bitmap createQRCodeBitmap(String content, int widthAndHeight) {
        Hashtable<EncodeHintType, Object> qrParam = new Hashtable<>();
        qrParam.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        qrParam.put(EncodeHintType.CHARACTER_SET, "utf-8");
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight, qrParam);
            int w = bitMatrix.getWidth();
            int h = bitMatrix.getHeight();
            int[] data = new int[w * h];

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (bitMatrix.get(x, y)){
                        data[y * w + x] = 0xff000000;
                    }else{
                        data[y * w + x] = -1;
                    }
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(data, 0, w, 0, 0, w, h);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public StorableWallet getWallet() {
        StorableWallet storableWallet = null;
        int index = -1;//Which one is selected
        ArrayList<StorableWallet> storableWallets = WalletStorage.getInstance(NextApplication.mContext).get();
        for (int i = 0 ; i < storableWallets.size(); i++){
            if (storableWallets.get(i).isSelect() ){
                WalletStorage.getInstance(NextApplication.mContext).updateWalletToList(NextApplication.mContext,storableWallets.get(i).getPublicKey(),false);
                index = i;
                storableWallet = storableWallets.get(i);
                if (TextUtils.isEmpty(storableWallet.getWalletImageId()) || !storableWallet.getWalletImageId().startsWith("icon_static_")){
                    String imgId = Utils.getWalletImg(NextApplication.mContext,i);
                    storableWallet.setWalletImageId(imgId);
                }
                break;
            }
        }
        if (index == -1 && storableWallets.size() > 0){
            storableWallet = storableWallets.get(0);
            if (TextUtils.isEmpty(storableWallet.getWalletImageId()) || !storableWallet.getWalletImageId().startsWith("icon_static_")){
                String imgId = Utils.getWalletImg(NextApplication.mContext,0);
                storableWallet.setWalletImageId(imgId);
            }
        }
        return storableWallet;
    }
}
