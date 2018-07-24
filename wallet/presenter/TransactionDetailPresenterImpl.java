package com.lingtuan.firefly.wallet.presenter;

import android.graphics.Bitmap;
import android.os.Message;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.wallet.contract.TransactionDetailContract;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Hashtable;

public class TransactionDetailPresenterImpl implements TransactionDetailContract.Presenter{

    private TransactionDetailContract.View mView;

    public TransactionDetailPresenterImpl(TransactionDetailContract.View view){
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public Bitmap createQRCodeBitmap(String content, int widthAndHeight) {
        if (TextUtils.isEmpty(content)) {
            content = " ";
        }
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
                    if (bitMatrix.get(x, y))
                        data[y * w + x] = 0xff000000;
                    else
                        data[y * w + x] = -1;
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

    /**
     * Get the block number of the transaction hash
     */
    public void getTransactionBlock(String tx) {

        NetRequestImpl.getInstance().getTxBlockNumber(tx, new RequestListener() {
            @Override
            public void start() {

            }

            @Override
            public void success(JSONObject response) {
                JSONArray array = response.optJSONArray("data");
                int lastBlockNumber = response.optInt("blockNumber", 0);
                if (array != null) {
                    JSONObject object = array.optJSONObject(0);
                    int transBlockNumber = object.optInt("txBlockNumber", 0);
                    int state = object.optInt("state", 0);
                    Message message = Message.obtain();
                    if (state == 0 || state == 1) {
                        message.what = 1;
                    } else if (state == 2) {
                        message.what = 2;
                    }
                    mView.success(transBlockNumber,lastBlockNumber,state,message);
                }
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                mView.error(errorCode,errorMsg);
            }
        });
    }
}
