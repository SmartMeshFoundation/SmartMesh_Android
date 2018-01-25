package com.lingtuan.firefly.raiden;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.raiden.vo.RaidenChannelVo;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyViewDialogFragment;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.wallet.vo.StorableWallet;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created on 2018/1/24.
 * raiden channel list ui
 */

public class RaidenChannelList extends BaseActivity implements  SwipeRefreshLayout.OnRefreshListener, RaidenChannelListAdapter.ChangeChannelStateListener {

    private static int RAIDEN_CHANNEL_CREATE = 100;

//    private TextView emptyTextView;
//    private RelativeLayout emptyRela;

    private ListView listView = null;
    private SwipeRefreshLayout swipeLayout;
    private ImageView createChannel;
    private RaidenChannelListAdapter mAdapter = null;
    private List<RaidenChannelVo> source = null ;

    private StorableWallet storableWallet;

    @Override
    protected void setContentView() {
        setContentView(R.layout.raiden_channel_list);
        getPassData();
    }


    private void getPassData() {
        storableWallet = (StorableWallet) getIntent().getSerializableExtra("storableWallet");
    }

    @Override
    protected void findViewById() {
        listView = (ListView) findViewById(R.id.listView);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

//        emptyRela = (RelativeLayout) findViewById(R.id.empty_like_rela);
//        emptyTextView = (TextView) findViewById(R.id.empty_text);
        createChannel = (ImageView) findViewById(R.id.app_right);
    }

    @Override
    protected void setListener() {
        swipeLayout.setOnRefreshListener(this);
        createChannel.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.raiden_channel));
        swipeLayout.setColorSchemeResources(R.color.black);
        createChannel.setVisibility(View.VISIBLE);
        createChannel.setImageResource(R.drawable.icon_home_more);
        source = new ArrayList<>();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
                loadChannelList();
            }
        }, 500);
        mAdapter = new RaidenChannelListAdapter(this, source,storableWallet,this);
        listView.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.app_right:
                Intent intent = new Intent(this,RaidenCreateChannel.class);
                intent.putExtra("storableWallet",storableWallet);
                startActivityForResult(intent,RAIDEN_CHANNEL_CREATE);
                Utils.openNewActivityAnim(this,false);
                break;
        }
    }


    @Override
    public void onRefresh() {
        loadChannelList();
    }

    private void loadChannelList() {
        RaidenNetUtils.getInstance().getChannels(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.sendEmptyMessage(0);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Message message = Message.obtain();
                message.what = 1;
                message.obj = response.body().string();
                mHandler.sendMessage(message);
            }
        });
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    showToast(getString(R.string.error_get_balance));
                    swipeLayout.setRefreshing(false);
                    break;
                case 1:
                    swipeLayout.setRefreshing(false);
                    parseJson((String)msg.obj);
                    break;
                case 2:
                    LoadingDialog.close();
                    break;
                case 3:
                    LoadingDialog.close();
                    break;
            }
        }
    };


    private void parseJson(String jsonString) {
        if (TextUtils.isEmpty(jsonString)){
            return;
        }
        try {
            source.clear();
            JSONArray array = new JSONArray(jsonString);
            if (array.length() > 0){
                for (int i = 0 ; i < array.length() ; i++){
                    JSONObject object = array.optJSONObject(i);
                    RaidenChannelVo channelVo = new RaidenChannelVo().parse(object);
                    if (TextUtils.equals(Constants.CONTACT_ADDRESS,channelVo.getTokenAddress().toLowerCase())){
                        source.add(channelVo);
                    }
                }
            }
            mAdapter.resetSource(source);
//            checkListEmpty();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

//    /**
//     * To test whether the current list is empty
//     */
//    private void checkListEmpty() {
//        if(source == null || source.size() == 0){
//            emptyRela.setVisibility(View.VISIBLE);
//            emptyTextView.setText(R.string.black_list_empty);
//        }else{
//            emptyRela.setVisibility(View.GONE);
//        }
//    }

    @Override
    public void changeChannel(final int position,boolean isOpen) {
        if (isOpen){
            MyViewDialogFragment mdf = new MyViewDialogFragment();
            mdf.setTitleAndContentText(getString(R.string.raiden_channel_close), null);
            mdf.setOkCallback(new MyViewDialogFragment.OkCallback() {
                @Override
                public void okBtn() {
                    channelCloseMethod(position);
                }
            });
            mdf.show(getSupportFragmentManager(), "mdf");
        }
    }

    /**
     * close channel
     * */
    private void channelCloseMethod(int position) {
        if (source.size() <= 0){
            return;
        }
        RaidenChannelVo channelVo = source.get(position);
        LoadingDialog.show(this,"");
        RaidenNetUtils.getInstance().closeChannel(channelVo.getChannelAddress(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.sendEmptyMessage(2);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mHandler.sendEmptyMessage(3);
            }
        });
    }
}
