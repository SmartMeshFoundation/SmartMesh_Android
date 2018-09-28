package com.lingtuan.firefly.spectrum;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.spectrum.vo.AddressContactVo;
import com.lingtuan.firefly.util.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

public class WalletContactListUI extends BaseActivity {

    @BindView(R.id.app_right)
    ImageView appRight;
    @BindView(R.id.empty_text)
    TextView emptyText;
    @BindView(R.id.empty_like_rela)
    RelativeLayout emptyLikeRela;
    @BindView(R.id.listView)
    ListView listView;

    private ArrayList<AddressContactVo> source;
    private WalletContactAdapter mAdapter;

    @Override
    protected void setContentView() {
        setContentView(R.layout.custom_list_layout);
    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {
        setTitle(getString(R.string.wallet_contact_title));
        appRight.setVisibility(View.VISIBLE);
        appRight.setImageResource(R.drawable.wallet_address_contact_add);
        source = new ArrayList<>();
        mAdapter = new WalletContactAdapter(this,null);
        listView.setAdapter(mAdapter);
        loadWalletContact();
    }

    private void loadWalletContact() {

    }

    @OnClick({R.id.app_right})
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.app_right:
                startActivity(new Intent(WalletContactListUI.this,WalletContactCreateUI.class));
                Utils.openNewActivityAnim(WalletContactListUI.this,false);
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    /**
     * To test whether the current list is empty
     */
    private void checkListEmpty() {
        if(source == null || source.size() == 0){
            emptyLikeRela.setVisibility(View.VISIBLE);
            emptyText.setText(R.string.black_list_empty);
        }else{
            emptyLikeRela.setVisibility(View.GONE);
        }
    }

}
