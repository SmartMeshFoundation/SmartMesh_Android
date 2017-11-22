package com.lingtuan.firefly.custom;

import android.content.Context;
import android.support.annotation.ArrayRes;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.widget.TextView;

import com.lingtuan.firefly.util.Utils;


/**
 * Created on 2015/7/26.
 */
public class LanguageTextView extends TextView implements LanguageChangableView {
    private int textId ;//Text id
    private int hintId ;//Hint of id
    private int arrResId,arrResIndex;

    //  app:fontStyle="light" medium
    public LanguageTextView(Context context) {
        super(context);
        init(context, null);
    }

    public LanguageTextView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init(paramContext, paramAttributeSet);
    }

    public LanguageTextView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        init(paramContext, paramAttributeSet);
    }

    /**
     * The initialization for XML resource id
     * @param context
     * @param attributeSet
     */
    private void init (Context context, AttributeSet attributeSet) {
        if (attributeSet!=null) {
            String textValue = attributeSet.getAttributeValue(ANDROIDXML, "text");
            if (!(textValue==null || textValue.length()<2)) {
                textId = Utils.string2int(textValue.substring(1,textValue.length()));
            }

            String hintValue = attributeSet.getAttributeValue(ANDROIDXML, "hint");
            if (!(hintValue==null || hintValue.length()<2)) {
                hintId = Utils.string2int(hintValue.substring(1,hintValue.length()));
            }
        }
    }

    @Override
    public void setTextById (@StringRes int strId) {
        this.textId = strId;
        setText(strId);
    }

    @Override
    public void setTextWithString(String text) {
        this.textId = 0;
        setText(text);
    }
    @Override
    public void setTextByArrayAndIndex (@ArrayRes int arrId, @StringRes int arrIndex) {
        arrResId = arrId;
        arrResIndex = arrIndex;
        String[] strs = getContext().getResources().getStringArray(arrId);
        setText(strs[arrIndex]);
    }

    @Override
    public void reLoadLanguage () {
        try {
            if (textId>0) {
                setText(textId);
            } else if (arrResId>0) {
                String[] strs = getContext().getResources().getStringArray(arrResId);
                setText(strs[arrResIndex]);
            }

            if (hintId>0) {
                setHint(hintId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
