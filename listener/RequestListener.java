package com.lingtuan.firefly.listener;

import org.json.JSONObject;

/**
 * Created on 2017/9/28.
 * Network request interface
 */

public interface RequestListener {
    /** Start the request data */
    void start();

    /** * Data request successful*/
    void success(JSONObject response);

    /*** Data request failed*/
    void error(int errorCode, String errorMsg);

}
