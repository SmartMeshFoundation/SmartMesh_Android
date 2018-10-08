package com.lingtuan.firefly.spectrum;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseActivity;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.spectrum.vo.AddressContactVo;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyDialogFragment;
import com.lingtuan.firefly.util.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;

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
    private boolean isSendWallet = false;

    @Override
    protected void setContentView() {
        setContentView(R.layout.custom_list_layout);
        getPassData();
    }

    private void getPassData() {
        isSendWallet = getIntent().getBooleanExtra("isSendWallet",false);
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

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWalletContact();
    }

    private void loadWalletContact() {
        LoadingDialog.show(WalletContactListUI.this,"");
        source = FinalUserDataBase.getInstance().getWalletContactList();
        if (source != null && source.size() > 0){
            mAdapter.resetSource(source);
        }
        LoadingDialog.close();
        checkListEmpty();
    }

    @OnItemClick(R.id.listView)
    public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
        if (isSendWallet){
            Intent intent = new Intent();
            intent.putExtra(Constants.WALLET_ADDRESS,source.get(position).getWalletAddress());
            setResult(RESULT_OK,intent);
            finish();
        }
    }


    @OnItemLongClick(R.id.listView)
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        MyDialogFragment mdf = new MyDialogFragment(MyDialogFragment.DIALOG_LIST,R.array.edit_del_list);
        mdf.setItemClickCallback(new MyDialogFragment.ItemClickCallback() {
            @Override
            public void itemClickCallback(int which) {
               if (0 == which){//edit contact
                   Intent intent = new Intent(WalletContactListUI.this,WalletContactCreateUI.class);
                   intent.putExtra(Constants.WALLET_CONTACT,source.get(position));
                   startActivity(intent);
                   Utils.openNewActivityAnim(WalletContactListUI.this,false);
               }else{// delete contact
                    LoadingDialog.show(WalletContactListUI.this,"");
                    FinalUserDataBase.getInstance().deleteWalletContact(source.get(position).getWalletAddress());
                    source.remove(position);
                    mAdapter.resetSource(source);
                    LoadingDialog.close();
                    checkListEmpty();
               }
            }
        });
        mdf.show(getSupportFragmentManager(), "mdf");
        return true;
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
            emptyText.setText(R.string.wallet_contact_empty);
        }else{
            emptyLikeRela.setVisibility(View.GONE);
        }
    }

}
