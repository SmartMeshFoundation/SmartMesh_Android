package com.lingtuan.firefly.raiden;

/**
 * Created on 2018/2/15.
 */

public interface ChangeChannelStateListener {

    /**
     * change channel
     * @param position  list position
     * @param isOpen    true open false  close
     */
    void changeChannel(int position, boolean isOpen);

    /**
     * deposit channel
     * @param position list position
     * */
    void deopsitChannel(int position);

    /**
     * transfer channel
     * @param position list position
     * */
    void transferChannel(int position);
}
