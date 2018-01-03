package com.lingtuan.firefly.setting;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.netutil.NetRequestImpl;

import org.json.JSONObject;

/**
 * Created on 2017/9/14.
 * SubmitFeedBackUI
 */

public class SubmitFeedBackUI extends BaseActivity {

    //reportProblem suggestImprovement
    private LinearLayout reportProblem,suggestImprovement;
    private View reportLine,suggestLine;

    //submitType
    private TextView submitType;

    //problem content suggest content
    private EditText problemEt,suggestEt;

    //email address
    private EditText emailEt;

    //submit
    private TextView submit;

    private int type;//0 report problem suggestImprovement

    @Override
    protected void setContentView() {
        setContentView(R.layout.submit_feedback_layout);
    }

    @Override
    protected void findViewById() {
        reportProblem = (LinearLayout) findViewById(R.id.reportProblem);
        suggestImprovement = (LinearLayout) findViewById(R.id.suggestImprovement);
        reportLine = findViewById(R.id.reportLine);
        suggestLine = findViewById(R.id.suggestLine);
        submitType = (TextView) findViewById(R.id.submitType);
        submit = (TextView) findViewById(R.id.submit);
        problemEt = (EditText) findViewById(R.id.problemEt);
        suggestEt = (EditText) findViewById(R.id.suggestEt);
        emailEt = (EditText) findViewById(R.id.emailEt);
    }

    @Override
    protected void setListener() {
        reportProblem.setOnClickListener(this);
        suggestImprovement.setOnClickListener(this);
        submit.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.submit_feedback));
    }

    @Override
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

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0://提交反馈成功
                    LoadingDialog.close();
                    showToast(getString(R.string.submit_success));
                    finish();
                    break;
                case 1://提交反馈失败
                    LoadingDialog.close();
                    showToast(getString(R.string.submit_faile));
                    finish();
                    break;
            }
        }
    };
}
