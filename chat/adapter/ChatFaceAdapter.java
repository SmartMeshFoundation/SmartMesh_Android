package com.lingtuan.firefly.chat.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.lingtuan.firefly.R;

import java.io.File;
import java.util.List;

/**
 * Expression adapter
 */
public class ChatFaceAdapter extends BaseAdapter {

    private Integer[] faceIds;
    private String faceNameIds[];
    private Context mContext;
    private int width;
    private int page;
    private boolean isFirstPage;
    /**
     * Whether the Gif grouping
     */
    private boolean isGif = false;


    public ChatFaceAdapter(Integer[] faceIds, String faceNameIds[], Context mContext, int page, boolean isGif) {
        this.faceIds = faceIds;
        this.faceNameIds = faceNameIds;
        this.mContext = mContext;
        this.page = page;
        this.isGif = isGif;
        int rowCount = isGif ? 4 : 6;
        int tempWidth = mContext.getResources().getDisplayMetrics().widthPixels / rowCount;
        width = isGif ? (tempWidth - 50) : (tempWidth - 10);
    }

    @Override
    public int getCount() {
        if(isGif){
            if((page+1) * 8<faceIds.length){
                return 8;
            } else{
                return faceIds.length - 8*page;
            }
        }else{
            return 18;
        }
    }

    @Override
    public Object getItem(int position) {
        return faceIds[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder h;
        if (convertView == null) {
            h = new Holder();
            convertView = View.inflate(mContext, R.layout.chatting_face_item, null);
            h.iv = (ImageView) convertView.findViewById(R.id.item_chatting_face);
            convertView.setTag(h);
        } else {
            h = (Holder) convertView.getTag();
        }
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(width, width);
        convertView.setLayoutParams(params);
        int ids = 0;
        if (position == getCount() - 1 && !isGif) { // When is the last item, and not the GIF image to display for the delete button
            ids = R.drawable.face_delete_normal;
        } else {
            try {
                int pageCount = isGif ? 8 : 17;
                int temp = position + page * pageCount;
                if (temp < faceIds.length) {
                    ids = faceIds[position + page * pageCount];
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        h.iv.setImageResource(ids);
        return convertView;
    }

    static class Holder {
        ImageView iv;
    }

}
