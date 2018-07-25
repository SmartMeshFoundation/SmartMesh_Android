package com.lingtuan.firefly.setting;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created on 2017/9/14.
 * SubmitFeedBackUI
 */

public class SubmitFeedBackUI extends BaseActivity {

    @BindView(R.id.reportLine)
    View reportLine;
    @BindView(R.id.suggestLine)
    View suggestLine;
    @BindView(R.id.submitType)
    TextView submitType; //submitType
    @BindView(R.id.problemEt)
    EditText problemEt; //problem content suggest content
    @BindView(R.id.suggestEt)
    EditText suggestEt;
    @BindView(R.id.emailEt)
    EditText emailEt;//email address

    private int type;//0 report problem suggestImprovement

    @Override
    protected void setContentView() {
        setContentView(R.layout.submit_feedback_layout);
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.submit_feedback));
    }

    @OnClick({R.id.reportProblem,R.id.suggestImprovement,R.id.submit})
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.reportProblem:
                type = 0;
                reportLine.setVisibility(View.VISIBLE);
                problemEt.setVisibility(View.VISIBLE);
                suggestLine.setVisibility(View.INVISIBLE);
                suggestEt.setVisibility(View.GONE);
                submitType.setText(getString(R.string.problem_info));
                break;
            case R.id.suggestImprovement:
                type = 1;
                reportLine.setVisibility(View.INVISIBLE);
                problemEt.setVisibility(View.GONE);
                suggestLine.setVisibility(View.VISIBLE);
                suggestEt.setVisibility(View.VISIBLE);
                submitType.setText(getString(R.string.advice_info));
                break;
            case R.id.submit:
                if (type == 0 && problemEt.getVisibility() == View.VISIBLE){
                    String message = problemEt.getText().toString();
                    if (TextUtils.isEmpty(message)){
                        showToast(getString(R.string.problem_info_empty));
                    }else{
                        feedBackMethod(message,0);
                    }
                }else{
                    String message = suggestEt.getText().toString();
                    if (TextUtils.isEmpty(suggestEt.getText().toString())){
                        showToast(getString(R.string.advice_info_empty));
                    }else{
                        feedBackMethod(message,1);
                    }
                }
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    /**
     * 意见反馈方法
     * */
    private void feedBackMethod(String message,int type){
        String email = emailEt.getText().toString();
        if (TextUtils.isEmpty(email)){
            showToast(getString(R.string.email_empty));
            return;
        }

        if (!isEmail(email)){
            showToast(getString(R.string.email_is_error));
            return;
        }
        NetRequestImpl.getInstance().feedBack(message, email, type, new RequestListener() {
            @Override
            public void start() {
                LoadingDialog.show(SubmitFeedBackUI.this,"");
            }

            @Override
            public void success(JSONObject response) {
                LoadingDialog.close();
                showToast(getString(R.string.submit_success));
                finish();
            }

            @Override
            public void error(int errorCode, String errorMsg) {
                LoadingDialog.close();
                showToast(getString(R.string.submit_faile));
                finish();
            }
        });

    }

    //邮箱验证
    public static boolean isEmail(String strEmail) {
        String strPattern = "^[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";
        if (TextUtils.isEmpty(strPattern)) {
            return false;
        } else {
            return strEmail.matches(strPattern);
        }
    }
}
