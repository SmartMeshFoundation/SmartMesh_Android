package com.lingtuan.firefly.custom;

import android.support.annotation.ArrayRes;
import android.support.annotation.StringRes;

/**
 * Change the language
 */
public interface LanguageChangableView {
    String ANDROIDXML = "http://schemas.android.com/apk/res/android";

    // the setText can't be rewritten, you need to add the following three essential method, if your app doesn't need to modify a multilingual textview value (just write XML die is enough), it does not need to implement them
    void setTextById (@StringRes int id);//Manually textId
    void setTextWithString(String text);//Manually remove textId, or reload the language will be reset
    void setTextByArrayAndIndex (@ArrayRes int arrId, @StringRes int arrIndex);//Manually by TextArray language setting

    void reLoadLanguage();//Modify the language mainly the method called
}
