package com.lingtuan.firefly.spectrum;

import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.gson.Gson;
import com.lingtuan.meshbox.R;
import com.lingtuan.meshbox.base.BaseActivity;
import com.lingtuan.meshbox.entity.IncomeByDayBean;
import com.lingtuan.meshbox.utils.DateUtil;
import com.lingtuan.meshbox.utils.DeviceUtil;
import com.lingtuan.meshbox.utils.LoadingDialog;

import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.ResponseBody;

public class IncomeActivity extends BaseActivity<IncomeContract.Presenter> implements IncomeContract.View {

    @BindView(R.id.lineChart)
    LineChart chart;
    @BindView(R.id.totalIncome)
    TextView totalIncome;
    @BindView(R.id.totalIncomeTitle)
    TextView totalIncomeTitle;

    private String type = "smt";
    private String totalRevenue = "0";

    @Override
    public int getLayoutId() {
        DeviceUtil.setStatusBar(this, 0, R.color.checkbox_select_color_3F7AE0);
        getPassData();
        return R.layout.activity_iocome;
    }

    private void getPassData() {
        type = getIntent().getStringExtra("type");
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
        totalIncomeTitle.setText(getString(R.string.income_total_income,type));
        mTitleRight.setVisibility(View.VISIBLE);
        mTitleRight.setText(getResources().getString(R.string.income_title_right));
        totalIncome.setText(totalRevenue);
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
            Log.e("***********",e.getMessage());
        }
    }

    @Override
    public void onError(Throwable throwable, String message) {
        LoadingDialog.close();
        Log.e("***********",throwable.toString());
    }

    private void drawChart(IncomeByDayBean incomeVo ){
        try {
            LineData lineData = new LineData();
            ArrayList<Entry> entries1 = new ArrayList<>();
            int size = incomeVo.getBy_day().size();
            for (int index = 0; index < size; index++) {
                String date = DateUtil.getMMdd(incomeVo.getBy_day().get(index).getDate() * 1000);
                float floatDate = Float.parseFloat(date);
                float revenue = incomeVo.getBy_day().get(index).getRevenue();
                entries1.add(new Entry(floatDate, revenue));
            }
            String dataSetLabel1 = "";
            LineDataSet lineDataSet1 = new LineDataSet(entries1, dataSetLabel1);
            lineDataSet1.setColor(getResources().getColor(R.color.checkbox_select_color_3F7AE0));//线的颜色
            lineDataSet1.setLineWidth(1.5f);//线的粗细
            lineDataSet1.setCircleColor(getResources().getColor(R.color.checkbox_select_color_3F7AE0));//线上圈的颜色
            lineDataSet1.setCircleRadius(3f);//线上圈的大小
            lineDataSet1.setFillColor(getResources().getColor(R.color.checkbox_select_color_3F7AE0));
            lineDataSet1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            lineDataSet1.setDrawValues(true);
            lineDataSet1.setValueTextSize(10f);
            lineDataSet1.setValueTextColor(getResources().getColor(R.color.checkbox_select_color_3F7AE0));
            lineDataSet1.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineData.addDataSet(lineDataSet1);
            chart.setData(lineData);//将数据集置于控件上

            Description description = new Description();
            description.setText("");
            chart.setDescription(description);
            chart.setDrawGridBackground(false);//如果启用，chart 绘图区后面的背景矩形将绘制
            chart.setGridBackgroundColor(Color.WHITE);//设置网格背景应与绘制的颜色
            chart.setTouchEnabled(false);//设置网格不可点击
            chart.setDrawBorders(false);//启用/禁用绘制图表边框（chart周围的线）
            chart.setBorderColor(getResources().getColor(R.color.checkbox_select_color_3F7AE0));//设置 chart 边框线的颜色
            chart.setBorderWidth(3f);//设置 chart 边界线的宽度，单位 dp
            chart.setMaxVisibleValueCount(8);//设置最大可见绘制的 chart count 的数量
            chart.animateY(3000);
            chart.animateX(2000);

            XAxis xAxis = chart.getXAxis();//获取X轴
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//设置X轴的显示位置为底部
            xAxis.setEnabled(true);//X轴不关闭
            xAxis.setLabelCount(7,true);
            xAxis.setAvoidFirstLastClipping(true);//如果设置为true，则在绘制时会避免“剪掉”在x轴上的图表或屏幕边缘的第一个和最后一个坐标轴标签项
            xAxis.setDrawGridLines(false);//绘制网格线
            xAxis.setAxisLineColor(getResources().getColor(R.color.checkbox_select_color_3F7AE0));

            YAxis yAxisleft = chart.getAxisLeft();//缺省情况下，y轴显示了两条，左边一条和右边一条
            YAxis yAxisright = chart.getAxisRight();
            yAxisleft.setStartAtZero(true);//设置从0开始
            yAxisleft.setAxisLineColor(getResources().getColor(R.color.checkbox_select_color_3F7AE0));
            yAxisleft.setDrawGridLines(false);//设置不显示Y轴横向网格线
            yAxisright.setEnabled(false);//关闭右边的Y轴

            Legend l = chart.getLegend();//设置lengend
            l.setFormSize(10f); // set the size of the legend forms/shapes
            l.setForm(Legend.LegendForm.CIRCLE); // set what type of form/shape should be used
            l.setTextSize(12f);
            l.setEnabled(false);
            l.setTextColor(Color.BLACK);
            l.setFormToTextSpace(5f);
            l.setXEntrySpace(5f); // set the space between the legend entries on the x-axis
            l.setYEntrySpace(5f); // set the space between the legend entries on the y-axis

            chart.invalidate();//重新绘制

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
