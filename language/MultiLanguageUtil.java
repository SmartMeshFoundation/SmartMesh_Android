package com.lingtuan.firefly.language;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.util.MySharedPrefs;

import java.util.Locale;

/**
 * Multi-language switching helper class
 * 多语言切换的帮助类
 */
public class MultiLanguageUtil {

    private static MultiLanguageUtil instance;

    public static MultiLanguageUtil getInstance() {
        if (instance == null) {
            synchronized (MultiLanguageUtil.class) {
                if (instance == null) {
                    instance = new MultiLanguageUtil();
                }
            }
        }
        return instance;
    }

    /**
     * set language
     * 设置语言
     */
    private void setConfiguration() {
        Locale targetLocale = getLanguageLocale();
        Configuration configuration = NextApplication.mContext.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(targetLocale);
        } else {
            configuration.locale = targetLocale;
        }
        Resources resources = NextApplication.mContext.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        resources.updateConfiguration(configuration, dm);
    }

    /**
     * If it is not English, Simplified Chinese, return to English by default.
     * 如果不是英文、简体中文 默认返回英文
     */
    private Locale getLanguageLocale() {
        int languageType = MySharedPrefs.readInt(NextApplication.mContext,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_SAVE_LANGUAGE);
        if (languageType == LanguageType.LANGUAGE_FOLLOW_SYSTEM) {
            Locale sysLocale= getSysLocale();
            if (TextUtils.equals(sysLocale.getLanguage(),"zh")){
                return Locale.SIMPLIFIED_CHINESE;
            }else{
                return Locale.ENGLISH;
            }
        } else if (languageType == LanguageType.LANGUAGE_EN) {
            return Locale.ENGLISH;
        } else if (languageType == LanguageType.LANGUAGE_CHINESE_SIMPLIFIED) {
            return Locale.SIMPLIFIED_CHINESE;
        }
        return Locale.ENGLISH;
    }

    /**
     * get locale
     * */
    private Locale getSysLocale() {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }
        return locale;
    }

    /**
     * update language
     * 更新语言
     */
    public void updateLanguage(int languageType,boolean writeSharedPrefs) {
        if (writeSharedPrefs){
            MySharedPrefs.writeInt(NextApplication.mContext,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_SAVE_LANGUAGE,languageType);
        }
        MultiLanguageUtil.getInstance().setConfiguration();
    }

    /**
     * Get the language type saved by the user
     * 获取用户保存的语言类型
     */
    public int getLanguageType() {
        int languageType = MySharedPrefs.readInt(NextApplication.mContext,MySharedPrefs.FILE_APPLICATION,MySharedPrefs.KEY_SAVE_LANGUAGE);
        if (languageType == LanguageType.LANGUAGE_FOLLOW_SYSTEM) {
            Locale sysLocale= getSysLocale();
            if (TextUtils.equals(sysLocale.getLanguage(),"zh")){
                return LanguageType.LANGUAGE_CHINESE_SIMPLIFIED;
            }else{
                return LanguageType.LANGUAGE_EN;
            }
        } else if (languageType == LanguageType.LANGUAGE_CHINESE_SIMPLIFIED) {
            return LanguageType.LANGUAGE_CHINESE_SIMPLIFIED;
        }
        return languageType;
    }

    /**
     * attach base context
     * 8.0
     * */
    public static Context attachBaseContext(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return createConfigurationResources(context);
        } else {
            MultiLanguageUtil.getInstance().setConfiguration();
            return context;
        }
    }

    /**
     * create configuration context
     * */
    @TargetApi(Build.VERSION_CODES.N)
    private static Context createConfigurationResources(Context context) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale=getInstance().getLanguageLocale();
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }
}
