package com.lingtuan.firefly.base;

public interface BaseView <P extends BasePresenter> {

    void setPresenter(P presenter);

}
