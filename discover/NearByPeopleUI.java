package com.lingtuan.firefly.discover;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.adapter.NearByPeopleAdapter;
import com.lingtuan.firefly.custom.LoadMoreListView;
import com.lingtuan.firefly.custom.LoadMoreListView.RefreshListener;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.MyToast;
import com.lingtuan.firefly.util.NetRequestUtils;
import com.lingtuan.firefly.util.NetRequestUtils.RequestListener;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.UserInfoVo;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NearByPeopleUI extends BaseFragmentActivity implements
        OnItemClickListener, RequestListener, RefreshListener, OnRefreshListener {


    private LoadMoreListView nearLv = null;


    private ImageView imgFilter, imgOtherApp;

    private NetRequestUtils mRequestUtils;

    private List<UserInfoVo> source;

    private NearByPeopleAdapter adapter = null;

    private int currentPage = 1;
    private int oldPage = 1;
    private String CURRENT_FILTER = Constants.FILTER_ALL;

    private SwipeRefreshLayout swipeLayout;

    private boolean isLoadingData = false;

    private TextView emptyTextView;
    private RelativeLayout emptyRela;


    @Override
    protected void setContentView() {
        setContentView(R.layout.nearby_people);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void findViewById() {
        nearLv = (LoadMoreListView) findViewById(R.id.refreshListView);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

        emptyRela = (RelativeLayout) findViewById(R.id.empty_like_rela);
        emptyTextView = (TextView) findViewById(R.id.empty_text);

        imgFilter = (ImageView) findViewById(R.id.detail_set);
        imgFilter.setImageResource(R.drawable.icon_filter);
        imgFilter.setVisibility(View.VISIBLE);
        imgOtherApp = (ImageView) findViewById(R.id.app_tab_right);
        imgOtherApp.setImageResource(R.drawable.icon_yun);
        imgOtherApp.setVisibility(View.VISIBLE);


    }

    @Override
    protected void setListener() {
        imgFilter.setOnClickListener(this);
        imgOtherApp.setOnClickListener(this);
        nearLv.setOnItemClickListener(this);
        nearLv.setOnScrollListener(new PauseOnScrollListener(NextApplication.mImageLoader, true, true));
        nearLv.setOnRefreshListener(this);

        swipeLayout.setOnRefreshListener(this);
    }

    @Override
    protected void initData() {
        swipeLayout.setColorScheme(R.color.app_title_bg);
        setTitle(getString(R.string.near_by_people_title));
        String filter = MySharedPrefs.readString(this, MySharedPrefs.FILE_USER, MySharedPrefs.NEARY_PEOPLE_FILTER);
        if (!TextUtils.isEmpty(filter)) {
            // int index=Integer.parseInt(filter);
            //imgFilter.setText(sexFilter[index]);
            CURRENT_FILTER = filter;
        }
        //else{
        //	imgFilter.setText(sexFilter[0]);
        //}
        imgFilter.setVisibility(View.VISIBLE);
        source = new ArrayList<UserInfoVo>();

        String response = Utils.readFromFile("discover-people_nearby.json");

        if (!TextUtils.isEmpty(response)) {
            try {
                JSONObject json = new JSONObject(response);
                JSONArray array = json.optJSONArray("data");
                parseJson(array);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mRequestUtils = NetRequestUtils.getInstance();
        adapter = new NearByPeopleAdapter(this, source);
        nearLv.setAdapter(adapter);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
                loadNearByList(1, CURRENT_FILTER);
            }
        }, 500);
    }

    /**
     * @param filterGender Filter results by: Filter settings by gender 0 All 1. Male 2. Female 3. Friends
     */
    private void loadNearByList(int page, String filterGender) {

        if (isLoadingData) {
            return;
        }

        if (NextApplication.myInfo == null) {
            return;
        }

        isLoadingData = true;
        String location = MySharedPrefs.readString(NextApplication.mContext, MySharedPrefs.FILE_USER, MySharedPrefs.KEY_LOCATION);
        if (TextUtils.isEmpty(location) || !location.contains(",")) {
            MyToast.showToast(this, getResources().getString(R.string.location_notify));
        }
        oldPage = page;
        Map<String, String> params = new HashMap<String, String>();
        params.put("page", page + "");
        params.put("gender", filterGender + "");
        params.put("uid", NextApplication.myInfo.getUid() + "");
        JSONObject jsonRequest = mRequestUtils.getJsonRequest("discover", "people_nearby", params);
        mRequestUtils.requestJsonObject(jsonRequest, this);

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.detail_set:  
                filter();
                break;
            case R.id.app_tab_right:  
                Intent intent = new Intent(this, CloudAppListUI.class);
                intent.putExtra("comeFrom",1);
                startActivity(intent);
                Utils.openNewActivityAnim(this, false);
                break;
        }
    }

    /**
     * Filter button click event handling
     */
    private void filter() {
        MyDialogFragment mdf = new MyDialogFragment(MyDialogFragment.DIALOG_LIST, null, R.array.filter_sex_array);
        mdf.setItemClickCallback(new ItemClickCallback() {

            @Override
            public void itemClickCallback(int which) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.setRefreshing(true);
                        loadNearByList(1, CURRENT_FILTER);
                    }
                }, 500);

                if (which == 0) {
                    CURRENT_FILTER = Constants.FILTER_GIRL;
                    currentPage = 1;
                    //rightBtn.setText(sexFilter[Integer.parseInt(Constants.FILTER_GIRL)]);
                } else if (which == 1) {
                    CURRENT_FILTER = Constants.FILTER_BOY;
                    currentPage = 1;
                } else if (which == 2) {
                    CURRENT_FILTER = Constants.FILTER_ALL;
                    currentPage = 1;
                }
                //rightBtn.setText(sexFilter[Integer.parseInt(CURRENT_FILTER)]);
                MySharedPrefs.write(NearByPeopleUI.this, MySharedPrefs.FILE_USER, MySharedPrefs.NEARY_PEOPLE_FILTER, CURRENT_FILTER);

            }
        });
        mdf.show(getSupportFragmentManager(), "mdf");

    }


    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        UserInfoVo clickVo = source.get(position);
        if (clickVo != null) {
            Utils.intentFriendUserInfo(this, clickVo, false);
        }
    }

    /**
     * Get the last pull-down time
     *
     * @return
     */
    private String getLastUpdateTimeStamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("MM:dd   HH:mm");
        Date curDate = new Date(System.currentTimeMillis());
        String tmStr = formatter.format(curDate);
        return tmStr;
    }

    @Override
    public void start() {
    }

    @Override
    public void success(JSONObject response) {

        currentPage = oldPage;
        if (currentPage == 1) {
            source.clear();
            //Utils.writeToFile(response, "friend-userlist.json");
            Utils.writeToFile(response, "discover-people_nearby.json");
        }
        JSONArray jsonArray = response.optJSONArray("data");
        parseJson(jsonArray);
        adapter.resetSource(source);
        isLoadingData = false;
        swipeLayout.setRefreshing(false);
        if (jsonArray != null && jsonArray.length() >= 10) {
            nearLv.resetFooterState(true);
        } else {
            nearLv.resetFooterState(false);
        }
        checkListEmpty();
    }

    /*parse json*/
    private void parseJson(JSONArray jsonArray) {
        if (jsonArray != null) {
            int count = jsonArray.length();
            for (int i = 0; i < count; i++) {
                UserInfoVo uInfo = new UserInfoVo().parse(jsonArray
                        .optJSONObject(i));
                source.add(uInfo);
            }
        }
    }

    @Override
    public void error(VolleyError error, int errorCode, String errorMsg) {
        isLoadingData = false;
        swipeLayout.setRefreshing(false);
        if (error != null) {
            error.printStackTrace();
        } else {
            MyToast.showToast(this, errorMsg);
        }

    }

    @Override
    public void loadMore() {
        loadNearByList(currentPage + 1, CURRENT_FILTER);
    }

    @Override
    public void onRefresh() {
        loadNearByList(1, CURRENT_FILTER);
    }


    /**
     * Check whether the current list is empty
     * @datetime 2017/12/15
     */
    private void checkListEmpty() {
        if (source == null || source.size() == 0) {
            emptyRela.setVisibility(View.VISIBLE);
            emptyTextView.setText(R.string.empty_nearby_people);
        } else {
            emptyRela.setVisibility(View.GONE);
        }
    }
}
