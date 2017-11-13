package com.lingtuan.firefly.setting;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;

import java.util.Locale;

/**
 * Created on 2017/9/13.
 * Language selection
 * {@link SettingUI}
 */

public class LanguageSelectionUI extends BaseActivity {

    //Chinese English
    private RelativeLayout chineseBody,englishBody;
    private ImageView chineseImg,englishImg;

    @Override
    protected void setContentView() {
        setContentView(R.layout.language_selection_layout);
    }

    @Override
    protected void findViewById() {
        chineseBody = (RelativeLayout) findViewById(R.id.chineseBody);
        englishBody = (RelativeLayout) findViewById(R.id.englishBody);
        chineseImg = (ImageView) findViewById(R.id.chineseImg);
        englishImg = (ImageView) findViewById(R.id.englishImg);
    }

    @Override
    protected void setListener() {
        chineseBody.setOnClickListener(this);
        englishBody.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.setting_language_selection));
        String language = MySharedPrefs.readString(LanguageSelectionUI.this,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_LANGUAFE);
        if (TextUtils.isEmpty(language)){
            String defaultLanguage = Locale.getDefault().getLanguage();
            if (TextUtils.equals("zh",defaultLanguage)){
                chineseImg.setVisibility(View.VISIBLE);
                englishImg.setVisibility(View.GONE);
            }else{//en
                chineseImg.setVisibility(View.GONE);
                englishImg.setVisibility(View.VISIBLE);
            }
        }else{
            if (TextUtils.equals(language,"zh")){//Chinese
                chineseImg.setVisibility(View.VISIBLE);
                englishImg.setVisibility(View.GONE);
            }else{//en
                chineseImg.setVisibility(View.GONE);
                englishImg.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.chineseBody:
                switchLanguage("zh");
                break;
            case R.id.englishBody:
                switchLanguage("en");
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    /**
     * Refresh the language
     * @param language  en english  zh chinese
     */
    public void switchLanguage(String language) {
        if (TextUtils.equals(language,"zh")){//chinese
            chineseImg.setVisibility(View.VISIBLE);
            englishImg.setVisibility(View.GONE);
        }else{//en
            chineseImg.setVisibility(View.GONE);
            englishImg.setVisibility(View.VISIBLE);
        }
        MySharedPrefs.write(LanguageSelectionUI.this,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_LANGUAFE,language);
        // Local language setting
        Locale locale = new Locale(language);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(locale);
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);

        setTitle(getString(R.string.setting_language_selection));
        Utils.updateViewLanguage(findViewById(android.R.id.content));
        Utils.sendBroadcastReceiver(LanguageSelectionUI.this,new Intent(Constants.CHANGE_LANGUAGE), false);
    }

}
