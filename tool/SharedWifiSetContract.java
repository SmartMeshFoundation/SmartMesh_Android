package com.lingtuan.firefly.tool;


import com.lingtuan.firefly.base.BasePresenter;
import com.lingtuan.firefly.base.BaseView;

import java.util.Map;

public class SharedWifiSetContract {
    public interface View extends BaseView {
    }

    public interface Presenter extends BasePresenter {

        void loadData(String url, Map<String, Object> map, String message);

        void loadPeopleTypeData(String url, String message);
    }
}
