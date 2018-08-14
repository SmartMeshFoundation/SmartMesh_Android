package com.lingtuan.firefly.setting;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.setting.contract.SubmitFeedBackContract;
import com.lingtuan.firefly.setting.presenter.SubmitFeedBackPresenterImpl;
import com.lingtuan.firefly.util.LoadingDialog;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created on 2017/9/14.
 * SubmitFeedBackUI
 */

public class SubmitFeedBackUI extends BaseActivity implements SubmitFeedBackContract.View{

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

    private SubmitFeedBackContract.Presenter mPresenter;

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
        new SubmitFeedBackPresenterImpl(this);
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
                    if (TextUtils.isEmpty(message)){
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
     * feed back method
     * 意见反馈方法
     * */
    private void feedBackMethod(String message,int type){
        String email = emailEt.getText().toString();
        if (TextUtils.isEmpty(email)){
            showToast(getString(R.string.email_empty));
            return;
        }
        if (!mPresenter.isEmail(email)){
            showToast(getString(R.string.email_is_error));
            return;
        }
        mPresenter.feedBack(message,email,type);
    }

    @Override
    public void setPresenter(SubmitFeedBackContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void feedBackStart() {
        LoadingDialog.show(SubmitFeedBackUI.this,"");
    }

    @Override
    public void feedBackSuccess() {
        LoadingDialog.close();
        showToast(getString(R.string.submit_success));
        finish();
    }

    @Override
    public void feedBackError() {
        LoadingDialog.close();
        showToast(getString(R.string.submit_faile));
        finish();
    }
}
