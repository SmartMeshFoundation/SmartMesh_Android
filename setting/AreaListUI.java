package com.lingtuan.firefly.setting;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;


public class AreaListUI extends BaseActivity implements OnItemClickListener{


	private ListView listview;
	private CountryAdapter adapter;

		
	@Override
	protected void setContentView() {
		setContentView(R.layout.country_layout);
	}

	@Override
	protected void findViewById() {
		listview = (ListView) findViewById(R.id.listview);
	}

	@Override
	protected void setListener() {
		listview.setOnItemClickListener(this);
	}

	@Override
	protected void initData() {
		setTitle(getResources().getString(R.string.select_country_title));
		int resourceid = R.array.list_0_0;
		String city = getIntent().getStringExtra("city");
		if(city.equals("安徽")){
			resourceid = R.array.list_0_0;
		}else if(city.equals("北京")){
			resourceid = R.array.list_0_1;
		}
		else if(city.equals("重庆"))
		{
			resourceid = R.array.list_0_2;
		}
		else if(city.equals("福建"))
		{
			resourceid = R.array.list_0_3;
		}
		else if(city.equals("广东"))
		{
			resourceid = R.array.list_0_4;
		}
		else if(city.equals("甘肃"))
		{
			resourceid = R.array.list_0_5;
		}
		else if(city.equals("广西"))
		{
			resourceid = R.array.list_0_6;
		}
		else if(city.equals("贵州"))
		{
			resourceid = R.array.list_0_7;
		}
		else if(city.equals("湖北"))
		{
			resourceid = R.array.list_0_8;
		}
		else if(city.equals("河北"))
		{
			resourceid = R.array.list_0_9;
		}
		else if(city.equals("黑龙江"))
		{
			resourceid = R.array.list_0_10;
		}
		else if(city.equals("湖南"))
		{
			resourceid = R.array.list_0_11;
		}
		else if(city.equals("河南"))
		{
			resourceid = R.array.list_0_12;
		}
		else if(city.equals("海南"))
		{
			resourceid = R.array.list_0_13;
		}
		else if(city.equals("吉林"))
		{
			resourceid = R.array.list_0_14;
		}
		else if(city.equals("江苏"))
		{
			resourceid = R.array.list_0_15;
		}
		else if(city.equals("江西"))
		{
			resourceid = R.array.list_0_16;
		}
		else if(city.equals("辽宁"))
		{
			resourceid = R.array.list_0_17;
		}
		else if(city.equals("内蒙古"))
		{
			resourceid = R.array.list_0_18;
		}
		else if(city.equals("宁夏"))
		{
			resourceid = R.array.list_0_19;
		}
		else if(city.equals("青海"))
		{
			resourceid = R.array.list_0_20;
		}
		else if(city.equals("四川"))
		{
			resourceid = R.array.list_0_21;
		}
		else if(city.equals("山东"))
		{
			resourceid = R.array.list_0_22;
		}
		else if(city.equals("上海"))
		{
			resourceid = R.array.list_0_23;
		}
		else if(city.equals("陕西"))
		{
			resourceid = R.array.list_0_24;
		}
		else if(city.equals("山西"))
		{
			resourceid = R.array.list_0_25;
		}
		else if(city.equals("天津"))
		{
			resourceid = R.array.list_0_26;
		}
		else if(city.equals("西藏"))
		{
			resourceid = R.array.list_0_27;
		}
		else if(city.equals("新疆"))
		{
			resourceid = R.array.list_0_28;
		}
		else if(city.equals("云南"))
		{
			resourceid = R.array.list_0_29;
		}
		else if(city.equals("浙江"))
		{
			resourceid = R.array.list_0_30;
		}
		else if(city.equals("台湾") || city.equals("中国台湾"))
		{
			resourceid = R.array.list_0_31;
		}
		else if(city.equals("香港") || city.equals("中国香港"))
		{
			resourceid = R.array.list_0_32;
		}
		adapter = new CountryAdapter(this, getResources().getStringArray(resourceid));
	    listview.setAdapter(adapter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		Intent i = new Intent();
		i.putExtra("area", adapter.getItem(position));
        setResult(RESULT_OK, i);
		finish();

	}
}
