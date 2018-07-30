package com.lingtuan.firefly.chat;

import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.adapter.SendFileExpandListViewAdapter;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.chat.vo.FileChildVo;
import com.lingtuan.firefly.util.FileSizeUtils;
import com.lingtuan.firefly.util.SDFileUtils;
import com.lingtuan.firefly.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Chat select file interface
 * Created on 2016/6/6.
 */
public class SendFileUI extends BaseActivity implements View.OnClickListener {

    private String[] textFiles = {".txt", ".doc", ".docx", ".pdf", ".wps", ".pptx", ".ppt", ".xlsx", ".xls", ".html", ".htm",};  //Document file
    private String[] videoFiles = {".mp4", ".dat", ".vob", ".avi", ".rm", ".asf", ".wmv", ".mov"};     //Video file
    private String[] musicFiles = {".cda", ".wav", ".mp3", ".wma", ".ra", ".rma", ".MID", ".MIDI", ".RMI", ".XMI", ".mid", ".OGG", ".vqf", ".mod", ".ape", ".aiff", ".aac", ".au"};  //Audio file
    private String[] otherFiles = {".apk", ".zip", ".rar", ".7z"};  //Other documents
    private String[][] fileTypes = {textFiles, videoFiles, musicFiles, otherFiles};
    private ExpandableListView expandListView;
    private ArrayList<FileChildVo> selectedList;
    private SendFileExpandListViewAdapter mAdapter;
    private TextView sendFileNum, hasSelectedSize, app_btn_right;
    private boolean isDestory;
    private List<List<FileChildVo>> totalList = new ArrayList<>();
    private List<FileChildVo> childList;
    private long totalSelectedSize = 0;
    /**
     * Can beat the TextView
     */
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    totalSelectedFile();
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    protected void setContentView() {
        setContentView(R.layout.chat_send_file_layout);
        if (selectedList == null || selectedList.isEmpty()) {  //  If the user had never choose a, the initialization
            selectedList = new ArrayList<>();
        }
    }

    @Override
    protected void findViewById() {
        app_btn_right = (TextView) findViewById(R.id.app_btn_right);
        expandListView = (ExpandableListView) findViewById(R.id.expand_listview);
        sendFileNum = (TextView) findViewById(R.id.send_file_num);
        hasSelectedSize = (TextView) findViewById(R.id.has_selected_size);
    }

    @Override
    protected void setListener() {
        app_btn_right.setOnClickListener(this);
        sendFileNum.setOnClickListener(this);
        expandListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                totalList.get(groupPosition).get(childPosition);
                FileChildVo vo = totalList.get(groupPosition).get(childPosition);
                if (vo.isSelectState()) {       //By the selected into the selected state
                    vo.setSelectState(false);
                    selectedList.remove(vo);
                    if (selectedList.size() <= 0) {
                        sendFileNum.setEnabled(false);
                    }
                    sendFileNum.setText(getString(R.string.chat_send_file, selectedList.size() + ""));
                    v.findViewById(R.id.img_file_select_state).setBackgroundResource(R.drawable.checkbox_unselected);

                } else {
                    long limitSize=(long)5 * 1024 * 1024 * 1024;
                    if (vo.getReallySize() + totalSelectedSize > limitSize) {
                        showToast(getString(R.string.chat_file_all_size));
                    } else {
                        selectedList.add(vo);
                        vo.setSelectState(true);
                        sendFileNum.setEnabled(true);
                        sendFileNum.setText(getString(R.string.chat_send_file, selectedList.size() + ""));
                        v.findViewById(R.id.img_file_select_state).setBackgroundResource(R.drawable.checkbox_selected);

                    }
                }
                app_btn_right.setVisibility(selectedList.size() > 0 ? View.VISIBLE : View.GONE);
                totalSelectedFile();
                return false;
            }
        });
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.chat_select_chatting_file));
        app_btn_right.setVisibility(View.GONE);
        app_btn_right.setText(getString(R.string.cancel));
        sendFileNum.setText(getString(R.string.chat_send_file, "0"));
        if (SDFileUtils.SDCardIsOk()) {
            File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath());// Get SD card path
            File[] files = path.listFiles();// read

            for (int i = 0; i < fileTypes.length; i++) {
                childList = new ArrayList<>();
                totalList.add(childList);
            }

            mAdapter = new SendFileExpandListViewAdapter(SendFileUI.this, totalList);
            expandListView.setAdapter(mAdapter);
            new ScanSDCardThread(files).start();
            mAdapter.notifyDataSetChanged();
        }
    }


    /**
     * The thread of traversing the SD card for all files
     */
    class ScanSDCardThread extends Thread {
        private File[] files;

        public ScanSDCardThread(File[] files) {
            this.files = files;
        }

        @Override
        public void run() {
            super.run();
            try {
                getFileName(files);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Message m = mHandler.obtainMessage();
            m.what = 0;
            mHandler.sendMessage(m);
        }

    }

    /**
     * The scanning file, file information
     *
     * @param files
     */
    private void getFileName(File[] files) throws IOException {
        if (isDestory) {
            return;
        }
        if (files != null) {        // To determine whether a directory is empty, or you will quote null pointer
            for (File file : files) {
                if (file.isDirectory()) {       //If the folder, skip
                    if (file.listFiles() == null) {
                        continue;
                    }
                    getFileName(file.listFiles());
                } else {                        //If is the file, the file type
                    String fileName = file.getName().toLowerCase();
                    for (int k = 0; k < fileTypes.length; k++) {
                        for (int i = 0; i < fileTypes[k].length; i++) {
                            FileChildVo childVo = new FileChildVo();
                            if (fileName.endsWith(fileTypes[k][i])) {
                                String filepath = file.getAbsolutePath();
                                long time = file.lastModified() / 1000;
                                String strTime = Utils.formatTime(time);
                                long reallySize = 0;//The file size, the unit is byte
                                String fileSize = "";//The file size, the unit
                                try {
                                    fileSize = FileSizeUtils.getAutoFileOrFilesSize(file);
                                    reallySize = FileSizeUtils.getFileSize(file);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                childVo.setFileType(fileTypes[k][i]);
                                childVo.setName(fileName);
                                childVo.setFilePath(filepath);
                                childVo.setReallySize(reallySize);
                                childVo.setSize(fileSize);
                                childVo.setTime(strTime);
                                childVo.setCreateTime(System.currentTimeMillis() / 1000);
                                childVo.setFid(UUID.randomUUID().toString());
                                totalList.get(k).add(childVo);
                            }
                        }
                    }

                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.send_file_num:    //Send a file
                Intent i = new Intent();
                i.putExtra("list", selectedList);
                setResult(RESULT_OK, i);
                Utils.exitActivityAndBackAnim(this, true);
                break;
            case R.id.app_btn_right:        //Cancel button
                if (selectedList != null && selectedList.size() > 0) {
                    for (int k = 0; k < selectedList.size(); k++) {
                        FileChildVo vo = selectedList.get(k);
                        if (vo.isSelectState()) {
                            vo.setSelectState(false);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                    selectedList.clear();
                    app_btn_right.setVisibility(View.GONE);
                    sendFileNum.setEnabled(false);
                    sendFileNum.setText(getString(R.string.chat_send_file, selectedList.size() + ""));
                    totalSelectedFile();
                }
                break;
        }
    }

    /**
     * Calculate a total file size
     */
    private void totalSelectedFile() {
        totalSelectedSize = 0;
        if (selectedList != null) {
            for (int i = 0; i < selectedList.size(); i++) {
                totalSelectedSize += selectedList.get(i).getReallySize();
            }
            String size = FileSizeUtils.formatFileSize(totalSelectedSize);
            hasSelectedSize.setText(getString(R.string.chat_has_selected_size, size));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestory = true;
        if (mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }
}
