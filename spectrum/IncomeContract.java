package com.lingtuan.firefly.spectrum;


import com.lingtuan.meshbox.base.BasePresenter;
import com.lingtuan.meshbox.base.BaseView;

import java.util.Map;

public class IncomeContract {
    public interface View extends BaseView {

    }

    public interface Presenter extends BasePresenter {

        void loadData(String type);

    }
}
