package com.lingtuan.firefly.setting;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.language.LanguageType;
import com.lingtuan.firefly.language.MultiLanguageUtil;
import com.lingtuan.firefly.ui.MainFragmentUI;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created on 2017/9/13.
 * Language selection
 * {@link SettingUI}
 */

public class LanguageSelectionUI extends BaseActivity {

    //Chinese English
    @BindView(R.id.chineseBody)
    RelativeLayout chineseBody;
    @BindView(R.id.englishBody)
    RelativeLayout englishBody;
    @BindView(R.id.chineseImg)
    ImageView chineseImg;
    @BindView(R.id.englishImg)
    ImageView englishImg;

    @Override
    protected void setContentView() {
        setContentView(R.layout.language_selection_layout);
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.setting_language_selection));
        int language = MultiLanguageUtil.getInstance().getLanguageType();
        if (LanguageType.LANGUAGE_CHINESE_SIMPLIFIED == language){//Chinese
            chineseImg.setVisibility(View.VISIBLE);
            englishImg.setVisibility(View.GONE);
        }else{//en
            chineseImg.setVisibility(View.GONE);
            englishImg.setVisibility(View.VISIBLE);
        }
    }

    @OnClick({R.id.chineseBody,R.id.englishBody})
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.chineseBody:
                switchLanguage(LanguageType.LANGUAGE_CHINESE_SIMPLIFIED);
                break;
            case R.id.englishBody:
                switchLanguage(LanguageType.LANGUAGE_EN);
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    /**
     * Refresh the language
     * @param languageType  0 default  1 english  2 chinese
     */
    public void switchLanguage(int languageType) {
        int language = MultiLanguageUtil.getInstance().getLanguageType();
        if (language == languageType){
            return;
        }
        if (languageType == LanguageType.LANGUAGE_CHINESE_SIMPLIFIED){//chinese
            chineseImg.setVisibility(View.VISIBLE);
            englishImg.setVisibility(View.GONE);
        }else {//en
            chineseImg.setVisibility(View.GONE);
            englishImg.setVisibility(View.VISIBLE);
        }
        MultiLanguageUtil.getInstance().updateLanguage(languageType,true);
        Intent intent = new Intent(LanguageSelectionUI.this, MainFragmentUI.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}
