package com.lingtuan.firefly.wallet.vo;

import org.json.JSONObject;

/**
 * Created  on 2017/8/28.
 * gas
 */

public class GasVo {
    private int minPrice;//min gasPrice
    private int maxPrice;//max gasPrice
    private int defaultPrice;//default gasPrice
    private int minLimit;//min limit
    private int maxLimit;//max limit

    public int getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(int minPrice) {
        this.minPrice = minPrice;
    }

    public int getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(int maxPrice) {
        this.maxPrice = maxPrice;
    }

    public int getDefaultPrice() {
        return defaultPrice;
    }

    public void setDefaultPrice(int defaultPrice) {
        this.defaultPrice = defaultPrice;
    }

    public int getMinLimit() {
        return minLimit;
    }

    public void setMinLimit(int minLimit) {
        this.minLimit = minLimit;
    }

    public int getMaxLimit() {
        return maxLimit;
    }

    public void setMaxLimit(int maxLimit) {
        this.maxLimit = maxLimit;
    }

    public GasVo parse(JSONObject object){
        if (object == null){
            return null;
        }
        setMinPrice(object.optInt("minPrice"));
        setMaxPrice(object.optInt("maxPrice"));
        setDefaultPrice(object.optInt("defaultPrice"));
        setMinLimit(object.optInt("minLimit"));
        setMaxLimit(object.optInt("maxLimit"));
        return this;
    }
}
