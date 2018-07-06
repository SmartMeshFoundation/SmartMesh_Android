package com.lingtuan.firefly.setting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.chat.vo.GalleryVo;
import com.lingtuan.firefly.custom.CharAvatarView;
import com.lingtuan.firefly.imagescan.CropActivity;
import com.lingtuan.firefly.imagescan.ScanLargePic;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.service.LoadDataService;
import com.lingtuan.firefly.util.BitmapUtils;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyDialogFragment;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.SDCardCtrl;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created on 2017/10/11.
 * The personal data
 */

public class MyProfileUI extends BaseActivity {

    private static final int SELECT_COUNTRY = 100;

    private CharAvatarView userImg;//Head portrait
    private EditText userName;//The user name
    private TextView addGender;//gender
    private TextView addRegion;//address
    private EditText addSign;//signature
    private TextView btnRight;//Save

    /**
     * Edit image related
     */
    private Uri cameraUri;

    /**
     * The Uri of the cut after the save image
     */
    private Uri photoUri;

    private String imgPath;

    private String gender;

    private UserInfoSettingReceiver infoSettingReceiver;


    @Override
    protected void setContentView() {
        setContentView(R.layout.my_profile_layout);
    }

    @Override
    protected void findViewById() {
        userImg = (CharAvatarView) findViewById(R.id.userImg);
        userName = (EditText) findViewById(R.id.userName);
        addGender = (TextView) findViewById(R.id.addGender);
        addRegion = (TextView) findViewById(R.id.addRegion);
        addSign = (EditText) findViewById(R.id.addSign);
        btnRight = (TextView) findViewById(R.id.app_btn_right);
    }

    @Override
    protected void setListener() {
        userImg.setOnClickListener(this);
        btnRight.setOnClickListener(this);
        addGender.setOnClickListener(this);
        addRegion.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.info_my_profile));
        btnRight.setVisibility(View.VISIBLE);
        btnRight.setText(getString(R.string.save));
        if (NextApplication.myInfo != null){
            userImg.setText(NextApplication.myInfo.getUsername(),userImg,NextApplication.myInfo.getThumb());
            userName.setText(NextApplication.myInfo.getUsername());
            addGender.setText(TextUtils.equals("1",NextApplication.myInfo.getGender()) ? getString(R.string.man) : getString(R.string.woman));
            addRegion.setText(NextApplication.myInfo.getAddress());
            addSign.setText(NextApplication.myInfo.getSightml());
            if (!TextUtils.isEmpty(NextApplication.myInfo.getUserName())){
                userName.setSelection(NextApplication.myInfo.getUserName().length());
            }
        }

        infoSettingReceiver = new UserInfoSettingReceiver();

        IntentFilter filter = new IntentFilter(LoadDataService.ACTION_FILE_UPLOAD_IMAGE);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(infoSettingReceiver, filter);

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.app_btn_right:
                int nameLenght = userName.getText().toString().trim().length();
                if (nameLenght > 20 || nameLenght <= 0){
                    showToast(getString(R.string.account_name_warning));
                    return;
                }
                updateInfoMethod();
                break;
            case R.id.userImg:
                scanOrEditAvatar();
                break;
            case R.id.addGender:
                addGenderMethod();
                break;
            case R.id.addRegion:
                startActivityForResult(new Intent(MyProfileUI.this,CountryListUI.class),SELECT_COUNTRY);
                Utils.openNewActivityAnim(MyProfileUI.this,false);
                break;
        }
    }

    /**
     * update gender method
     * */
    private void addGenderMethod() {
        MyDialogFragment mdf = new MyDialogFragment(MyDialogFragment.DIALOG_LIST, R.array.sex_list);
        mdf.setItemClickCallback(new MyDialogFragment.ItemClickCallback() {
            @Override
            public void itemClickCallback(int which) {
                if (which == 0) { // See a larger head
                    gender = "1";
                    addGender.setText(getString(R.string.man));
                } else if (which == 1) { // Photo modified head
                    gender = "2";
                    addGender.setText(getString(R.string.woman));
                }
            }
        });
        mdf.show(getSupportFragmentManager(), "mdf");
    }

    /**
     * Modify the personal information
     * */
    private void updateInfoMethod() {
        LoadingDialog.show(MyProfileUI.this,getString(R.string.info_edit_dialog));
        final String username = userName.getText().toString().trim();
        final String sightml = addSign.getText().toString();
        final String birthcity = addRegion.getText().toString();
        if (imgPath != null){
            Bundle bundle = new Bundle();
            bundle.putSerializable("imgPath", imgPath);
            bundle.putString("username", username);
            bundle.putString("sightml", sightml);
            bundle.putString("gender", gender);
            bundle.putString("birthcity", birthcity);
            Utils.intentServiceAction(MyProfileUI.this, LoadDataService.ACTION_FILE_UPLOAD_IMAGE, bundle);
        }else{
            try {
                NetRequestImpl.getInstance().editUserInfo(null, username, sightml, gender, birthcity, new RequestListener() {
                    @Override
                    public void start() {

                    }

                    @Override
                    public void success(JSONObject response) {
                        LoadingDialog.close();
                        NextApplication.myInfo.updateJsonUserInfo(username, sightml, gender, birthcity,MyProfileUI.this);
                        NextApplication.myInfo.setUsername(username);
                        NextApplication.myInfo.setSightml(sightml);
                        NextApplication.myInfo.setGender(gender);
                        NextApplication.myInfo.setAddress(birthcity);
                        finish();
                    }

                    @Override
                    public void error(int errorCode, String errorMsg) {
                        LoadingDialog.close();
                        showToast(errorMsg);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(infoSettingReceiver);
    }

    /**
     * View the big picture or edit avatar
     */
    private void scanOrEditAvatar() {
        MyDialogFragment mdf = new MyDialogFragment(MyDialogFragment.DIALOG_LIST, R.array.scan_edit_avatar);
        mdf.setItemClickCallback(new MyDialogFragment.ItemClickCallback() {
            @Override
            public void itemClickCallback(int which) {
                if (which == 0) { // See a larger head
                    if (NextApplication.myInfo != null) {
                        Intent intent = new Intent(MyProfileUI.this, ScanLargePic.class);
                        ArrayList<String> picList = new ArrayList<>();
                        picList.add(NextApplication.myInfo.getPic());
                        intent.putStringArrayListExtra("picList", picList);
                        startActivity(intent);
                        Utils.openNewActivityAnim(MyProfileUI.this, false);
                    }
                } else if (which == 1) { // Photo modified head
                    Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //Call system camera
                    File file = new File(SDCardCtrl.getUploadPath(), BitmapUtils.getPhotoFileName());
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    cameraUri = Uri.fromFile(file);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Uri photoUri = FileProvider.getUriForFile(MyProfileUI.this,"com.lingtuan.firefly.fileProvider",file);
                        camera.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    }else{
                        camera.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
                    }
                    startActivityForResult(camera, Constants.CAMERA_WITH_DATA);
                } else { // From the album revision
                    try {
                        Intent photo = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(photo, Constants.PHOTO_PICKED_WITH_DATA);
                    } catch (ActivityNotFoundException e) {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        startActivityForResult(intent, Constants.PHOTO_PICKED_WITH_DATA);
                    }
                }

            }
        });
        mdf.show(getSupportFragmentManager(), "mdf");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case SELECT_COUNTRY: // Camera returned by the program, called again image editing program to cut images
                String address = data.getStringExtra("address");
                addRegion.setText(address);
                break;

            case Constants.PHOTO_PICKED_WITH_DATA://Photo album
                if (data != null) {
                    //Cutting to jump to page
                    try {
                        Uri selectedImage = data.getData();
                        String[] filePathColumns = {MediaStore.Images.Media.DATA};
                        Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
                        if (c != null) {
                            if (c.moveToNext()) {
                                c.moveToFirst();
                                int columnIndex = c.getColumnIndex(filePathColumns[0]);
                                String picturePath = c.getString(columnIndex);
                                Intent intent = new Intent(MyProfileUI.this, CropActivity.class).putExtra("photoUri", picturePath);
                                startActivityForResult(intent, Constants.REQUEST_CODE_PHOTO_URL);
                            }
                            c.close();
                        } else {
                            String picturePath = selectedImage.toString().replace("file://", "");
                            Intent intent = new Intent(MyProfileUI.this, CropActivity.class).putExtra("photoUri", picturePath);
                            startActivityForResult(intent, Constants.REQUEST_CODE_PHOTO_URL);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showToast(getString(R.string.select_img_error));
                    }
                }
                break;

            case Constants.CAMERA_WITH_DATA: // Camera returned by the program, called again image editing program to cut images
                if (cameraUri == null) {
                    showToast(getString(R.string.select_camera_error));
                    return;
                }
                Intent intent = new Intent(MyProfileUI.this, CropActivity.class).putExtra("photoUri", cameraUri.getPath());
                startActivityForResult(intent, Constants.REQUEST_CODE_PHOTO_URL);
                break;

            case Constants.REQUEST_CODE_PHOTO_URL: // Cut out pictures return url
                if (data != null) {
                    try {
                        String photoUrl = data.getStringExtra("photourl");
                        photoUri = Uri.parse("file://" + photoUrl);
                        if (photoUri != null && photoUri.toString().startsWith("file://")) {
                            imgPath = photoUri.toString().replace("file://", "");
                        }
                        userImg.setText(NextApplication.myInfo.getUsername(),userImg,photoUri.toString());
                    } catch (Exception e) {
                        showToast(getString(R.string.info_edit_picture_cut_failure));
                        e.printStackTrace();
                    } catch (Error e) {
                        showToast(getString(R.string.info_edit_picture_cut_failure));
                        e.printStackTrace();
                    }
                }
                break;

        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    JSONObject result = (JSONObject) msg.obj;
                    showToast(result.optString("msg"));
                    try {
                        userImg.setText(NextApplication.myInfo.getUsername(),userImg,result.optString("avatarPath"));
                    } catch (Exception e) {
                        showToast(getString(R.string.info_edit_picture_cut_failure));
                        e.printStackTrace();
                    } catch (Error e) {
                        showToast(getString(R.string.info_edit_picture_cut_failure));
                        e.printStackTrace();
                    }
                    break;

                case 2:
                    showToast(getString(R.string.error));
                    break;

            }
        }
    };

    class UserInfoSettingReceiver extends BroadcastReceiver {

        @Override
        public synchronized void onReceive(final Context context, Intent intent) {
            if (intent != null) {
                 if (TextUtils.equals(LoadDataService.ACTION_FILE_UPLOAD_IMAGE, intent.getAction())) { // Upload the image correction
                      LoadingDialog.close();
                      try {
                          Bundle bundle = intent.getExtras();
                          String result = bundle.getString("result");
                          JSONObject obj = new JSONObject(result);
                          Message message = mHandler.obtainMessage();
                          try {
                              obj.putOpt("avatarPath", obj.optString("thumb"));
                          } catch (JSONException e) {
                              e.printStackTrace();
                          }
                          message.obj = obj;
                          if (result != null) {
                              message.what = 1;
                              mHandler.sendMessage(message);
                              if (obj.optInt("errcode") == 0 ) {
                                  NextApplication.myInfo.updateJsonAvatar(obj.optString("thumb"), obj.optString("pic"), MyProfileUI.this);
                                  NextApplication.myInfo.setPic(obj.optString("pic"));
                                  NextApplication.myInfo.setThumb(obj.optString("thumb"));
                              }
                          } else {
                              message.what = 2;
                              mHandler.sendMessage(message);
                          }
                          userImg.setText(NextApplication.myInfo.getUsername(),userImg,obj.optString("thumb"));
                          finish();
                    } catch (Exception e2) {
                        showToast(getString(R.string.info_upload_picture_failure));
                    }
                }
            }
        }
    }

}
