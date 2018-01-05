package com.lingtuan.firefly.imagescan;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.contact.ContactSelectedUI;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.quickmark.GroupQuickMarkUI;
import com.lingtuan.firefly.quickmark.QuickMarkUI;
import com.lingtuan.firefly.util.BitmapUtils;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;
import com.lingtuan.firefly.vo.ChatMsg;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * A larger preview public class
 */
public class ScanLargePic extends BaseActivity implements ScanPicAdapter.FinishCallBack, ScanPicAdapter.ShowTitleCallBack, ScanPicAdapter.OnLongClickCallBack {
    private ScanPicAdapter adapter;
    private MyViewPager vpPhotos;
    private int position;
    private ArrayList<String> picList = null;
    private ArrayList<String> picId = null;
    private String uid = null;  //Receive the uid
    private ImageView deleteTV = null;
    private RelativeLayout sendToFriend,saveToSD, identifyQr;
    private RelativeLayout titleBar = null;

    /**
     * Is it in your own photo album pictures
     */
    private boolean isOurSelf = false;

    private int currentSelected = 1;

    private Dialog mProgressDialog;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    final ScanImageVo vo = (ScanImageVo) msg.obj;
                    if (vo == null || vo.dialog == null || vo.currentIndex != vpPhotos.getCurrentItem()) {
                        return;
                    }
                    identifyQr.setVisibility(View.VISIBLE);
                    identifyQr.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            vo.dialog.dismiss();
                            Intent intent = new Intent(ScanLargePic.this, QuickMarkUI.class);
                            intent.putExtra("result", vo.scanResult);
                            startActivity(intent);
                            Utils.openNewActivityAnim(ScanLargePic.this, false);
                        }
                    });
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_viewpaper);
    }

    @Override
    protected void findViewById() {
        vpPhotos = (MyViewPager) findViewById(R.id.vpPhotos);
        titleBar = (RelativeLayout) findViewById(R.id.app_title_rela);
        deleteTV = (ImageView) findViewById(R.id.detail_set);
    }

    @Override
    protected void setListener() {
        deleteTV.setOnClickListener(new MyDeleteListener());
        vpPhotos.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                currentSelected = arg0 + 1;
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                if (titleBar.getVisibility() == View.VISIBLE) {
                    TranslateAnimation gone = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -1);
                    gone.setDuration(300);
                    gone.setFillAfter(true);
                    titleBar.setAnimation(gone);
                    titleBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void initData() {
        Intent i = getIntent();
        if (i != null) {
            picList = i.getStringArrayListExtra("picList");
            picId = i.getStringArrayListExtra("picId");
            position = i.getIntExtra("position", 0);
            uid = i.getStringExtra("uid");
            isOurSelf = i.getBooleanExtra("isOurSelf", false);
        }

        if (!TextUtils.isEmpty(uid)) {
            if (NextApplication.myInfo.getLocalId().equals(uid)) {
                deleteTV.setVisibility(View.VISIBLE);
            }
        }

        if (picList == null) {
            return;
        }

        if (picList.size() > 0) {
            if (adapter == null) {
                adapter = new ScanPicAdapter(picList, this, isOurSelf);
            }
            vpPhotos.setAdapter(adapter);
            adapter.setFinishCallback(this);
            adapter.setShowTitleCallBack(this);
            adapter.setOnLongClickCallBack(this);
            vpPhotos.setCurrentItem(Integer.valueOf(position));
        }
    }

    @Override
    public void finishCallBack() {
        Utils.exitActivityAndBackAnim(this, true);
    }

    int currentPos = 0; // Want to delete the item

    class MyDeleteListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            currentPos = vpPhotos.getCurrentItem();
            if (picId == null && picId.isEmpty()) {
                Utils.exitActivityAndBackAnim(ScanLargePic.this, true);
                return;
            }

            NetRequestImpl.getInstance().editAlbum(picId.get(currentPos), new RequestListener() {
                @Override
                public void start() {
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    mProgressDialog = LoadingDialog.showDialog(ScanLargePic.this, null, null);
                    mProgressDialog.setCancelable(false);
                }

                @Override
                public void success(JSONObject response) {
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    showToast(getString(R.string.delete_success));
                    String deleteId = picId.remove(currentPos);
                    /**Below to update the local JSON*/
                    try {
                        Intent i = new Intent(Constants.BROADCAST_UPDATE_USER);
                        i.putExtra("deleteId", deleteId);
                        picList.remove(currentPos);
                        if (currentSelected > picList.size()) {
                            currentSelected--;
                        }
                        setTitle(currentSelected + "/" + adapter.getCount());
                        adapter.resetSource(picList);
                        LocalBroadcastManager.getInstance(ScanLargePic.this).sendBroadcast(i);

                        if (currentPos == 0 && picList.size() == 0) { // If you remove the last picture, then close the preview page, the page to add new photos
                            Utils.exitActivityAndBackAnim(ScanLargePic.this, true);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void error(int errorCode, String errorMsg) {
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    showToast(errorMsg);
                }
            });
        }
    }

    @Override
    public void showTitleCallBack(final String count,boolean isShowTitle) {
        setTitle(currentSelected + "/" + count);
        if (isShowTitle) {
            TranslateAnimation visible = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -1, Animation.RELATIVE_TO_SELF, 0);
            visible.setDuration(200);
            visible.setFillAfter(true);
            titleBar.setAnimation(visible);
            titleBar.setVisibility(View.VISIBLE);
            deleteTV.setVisibility(View.VISIBLE);
            deleteTV.setImageResource(R.drawable.delete);
        } else {
            TranslateAnimation gone = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -1);
            gone.setDuration(200);
            gone.setFillAfter(true);
            titleBar.setAnimation(gone);
            titleBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLongClickCallback(final String oldPath) {
        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.large_pic_diaog, null);
        saveToSD = (RelativeLayout) view.findViewById(R.id.save_to_phone_rela);
        identifyQr = (RelativeLayout) view.findViewById(R.id.zxing_rela);
        sendToFriend = (RelativeLayout) view.findViewById(R.id.send_to_friend);
        identifyQr.setVisibility(View.GONE);

        AlertDialog.Builder builder = new AlertDialog.Builder(ScanLargePic.this);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);//Set other area for Dialog click screen disappeared
        saveToSD.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String qrPath = BitmapUtils.saveBitmapFile(getApplicationContext(), oldPath);
                if (!TextUtils.isEmpty(qrPath)) {
                    showToast(getString(R.string.photo_save_dir,qrPath));
                    dialog.dismiss();
                }
            }
        });

        sendToFriend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ChatMsg> list = new ArrayList<>();
                ChatMsg msg = new ChatMsg();
                msg.setType(1);
                msg.setContent(oldPath);
                msg.setLocalUrl(oldPath);
                msg.parseUserBaseVo(NextApplication.myInfo);
                list.add(msg);
                Intent intent = new Intent(ScanLargePic.this, ContactSelectedUI.class);
                intent.putExtra("msglist", list);
                startActivity(intent);
                Utils.openNewActivityAnim(ScanLargePic.this, false);
                dialog.dismiss();
            }
        });
        /*If open thread scanning images is qr code*/
        new Thread(new Runnable() {
            @Override
            public void run() {
                Result result = scanningImage(oldPath);
                if (result != null) {
                    Message m = mHandler.obtainMessage();
                    ScanImageVo vo = new ScanImageVo();
                    vo.dialog = dialog;
                    vo.oldPath = oldPath;
                    vo.scanResult = result.toString();
                    vo.currentIndex = vpPhotos.getCurrentItem();
                    m.what = 0;
                    m.obj = vo;
                    mHandler.sendMessage(m);

                }
            }
        }).start();
    }

    public Result scanningImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        Hashtable<DecodeHintType, Object> hints = new Hashtable<>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF8");
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.QR_CODE);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap scanBitmap = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false;
        int sampleSize = (int) (options.outHeight / (float) 512);
        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        scanBitmap = BitmapFactory.decodeFile(path, options);
        int width = scanBitmap.getWidth();
        int height = scanBitmap.getHeight();
        int[] pixels = new int[width * height];
        scanBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        RGBLuminanceSource source = new RGBLuminanceSource(width,height,pixels);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            return reader.decode(bitmap1, hints);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
        return null;
    }
}
