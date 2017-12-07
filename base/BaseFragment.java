package com.lingtuan.firefly.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.lingtuan.firefly.util.MyToast;

/**
 * Created on 2017/8/21.
 */

public abstract  class BaseFragment extends Fragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    protected void showToast(String msg){
        MyToast.showToast(getActivity(), msg);
    }

}
