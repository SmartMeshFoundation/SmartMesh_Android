package com.lingtuan.firefly.util;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.custom.DiscussGroupImageView;
import com.lingtuan.firefly.vo.UserBaseVo;

import java.util.List;

public class MyViewDialogFragment extends DialogFragment implements OnClickListener {

    private View dialogView = null;



    private String titleText, contentText, cancleText, okText;

    private TextView titleTv, contentTv;
    private View titleBody = null;

    private String editHint, editValue;
    boolean isEditValueEmpty = false;

    private int dialogType = -1;
    private boolean multiLine = false;
    private boolean isVerifyPwd = false;

    private String username,sendTo,linkTitle;
    private List<UserBaseVo> members;

    private OkCallback okCallback = null;
    private CancelCallback cancelCallback = null;
    private EditCallback editCallback = null;
    private SingleCallback singleCallback = null;
    private EditOkCallback editOkCallback = null;
    private LeaveMsgCallBack leaveMsgCallBack = null;//Share the message callback


    public MyViewDialogFragment() {

    }

    public static final int DIALOG_INPUT_PWD = 0;//Password input box
    public static final int DIALOG_SINGLE_BUTTON = DIALOG_INPUT_PWD +1;//Single button
    public static final int DIALOG_TEXT_ENTRY = DIALOG_SINGLE_BUTTON + 1;//Single input box
    public static final int DIALOG_FORWOED = DIALOG_TEXT_ENTRY + 1;//Forwarding, sharing a message
    public static final int DIALOG_SINGLE_EDIT = DIALOG_FORWOED + 1;//A single input box (can only enter Numbers and letters)

    private Button cancelBtn, okBtn;


    static MyViewDialogFragment newInstance(int num) {
        MyViewDialogFragment f = new MyViewDialogFragment();
        Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }


    public MyViewDialogFragment(int dialogType,EditCallback editCallback) {
        this.dialogType = dialogType;
        this.editCallback = editCallback;
    }

    public MyViewDialogFragment(int dialogType,SingleCallback singleCallback) {
        this.dialogType = dialogType;
        this.singleCallback = singleCallback;
    }

    public MyViewDialogFragment(int dialogType, String titleText, String editHint, String editValue) {
        this.dialogType = dialogType;
        this.titleText = titleText;
        this.editHint = editHint;
        this.editValue = editValue;
    }

    public MyViewDialogFragment(int dialogType,String title,String username,String linkTitle,List<UserBaseVo> members){
        this.dialogType = dialogType;
        this.sendTo  = title;
        this.username = username;
        this.members = members;
        this.linkTitle = linkTitle;
    }



    public interface OkCallback {
        void okBtn();
    }


    public interface CancelCallback {
        void cancelBtn();
    }

    public interface EditCallback {
        void getEditText(String editText);
    }

    public interface EditOkCallback {
        void okBtn(String edittext);
    }

    public interface SingleCallback {
        void getSingleCallBack();
    }

    public interface LeaveMsgCallBack {
        void leaveMsgMethod(String content);
    }

    public void setLeaveMsgCallBack(LeaveMsgCallBack leaveMsgCallBack){
        this.leaveMsgCallBack = leaveMsgCallBack;
    }

    public void setOkCallback(OkCallback okCallback) {
        this.okCallback = okCallback;
    }



    public void setEditOkCallback(EditOkCallback editOkCallback) {
        this.editOkCallback = editOkCallback;
    }


    public void setTitleAndContentText(String title, String content) {
        this.titleText = title;
        this.contentText = content;
    }

    public void setSubmitNamContentText(String submitName) {
        this.okText = submitName;
    }

    //是否是验证密码
    public void setVerifyPwd(boolean isVerifyPwd) {
        this.isVerifyPwd = isVerifyPwd;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle params = getArguments();
        if (params != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        switch (dialogType) {
            default:
                dialogView = inflater.inflate(R.layout.my_dialog_fragment, container);
                titleTv = (TextView) dialogView.findViewById(R.id.titleTv);
                contentTv = (TextView) dialogView.findViewById(R.id.contentTv);
                okBtn = (Button) dialogView.findViewById(R.id.okBtn);
                okBtn.setOnClickListener(this);
                if (!TextUtils.isEmpty(contentText)) {
                    contentTv.setText(contentText);
                }
                if (!TextUtils.isEmpty(cancleText)) {
                    cancelBtn.setText(cancleText);
                }
                if (!TextUtils.isEmpty(okText)) {
                    okBtn.setText(okText);
                }
                break;
            case DIALOG_INPUT_PWD: // Password input box
                dialogView = inflater.inflate(R.layout.dialog_input_pwd, container);
                titleTv = (TextView) dialogView.findViewById(R.id.titleTv);
                final EditText editText = (EditText) dialogView.findViewById(R.id.dialogPwd);
                okBtn = (Button) dialogView.findViewById(R.id.okBtn);
                okBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismiss();
                        if (isAdded() && getActivity() != null && editCallback != null){
                            editCallback.getEditText(editText.getText().toString());
                        }
                    }
                });
                break;
            case DIALOG_SINGLE_BUTTON: // Single button
                dialogView = inflater.inflate(R.layout.dialog_single_button, container);
                okBtn = (Button) dialogView.findViewById(R.id.okBtn);
                okBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismiss();
                        if (isAdded() && getActivity() != null && singleCallback != null){
                            singleCallback.getSingleCallBack();
                        }
                    }
                });
                break;
            case DIALOG_FORWOED:
                dialogView = inflater.inflate(R.layout.dialog_format_y_n, container);
                TextView titleTv1 = (TextView) dialogView.findViewById(R.id.dialog_format_title);
                TextView body = (TextView) dialogView.findViewById(R.id.dialog_format_body);
                okBtn = (Button) dialogView.findViewById(R.id.okBtn);
                DiscussGroupImageView avatar = (DiscussGroupImageView) dialogView.findViewById(R.id.avatar);
                ImageView avatarSingle = (ImageView) dialogView.findViewById(R.id.avatar_single);
                final LinearLayout leaveMsgBody = (LinearLayout) dialogView.findViewById(R.id.leaveMsgBody);
                final  EditText leaveMsgEt = (EditText) dialogView.findViewById(R.id.leaveMsgEt);
                final  TextView leaveMsgLink = (TextView) dialogView.findViewById(R.id.leaveMsgLink);
                if (!TextUtils.isEmpty(linkTitle)){
                    leaveMsgBody.setVisibility(View.VISIBLE);
                    leaveMsgLink.setText(linkTitle);
                }else{
                    leaveMsgBody.setVisibility(View.GONE);
                }
                titleTv1.setText(sendTo);
                body.setSingleLine(true);
                body.setText(username);
                if (members != null) {
                    if (members.size() == 1) {
                        avatarSingle.setVisibility(View.VISIBLE);
                        NextApplication.displayCircleImage(avatarSingle, members.get(0).getThumb());
                    } else {
                        avatar.setVisibility(View.VISIBLE);
                        avatar.setMember(members);
                    }
                }

                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        if (leaveMsgCallBack != null){
                            if (leaveMsgEt != null){
                                leaveMsgCallBack.leaveMsgMethod(leaveMsgEt.getText().toString());
                            }else{
                                leaveMsgCallBack.leaveMsgMethod(leaveMsgEt.getText().toString());
                            }
                        }
                    }
                });
                break;
            case DIALOG_TEXT_ENTRY: // Single input box
                dialogView = inflater.inflate(R.layout.alert_dialog_text_entry, container);
                titleBody = dialogView.findViewById(R.id.titleBody);
                titleTv = (TextView) dialogView.findViewById(R.id.titleTv);
                final EditText editText1 = (EditText) dialogView.findViewById(R.id.editText);
                final EditText editText2 = (EditText) dialogView.findViewById(R.id.editText2);
                okBtn = (Button) dialogView.findViewById(R.id.okBtn);
                if (multiLine) { // Multiline text box
                    editText1.setVisibility(View.GONE);
                    editText2.setVisibility(View.VISIBLE);
                    if (!TextUtils.isEmpty(editHint)) {
                        editText2.setHint(editHint);
                    }

                    if (!TextUtils.isEmpty(editValue)) {
                        editText2.setText(editValue);
                    }
                } else { // Single-line text box
                    editText1.setVisibility(View.VISIBLE);
                    editText2.setVisibility(View.GONE);
                    if (!TextUtils.isEmpty(editHint)) {
                        editText1.setHint(editHint);
                    }

                    if (!TextUtils.isEmpty(editValue)) {
                        editText1.setText(editValue);
                    }
                }
                okBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isAdded() && getActivity() != null && editOkCallback != null) {
                            if (multiLine) {
                                if (TextUtils.isEmpty(editText2.getText().toString().trim())) {
                                    Toast.makeText(getActivity(), getActivity().getString(R.string.hint_content_cannot_empty), Toast.LENGTH_SHORT).show();
                                    return;
                                } else {
                                    dismiss();
                                    editOkCallback.okBtn(editText2.getText().toString());
                                }
                            } else {
                                if (TextUtils.isEmpty(editText1.getText().toString().trim())) {
                                    if (isEditValueEmpty) {
                                        dismiss();
                                        editOkCallback.okBtn(multiLine ? editText2.getText().toString() : editText1.getText().toString());
                                    } else {
                                        Toast.makeText(getActivity(), getActivity().getString(R.string.hint_content_cannot_empty), Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                } else {
                                    dismiss();
                                    editOkCallback.okBtn(multiLine ? editText2.getText().toString() : editText1.getText().toString());
                                }
                            }
                        }
                    }
                });
                break;
            case DIALOG_SINGLE_EDIT: // Single input box
                dialogView = inflater.inflate(R.layout.alert_dialog_edit_single, container);
                titleBody = dialogView.findViewById(R.id.titleBody);
                titleTv = (TextView) dialogView.findViewById(R.id.titleTv);
                final EditText singEdit = (EditText) dialogView.findViewById(R.id.editText);
                okBtn = (Button) dialogView.findViewById(R.id.okBtn);
                singEdit.setVisibility(View.VISIBLE);

                if (!TextUtils.isEmpty(editHint)) {
                    singEdit.setHint(editHint);
                }

                if (!TextUtils.isEmpty(editValue)) {
                    singEdit.setText(editValue);
                }

                okBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isAdded() && getActivity() != null && editOkCallback != null) {
                            String singContent = singEdit.getText().toString().trim();
                            if (TextUtils.isEmpty(singContent)) {
                                Toast.makeText(getActivity(), getActivity().getString(R.string.hint_content_cannot_empty), Toast.LENGTH_SHORT).show();
                                dismiss();
                            } else {
                                dismiss();
                                if (isVerifyPwd){
                                    editOkCallback.okBtn(singEdit.getText().toString());
                                }else{
                                    if (singContent.length() < 6 || singContent.length() > 20){
                                        Toast.makeText(getActivity(), getActivity().getString(R.string.mid_length_warning), Toast.LENGTH_SHORT).show();
                                    }else{
                                        editOkCallback.okBtn(singEdit.getText().toString());
                                    }
                                }

                            }
                        }
                    }
                });
                break;

        }


        /**Here are some common controls, don't pay any attention */
        cancelBtn = (Button) dialogView.findViewById(R.id.cancelBtn);

        if (cancelBtn != null){
            cancelBtn.setOnClickListener(this);
        }

        if (titleTv != null){
            if (!TextUtils.isEmpty(titleText)){
                titleTv.setText(titleText);
            }else{
                titleTv.setVisibility(View.GONE);
            }
        }

        return dialogView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancelBtn:
                dismiss();
                if (isAdded() && getActivity() != null && cancelCallback != null) {
                    cancelCallback.cancelBtn();
                }
                break;

            case R.id.okBtn:
                dismiss();
                if (isAdded() && getActivity() != null && okCallback != null) {
                    okCallback.okBtn();
                }
                break;

            default:
                break;
        }
    }
}
