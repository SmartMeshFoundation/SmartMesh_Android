package com.lingtuan.firefly.raiden;

import com.lingtuan.firefly.NextApplication;

import java.math.BigDecimal;

/**
 * Created on 2018/1/25.
 * raiden net utils
 */

public class RaidenNetUtils {

    private static RaidenNetUtils instance;

    private static String ROOT_URL = "http://127.0.0.1:5001/api/1/";

    /**
     * The singleton
     * */
    public static RaidenNetUtils getInstance(){
        if(instance == null ){
            instance = new RaidenNetUtils();
        }
        return instance;
    }

    public void destory(){
        instance = null;
    }


    /**
     * get channel list
     * @return raiden channel list
     * */
    public String getChannels() throws Exception{
//        if (NextApplication.api != null){
//           return NextApplication.api.getChannelList();
//        }
        return null;
    }

    /**
     * open channel
     * @param partner_address  target address   (to address)
     * @param token_address    token address    (contract address)
     * @param balance          channel balance  (mortgage amount)
     * */
    public String openChannel(String partner_address,String token_address,double balance,int settle_timeout) throws Exception{
//        BigDecimal ONE_ETHER = new BigDecimal("1000000000000000000");
//        String newBalance = new BigDecimal(balance).multiply(ONE_ETHER).setScale(0,BigDecimal.ROUND_DOWN).toPlainString();
//        if (NextApplication.api != null){
//            return NextApplication.api.openChannel(partner_address,token_address,settle_timeout,newBalance );
//        }
        return null;
    }

    /**
     * close channel
     * @param channel_address channel address
     * */
    public String closeChannel(String channel_address) throws Exception{
//        if (NextApplication.api != null){
//            return NextApplication.api.closeChannel(channel_address);
//        }
        return null;
    }

    /**
     * deposit channel
     * @param balance             deposit amount
     * @param channel_address     channel address
     * */
    public String depositChannel(double balance,String channel_address) throws Exception{
//        BigDecimal ONE_ETHER = new BigDecimal("1000000000000000000");
//        String newBalance = new BigDecimal(balance).multiply(ONE_ETHER).setScale(0,BigDecimal.ROUND_DOWN).toPlainString();
//        if (NextApplication.api != null){
//            return NextApplication.api.depositChannel(channel_address,newBalance);
//        }
        return null;
    }

    /**
     * send amount
     * @param amount              transfer amount
     * @param partner_address     target address   (to address)
     * @param token_address       token address    (contract address)
     * */
    public String sendAmount(double amount,String partner_address,String token_address) throws Exception {
//        BigDecimal ONE_ETHER = new BigDecimal("1000000000000000000");
//        String newAmount = new BigDecimal(amount).multiply(ONE_ETHER).setScale(0,BigDecimal.ROUND_DOWN).toPlainString();
//        long time= System.currentTimeMillis()/1000;
//        if (NextApplication.api != null){
////            return NextApplication.api.transfers(token_address,partner_address,newAmount,time);
//        }
        return null;
    }
}
