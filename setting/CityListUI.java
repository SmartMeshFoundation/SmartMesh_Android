package com.lingtuan.firefly.setting;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.util.Utils;


public class CityListUI extends BaseActivity implements OnItemClickListener{


	private ListView listview;
	private CountryAdapter adapter;

	private int selectposition;
		
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
		String country = getIntent().getStringExtra("country");

		int resourceid = R.array.list_0;

		if(country.equals("中国")){
			resourceid = R.array.list_0;
		}
		adapter = new CountryAdapter(this, getResources().getStringArray(resourceid));
	    listview.setAdapter(adapter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		selectposition = position;
		Intent intentArea = new Intent(new Intent(this, AreaListUI.class));
		intentArea.putExtra("city", adapter.getItem(position));
		startActivityForResult(intentArea,100);
		Utils.openNewActivityAnim(this, false);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode ==100 && resultCode ==RESULT_OK){
			String area = data.getStringExtra("area");
			Intent i = new Intent();
			i.putExtra("area", area);
			i.putExtra("city", adapter.getItem(selectposition));
	        setResult(RESULT_OK, i);
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
