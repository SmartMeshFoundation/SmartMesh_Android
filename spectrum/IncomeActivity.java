package com.lingtuan.firefly.spectrum;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.custom.Entry;
import com.lingtuan.firefly.custom.LineChartView;
import com.lingtuan.firefly.entity.IncomeByDayBean;
import com.lingtuan.firefly.utils.DateUtil;
import com.lingtuan.firefly.utils.DeviceUtil;
import com.lingtuan.firefly.utils.LoadingDialog;

import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.ResponseBody;

public class IncomeActivity extends BaseActivity<IncomeContract.Presenter> implements IncomeContract.View {

    @BindView(R.id.lineChart)
    LineChartView chart;
    @BindView(R.id.totalIncome)
    TextView totalIncome;
    @BindView(R.id.totalIncomeTitle)
    TextView totalIncomeTitle;
    @BindView(R.id.incomeState)
    TextView incomeState;
    @BindView(R.id.totalIncomeType)
    TextView totalIncomeType;
    @BindView(R.id.spectrumFee)
    RelativeLayout spectrumFee;

    private String type = "SMT";
    private String totalRevenue = "0";

    @Override
    public int getLayoutId() {
        DeviceUtil.setStatusBar(this, 0, R.color.checkbox_select_color_3F7AE0);
        getPassData();
        return R.layout.activity_iocome;
    }

    private void getPassData() {
        type = getIntent().getStringExtra("type");
        if (TextUtils.isEmpty(type)){
            type = "SMT";
        }
        totalRevenue = getIntent().getStringExtra("totalRevenue");
    }

    @Override
    public IncomePresenter createPresenter() {
        return new IncomePresenter(this);
    }


    @OnClick({R.id.spectrumFee,R.id.titleRight})
    public void onClick(View v){
        switch (v.getId()){
            case R.id.spectrumFee:
                Intent intentFee = new Intent(this,SpectrumTransactionFee.class);
                intentFee.putExtra("type",type);
                intentFee.putExtra("totalRevenue",totalRevenue);
                startActivity(intentFee);
                break;
            case R.id.titleRight:
                Intent intent = new Intent(this,IncomeDetailsActivity.class);
                intent.putExtra("type",type);
                intent.putExtra("totalRevenue",totalRevenue);
                startActivity(intent);
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    protected void initData() {
        mTitle.setText(getString(R.string.income_title,type));
        totalIncomeType.setText(type);
        totalIncomeTitle.setText(getString(R.string.income_total_income,type));
        mTitleRight.setVisibility(View.VISIBLE);
        mTitleRight.setText(getResources().getString(R.string.income_title_right));
        totalIncome.setText(totalRevenue);
        incomeState.setText(getString(R.string.income_state));
        if (TextUtils.equals(getString(R.string.smt),type)){
            spectrumFee.setVisibility(View.VISIBLE);
        }else{
            spectrumFee.setVisibility(View.GONE);
        }
        LoadingDialog.show(this);
        mPresenter.loadData(type);
    }

    @Override
    public void onResult(Object result, String message) {
        LoadingDialog.close();
        try {
            ResponseBody body = (ResponseBody) result;
            String jsonString = body.string();
            JSONObject obj = new JSONObject(jsonString);
            int code = obj.optInt("code");
            if (code == 0){
                String meshString = obj.optString("data");
                IncomeByDayBean incomeByDayBean = new Gson().fromJson(meshString,IncomeByDayBean.class);
                drawChart(incomeByDayBean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Throwable throwable, String message) {
        LoadingDialog.close();
    }

    private void drawChart(IncomeByDayBean incomeVo ){
        try {
            ArrayList<Entry> entries = new ArrayList<>();
            int size = incomeVo.getBy_day().size();
            for (int index = 0; index < size; index++) {
                String date = DateUtil.getMMdd(incomeVo.getBy_day().get(index).getDate() * 1000);
                float revenue = incomeVo.getBy_day().get(index).getRevenue();
                entries.add(new Entry((int) revenue, date));
            }
            chart.setStepSpace(45);
            chart.setShowTable(true);
            chart.setBezierLine(true);
            chart.setData(entries);
            chart.playAnim();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
