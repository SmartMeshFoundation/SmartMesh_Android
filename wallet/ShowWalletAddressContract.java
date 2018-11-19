package com.lingtuan.wallet;


import com.lingtuan.meshbox.base.BasePresenter;
import com.lingtuan.meshbox.base.BaseView;

import java.util.Map;

public class ShowWalletAddressContract {
    public interface View extends BaseView {
    }

    public interface Presenter extends BasePresenter {

        void loadData(String url, Map<String, Object> map, String message);

        void loadPeopleTypeData(String url, String message);
    }
}
