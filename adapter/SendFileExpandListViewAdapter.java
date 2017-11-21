package com.lingtuan.firefly.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.chat.vo.FileChildVo;
import com.lingtuan.firefly.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2016/6/6.
 */
public class SendFileExpandListViewAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private List<List<FileChildVo>> mList = new ArrayList<>();

    public SendFileExpandListViewAdapter(Context context, List<List<FileChildVo>> list) {
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public int getGroupCount() {
        if (mList != null && mList.size() > 0) {
            return mList.size();
        } else {
            return 0;
        }
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (mList != null && mList.size() > 0) {
            if (mList.get(groupPosition) != null && mList.get(groupPosition).size() > 0) {
                return mList.get(groupPosition).size();
            }
        }

        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {

        return mList == null ? null : mList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {

        if (mList != null && mList.size() > 0) {
            if (mList.get(groupPosition) != null && mList.get(groupPosition).size() > 0) {
                return mList.get(groupPosition).get(childPosition);
            }
        }
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupHolder groupHolder = null;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.send_file_expand_listview_fathrer_item, null);
            groupHolder = new GroupHolder();
            groupHolder.txtFileStyle = (TextView) convertView.findViewById(R.id.txt_file_style);
            groupHolder.imgExpandState = (ImageView) convertView.findViewById(R.id.img_expand_state);
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) convertView.getTag();
        }

        if (groupPosition == 0) {
            groupHolder.txtFileStyle.setText(mContext.getString(R.string.chat_document));
        } else if (groupPosition == 1) {
            groupHolder.txtFileStyle.setText(mContext.getString(R.string.chat_video));
        } else if (groupPosition == 2) {
            groupHolder.txtFileStyle.setText(mContext.getString(R.string.chat_audio));
        } else {
            groupHolder.txtFileStyle.setText(mContext.getString(R.string.chat_other));
        }

        if (isExpanded == true) {//A state of
            groupHolder.imgExpandState.setImageResource(R.drawable.contact_open_tips);
        } else {                 //Pack up state
            groupHolder.imgExpandState.setImageResource(R.drawable.contact_close_tips);
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildHolder itemHolder = null;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.send_file_expand_listview_child_item, null);
            itemHolder = new ChildHolder();
            itemHolder.txtFileName = (TextView) convertView.findViewById(R.id.txt_file_name);
            itemHolder.txtFileSize = (TextView) convertView.findViewById(R.id.txt_file_size);
            itemHolder.txtFileTime = (TextView) convertView.findViewById(R.id.txt_file_time);
            itemHolder.imgFileLogo = (ImageView) convertView.findViewById(R.id.img_file_logo);
            itemHolder.imgSelectState = (ImageView) convertView.findViewById(R.id.img_file_select_state);
            convertView.setTag(itemHolder);
        } else {
            itemHolder = (ChildHolder) convertView.getTag();
        }
        if (mList != null && mList.size() > 0) {
            List<FileChildVo> childList = mList.get(groupPosition);
            FileChildVo childVo = childList.get(childPosition);
            if (childVo != null) {
                itemHolder.txtFileName.setText(childVo.getName());
                itemHolder.txtFileSize.setText(childVo.getSize());
                itemHolder.txtFileTime.setText(childVo.getTime());
                if (childVo.isSelectState()) {
                    itemHolder.imgSelectState.setBackgroundResource(R.drawable.checkbox_selected);
                } else {
                    itemHolder.imgSelectState.setBackgroundResource(R.drawable.checkbox_unselected);
                }
                itemHolder.imgFileLogo.setTag(childVo.getFilePath());
                Utils.showFileIcon(mContext,childVo.getFileType(),childVo.getFilePath(), itemHolder.imgFileLogo);
            }
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public class GroupHolder {
        public TextView txtFileStyle;
        public ImageView imgExpandState;
    }

    public class ChildHolder {
        public ImageView imgFileLogo, imgSelectState;
        public TextView txtFileName, txtFileSize, txtFileTime;
    }
}
