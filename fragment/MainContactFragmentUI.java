package com.lingtuan.firefly.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.R;
import com.lingtuan.firefly.base.BaseFragment;
import com.lingtuan.firefly.contact.ContactSearchNickUI;
import com.lingtuan.firefly.contact.DiscussGroupListUI;
import com.lingtuan.firefly.contact.adapter.NewContactListAdapter;
import com.lingtuan.firefly.contact.vo.ContactVo;
import com.lingtuan.firefly.contact.vo.NewContactVO;
import com.lingtuan.firefly.custom.contact.ContactItemComparator;
import com.lingtuan.firefly.custom.contact.ContactItemInterface;
import com.lingtuan.firefly.db.user.FinalUserDataBase;
import com.lingtuan.firefly.login.LoginUtil;
import com.lingtuan.firefly.quickmark.CaptureActivity;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.LoadingDialog;
import com.lingtuan.firefly.util.MyDialogFragment;
import com.lingtuan.firefly.util.MySharedPrefs;
import com.lingtuan.firefly.util.Utils;
import com.lingtuan.firefly.vo.UserBaseVo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Friends list page (network)
 */
public class MainContactFragmentUI extends BaseFragment implements  OnItemClickListener, OnItemLongClickListener,SwipeRefreshLayout.OnRefreshListener, OnClickListener, MainContactContract.View {

    private View view;
    private boolean isDataFirstLoaded;

    //The title bar
    private ImageView mBack;
    private TextView mTitle;
    private ImageView mContactAddBtn;

    //The friends list
    private List<UserBaseVo> mFriendInfoList = new ArrayList<>();
    private ListView mNewListView;
    private List<ContactItemInterface> mContactList = new ArrayList<>();
    //The contact number
    private TextView contactNum;
    private NewContactListAdapter mNewContactListAdapter;

    private Object mLockContact = new Object();//Save friends information thread lock
    private boolean isStop;//Repeat to save

    //Friends broadcast to state changes
    private RefreshContactReceiver contactReceiver;

    private int currentThread;
    private int totalThread;
    private boolean hasInit = false;//Determine whether synchronous web period of contact, only the first time synchronization

    private SwipeRefreshLayout swipeLayout;

    private TextView uploadRegisterInfo;//Synchronous registration information


    private PopupWindow homePop;

    private MainContactContract.Presenter mPresenter;


    public MainContactFragmentUI() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        isDataFirstLoaded = true;
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        if (!isDataFirstLoaded && view != null) {
            ((ViewGroup) view.getParent()).removeView(view);
            return view;
        }
        view = inflater.inflate(R.layout.main_contact_layout, container, false);
        initView();
        return view;
    }

    private void initView() {

        new MainContactPresenter(this);

        mBack =  view.findViewById(R.id.app_back);
        mTitle =  view.findViewById(R.id.app_title);
        mNewListView = view.findViewById(R.id.contact_list);

        uploadRegisterInfo =  view.findViewById(R.id.uploadRegisterInfo);

        uploadRegisterInfo.setOnClickListener(this);
        swipeLayout = view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(R.color.black);
        View headerDiscus = View.inflate(getActivity(), R.layout.include_friends_header_discuss, null);
        mNewListView.addHeaderView(headerDiscus);

        View footerView = View.inflate(getActivity(), R.layout.include_friends_footer, null);
        contactNum = footerView.findViewById(R.id.include_contact_num);
        mNewListView.addFooterView(footerView);
        contactNum.setText(getString(R.string.contact_num, 0));

        mNewContactListAdapter = new NewContactListAdapter(getActivity(), R.layout.contact_child_item, mContactList, false);
        mNewListView.setAdapter(mNewContactListAdapter);


        mNewListView.setOnItemClickListener(this);
        mNewListView.setOnItemLongClickListener(this);

        //A popwindow click add buddy, etc
        mContactAddBtn =  view.findViewById(R.id.detail_set);
        mContactAddBtn.setImageResource(R.drawable.icon_add_friend);
        mContactAddBtn.setVisibility(View.VISIBLE);


        contactReceiver = new RefreshContactReceiver();
        IntentFilter filter = new IntentFilter(Constants.ACTION_SELECT_CONTACT_REFRESH);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(contactReceiver, filter);

        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(Constants.ACTION_NETWORK_RECEIVER);//Network changes to monitor
        getActivity().registerReceiver(mBroadcastReceiver, filter1);
    }


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && Constants.ACTION_NETWORK_RECEIVER.equals(intent.getAction())) {
                Utils.updateViewMethod(uploadRegisterInfo,getActivity());
            }
        }
    };


    @SuppressLint("HandlerLeak")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!isDataFirstLoaded) {
            return;
        }
        mTitle.setText(R.string.main_contact);
        mContactAddBtn.setOnClickListener(this);
        mBack.setVisibility(View.GONE);
        isDataFirstLoaded = false;
    }

    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getActivity() != null){
            getActivity().unregisterReceiver(mBroadcastReceiver);
        }
    }

    @Override
    public void setPresenter(MainContactContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void loadFriendsSuccess(JSONObject response) {
        new MainContactFragmentUI.UpdateFriendThread(response).start();
    }

    @Override
    public void loadFriendsError(int errorCode, String errorMsg) {
        swipeLayout.setRefreshing(false);
        showToast(errorMsg);
    }

    @Override
    public void deleteFriendsSuccess(String message, String uid) {
        showToast(message);
        FinalUserDataBase.getInstance().deleteFriendByUid(uid);
        int i = 0;
        for (UserBaseVo info : mFriendInfoList) {
            if (uid.equals(info.getLocalId())) {
                mFriendInfoList.remove(i);
                break;
            }
            i++;
        }
        mContactList.clear();
        for (UserBaseVo voT : mFriendInfoList) {
            NewContactVO info = mPresenter.getNewContact(voT);
            mContactList.add(info);
        }
        mNewListView.setFastScrollEnabled(true);
        Collections.sort(mContactList, new ContactItemComparator());
        mNewContactListAdapter.updateList(mContactList);
        contactNum.setText(getString(R.string.contact_num, mContactList.size()));
    }

    @Override
    public void deleteFriendsError(int errorCode, String errorMsg) {
        showToast(errorMsg);
    }

    @Override
    public void createDiscussionGroupStart() {
        LoadingDialog.show(getActivity(),"");
    }

    @Override
    public void createDiscussionGroupSuccess(String message, String cid, List<UserBaseVo> member) {
        LoadingDialog.close();
        showToast(message);
        Utils.gotoGroupChat(getActivity(),false,null,cid, member);
    }

    @Override
    public void createDiscussionGroupError(int errorCode, String errorMsg) {
        LoadingDialog.close();
        if (errorCode == -1) {
            showToast(getString(R.string.error_net));
        } else {
            showToast(errorMsg);
        }
    }

    class LoadDatabasesThread extends Thread {
        @SuppressLint("HandlerLeak")
        private Handler handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (currentThread == totalThread) {
                    ContactVo vo = (ContactVo) msg.obj;
                    mFriendInfoList.clear();
                    mFriendInfoList.addAll(vo.getmFriendInfoList());
                    mContactList.clear();
                    mContactList.addAll(vo.getmContactList());
                    mNewListView.setFastScrollEnabled(true);
                    mNewContactListAdapter.updateList(mContactList);
                    try {
                        contactNum.setText(getString(R.string.contact_num, mContactList.size()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //Callback interface synchronous no net friends
                    uploadOfflineFriends();

                    if (!hasInit) {
                        hasInit = true;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mPresenter.loadFriends();
                            }
                        }, 500);
                    }
                }
            }
        };

        @Override
        public void run() {
            try {
                totalThread++;
                List<UserBaseVo> mGList = FinalUserDataBase.getInstance().getFriendUserBaseAll();
                List<ContactItemInterface> mCList = new ArrayList<>();
                for (UserBaseVo vos : mGList) {
                    if (vos == null) {
                        continue;
                    }
                    NewContactVO info = mPresenter.getNewContact(vos);
                    mCList.add(info);
                }
                Collections.sort(mCList, new ContactItemComparator());
                ContactVo go = new ContactVo();
                go.setmFriendInfoList(mGList);
                go.setmContactList(mCList);
                currentThread++;
                Message msg = new Message();
                msg.obj = go;
                handler.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Synchronous no net friends
     * */
    private void uploadOfflineFriends() {
        if (!Utils.isConnectNet(getActivity())){
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (int i =  0; i < mFriendInfoList.size() ; i++){
            if (mFriendInfoList.get(i).isOffLine()){
                builder.append(mFriendInfoList.get(i).getLocalId()).append(",");
            }
        }
        if (builder.length() > 0){
            builder.deleteCharAt(builder.length()-1);
            mPresenter.addOfflineFriend(builder.toString());
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.detail_set: //Search for friends
                startActivity(new Intent(getActivity(), ContactSearchNickUI.class));
                Utils.openNewActivityAnim(getActivity(), false);
                break;
            case R.id.contact_search_bg:
                break;
            case R.id.txt_home_pop_3:       //scan
                startActivity(new Intent(getActivity(), CaptureActivity.class));
                Utils.openNewActivityAnim(getActivity(), false);
                dismissHomePop();
                break;
            case R.id.uploadRegisterInfo:       //register
                LoginUtil.getInstance().initContext(getActivity());
                LoginUtil.getInstance().showRegistDialog(uploadRegisterInfo);
                break;
            default:
                break;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == getActivity().RESULT_OK){//Select the contact to return
            ArrayList<UserBaseVo> selectList = (ArrayList<UserBaseVo>) data.getSerializableExtra("selectList");
            if (selectList != null && selectList.size() == 1) {
                UserBaseVo vo = selectList.get(0);
                Utils.intentChattingUI(getActivity(), vo.getLocalId(), vo.getThumb(), vo.getShowName(), vo.getGender(),vo.getFriendLog(),false, false, false, 0, false);
            } else if (selectList != null && selectList.size() > 1) {
                StringBuilder touids = new StringBuilder();
                for (UserBaseVo vo : selectList) {
                    touids.append(vo.getLocalId()).append(",");
                }
                touids.deleteCharAt(touids.lastIndexOf(","));
                mPresenter.createDiscussionGroup(touids.toString(), selectList);
            }
        }
    }


    private void dismissHomePop() {
        if (homePop != null && homePop.isShowing()) {
            homePop.dismiss();
            homePop = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()) {
            new LoadDatabasesThread().start();
            if (Constants.isRefresh) {
                mPresenter.loadFriends();
                Constants.isRefresh = false;
            }
        }
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            Utils.updateViewMethod(uploadRegisterInfo,getActivity());
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position >= mContactList.size() + 1) {
            return;
        }else if (position == 0) {//group chat
            startActivity(new Intent(getActivity(), DiscussGroupListUI.class));
            Utils.openNewActivityAnim(getActivity(), false);
            return;
        }
        NewContactVO baseVo = (NewContactVO) mContactList.get(position - 1);
        baseVo.setFriendLog(1);
        Utils.intentFriendUserInfo(getActivity(), baseVo, false);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,int position, long id) {
        if (position < 1 || position >= mContactList.size() + 1) {
            return true;
        }
        NewContactVO vo = (NewContactVO) mContactList.get(position - 1);
        if (TextUtils.equals(NextApplication.myInfo.getLocalId(),vo.getLocalId())){
            return true;
        }
        ShowDeleteDialog(vo.getLocalId(), ((NewContactVO) mContactList.get( position - 1)).isOffLine());
        return true;
    }

    /**
     * Remove buddy bounced
     * */
    private void ShowDeleteDialog(final String uid, final boolean isOfflineFriend) {
        MyDialogFragment mdf = new MyDialogFragment(MyDialogFragment.DIALOG_LIST, R.array.delete_friend_array);
        mdf.setItemClickCallback(new MyDialogFragment.ItemClickCallback() {
            @Override
            public void itemClickCallback(int which) {
                deleteFriends(uid,isOfflineFriend);
            }
        });
        mdf.show(getFragmentManager(), "mdf");
    }

    /**
     * Remove buddy
     * */
    private void deleteFriends(final String uid, final boolean isFriendOffline) {
        if(isFriendOffline){
            FinalUserDataBase.getInstance().deleteFriendByUid(uid);
            int i = 0;
            for (UserBaseVo info : mFriendInfoList) {
                if (uid.equals(info.getLocalId())) {
                    mFriendInfoList.remove(i);
                    break;
                }
                i++;
            }
            mContactList.clear();
            for (UserBaseVo voT : mFriendInfoList) {
                NewContactVO info = mPresenter.getNewContact(voT);
                mContactList.add(info);
            }
            mNewListView.setFastScrollEnabled(true);
            Collections.sort(mContactList, new ContactItemComparator());
            mNewContactListAdapter.updateList(mContactList);
            contactNum.setText(getString(R.string.contact_num, mContactList.size()));
        }else {
            mPresenter.deleteFriends(uid);
        }
    }

    @Override
    public void onRefresh() {
        mPresenter.loadFriends();
    }

    /**
     * The refresh friends broadcast
     */
    class RefreshContactReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && Constants.ACTION_SELECT_CONTACT_REFRESH.equals(intent.getAction())) {
                mPresenter.loadFriends();
            }
        }
    }

    /**
     * Save the friend information
     */
    class UpdateFriendThread extends Thread {
        JSONObject response;

        @SuppressLint("HandlerLeak")
        private Handler handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                isStop = true;
                new SaveFriendThread().start();
                try {
                    mNewListView.setFastScrollEnabled(true);
                    mNewContactListAdapter.updateList(mContactList);
                    contactNum.setText(getString(R.string.contact_num, mContactList.size()));
                    swipeLayout.setRefreshing(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        UpdateFriendThread(JSONObject response) {
            this.response = response;
        }

        @Override
        public void run() {
            synchronized (mLockContact) {
                JSONArray array = response.optJSONArray("data");
                UserBaseVo voT;
                List<UserBaseVo> mGList = new ArrayList<>();
                List<ContactItemInterface> mCList = new ArrayList<>();
                UserBaseVo voMe = new UserBaseVo();
                if (NextApplication.myInfo != null){
                    voMe.setGender(NextApplication.myInfo.getGender());
                    voMe.setPic(NextApplication.myInfo.getPic());
                    voMe.setSightml(NextApplication.myInfo.getSightml());
                    voMe.setThumb(NextApplication.myInfo.getThumb());
                    voMe.setLocalId(NextApplication.myInfo.getLocalId());
                    voMe.setUsername(NextApplication.myInfo.getShowName());
                    mGList.add(voMe);
                }
                NewContactVO infoMe = mPresenter.getNewContact(voMe);
                infoMe.setFriendLog(-1);
                mCList.add(infoMe);

                for (int i = 0; i < array.length(); i++) {
                    voT = new UserBaseVo().parse(array.optJSONObject(i));
                    mGList.add(voT);
                    voT.setLocalId(voT.getLocalId());
                    NewContactVO info = mPresenter.getNewContact(voT);
                    mCList.add(info);
                }

                for(int i=0 ;i<mFriendInfoList.size();i++){
                    boolean foundUser=false;
                    for(int m=0;m<mGList.size();m++){
                        if(mFriendInfoList.get(i).isOffLine() && mGList.get(m).getLocalId().equals(mFriendInfoList.get(i).getLocalId())){
                            foundUser = true;
                            break;
                        }
                    }
                    if(mFriendInfoList.get(i).isOffLine()){
                        if(foundUser){
                            mFriendInfoList.remove(i);
                            i--;
                        }
                    }else{
                        mFriendInfoList.remove(i);
                        i--;
                    }
                }

                for (int i = 0; i < mFriendInfoList.size(); i++) {
                    voT = mFriendInfoList.get(i);
                    voT.setLocalId(voT.getLocalId());
                    NewContactVO info = mPresenter.getNewContact(voT);
                    mCList.add(info);
                }
                mFriendInfoList.addAll(mGList);
                mContactList.clear();
                mContactList.addAll(mCList);
                Collections.sort(mContactList, new ContactItemComparator());
                Message msg = new Message();
                handler.sendMessage(msg);
            }
        }
    }


    /**
     * Save the friend information
     */
    class SaveFriendThread extends Thread {
        @Override
        public void run() {
            synchronized (mLockContact) {
                isStop = false;
                try {
                    FinalUserDataBase.getInstance().clearFriends(mFriendInfoList);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    FinalUserDataBase.getInstance().beginTransaction();
                    for (int i = 0; i < mFriendInfoList.size(); i++) {
                        if (isStop) {
                            break;
                        }
                        UserBaseVo vo = mFriendInfoList.get(i);
                        if(vo.getLocalId().equals(NextApplication.myInfo.getLocalId())){
                            continue;
                        }
                        if (!TextUtils.isEmpty(vo.getNote())) {
                            MySharedPrefs.write(getActivity(), MySharedPrefs.KEY_FRIEND_NOTE + NextApplication.myInfo.getLocalId(), vo.getLocalId(), vo.getNote());
                        } else {
                            MySharedPrefs.removeFriendNote(getActivity(), vo.getLocalId());
                        }
                        FinalUserDataBase.getInstance().saveFriendUserBase(vo);
                    }
                    if (!isStop) {
                        FinalUserDataBase.getInstance().setTransactionSuccessful();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    FinalUserDataBase.getInstance().endTransaction();
                }
            }
        }
    }

}