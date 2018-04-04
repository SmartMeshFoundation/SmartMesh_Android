package com.lingtuan.firefly.util.netutil;

import android.text.TextUtils;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.listener.RequestListener;
import com.lingtuan.firefly.util.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2017/9/28.
 * Interface implementation class
 */

public class NetRequestImpl {

    //Interface implementation class
    private static NetRequestImpl instance;

    //Interface wrapper classes
    private static VolleyUtils requestUtils;


    /**
     * The singleton
     * */
    public static NetRequestImpl getInstance(){
        if(instance == null ){
            instance = new NetRequestImpl();
        }
        requestUtils = VolleyUtils.getInstance();
        return instance;
    }

    public void destory(){
        instance = null;
        requestUtils.destory();
    }

    /**
     * registered
     * @ param username username
     * @ param "password," password
     * @ param mid user login id custom unique id
     * @ param localid locally generated unique id used to netcom
     * @ param listener log in listening
     */
    public void register(String username, String password,String mid,String localid,String aeskey, RequestListener listener){
        Map<String,String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        if (!TextUtils.isEmpty(mid)){
            params.put("mid", mid);
        }
        params.put("localid", localid);
        params.put("aeskey", aeskey);
        params.put("encrypt", "0");
        JSONObject jsonRequest = requestUtils.getJsonRequest("user", "register",false,params);
        requestUtils.requestJsonObject(jsonRequest, listener);
    }

    /**
     * login authentication
     * @ param mid user login
     * @ param "password," password
     * @ param listener log in listening
     */
    public void login(String mid, String password,String aeskey, RequestListener listener){
        Map<String,String> params = new HashMap<>();
        params.put("loginid", mid);
        params.put("password", password);
        params.put("aeskey", aeskey);
        params.put("encrypt", "1");
        JSONObject jsonRequest = requestUtils.getJsonRequest("user", "login",false,params);
        requestUtils.requestJsonObject(jsonRequest, listener);
    }

    /**
     * exit
     */
    public void logout( RequestListener listener){
        JSONObject jsonRequest = requestUtils.getJsonRequest("user", "logout",false,null);
        requestUtils.requestJsonObject(jsonRequest, listener);
    }

    /**
     * request user information
     * @ param localid user id
     * @ param listener log in listening
     */
    public void requestUserInfo(String localid,RequestListener listener){
        Map<String,String> params = new HashMap<>();
        params.put("localid",localid);
        JSONObject jsonRequest = requestUtils.getJsonRequest("user", "userinfo",true,params);
        requestUtils.requestJsonObject(jsonRequest, listener);
    }


    /**
     * edit album interface
     * @ param picid photo id
     */
    public void editAlbum(String picid,RequestListener listener) {
        Map<String, String> params = new HashMap<>();
        params.put("picid", picid);
        JSONObject jsonObject = requestUtils.getJsonRequest("user", "edit_album",true, params);
        requestUtils.requestJsonObject(jsonObject,listener);
    }

    /**
     * binding mobile phone number
     * @ param phonenumber phone number
     */
    public void bindMobile(String phonenumber,RequestListener listener){
        Map<String,String> params = new HashMap<>();
        params.put("phonenumber",phonenumber);
        JSONObject jsonObject =requestUtils.getJsonRequest("user", "bingd_mobile",true, params);
        requestUtils.requestJsonObject(jsonObject,listener);
    }

    /**
     * binding email
     * @ param email mailbox
     */
    public void bindEmail(String email,RequestListener listener){
        Map<String,String> params = new HashMap<>();
        params.put("email",email);
        JSONObject jsonObject =requestUtils.getJsonRequest("user", "bingd_email",true, params);
        requestUtils.requestJsonObject(jsonObject,listener);
    }

    /**
     * phone number to retrieve password
     * @ param phonenumber phone number
     * @ param password the new password
     */
    public void smscForget(String phonenumber,String password,RequestListener listener){
        Map<String,String> params = new HashMap<>();
        params.put("phonenumber",phonenumber);
        params.put("password",password);
        JSONObject jsonObject =requestUtils.getJsonRequest("user", "smsc_forget",false,params);
        requestUtils.requestJsonObject(jsonObject,listener);
    }

    /**
     * phone number to retrieve password
     * @ param email mailbox
     * @ param password the new password
     */
    public void emailForget(String email,String password,RequestListener listener){
        Map<String,String> params = new HashMap<>();
        params.put("email",email);
        params.put("password",password);
        JSONObject jsonObject =requestUtils.getJsonRequest("user", "email_forget",false,params);
        requestUtils.requestJsonObject(jsonObject,listener);
    }

    /**
     * upload users face file
     * @ param imgPath head walk normal logic path is empty Logic is not null to upload files
     * @ param sightml signature
     * @ param username username
     * @ param birthcity user address
     * @ param gender 1 male, 2 female gender
     */
    public void editUserInfo(String imgPath,String username,String sightml,String gender,String birthcity,RequestListener listener) throws Exception{
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("sightml", sightml);
        params.put("gender", gender);
        params.put("birthcity", birthcity);
        JSONObject jsonRequest = requestUtils.getJsonRequest("user", "edit_userinfo", true,params);
        if (imgPath != null){
            requestUtils.requestImage(jsonRequest,"face",imgPath,listener);
        }else{
            requestUtils.requestJsonObject(jsonRequest,listener);
        }

    }


    /**
     * add wallet address
     * @param address  Account address
     */
    public void addAddress(String address,RequestListener listener){
        Map<String, String> params = new HashMap<>();
        params.put("address", address);
        JSONObject jsonRequest = requestUtils.getJsonRequest("user", "add_address", true,params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }

    /**
     * delete wallet address
     * @param address  Account address
     */
    public void delAddress(String address,RequestListener listener){
        Map<String, String> params = new HashMap<>();
        params.put("address", address);
        JSONObject jsonRequest = requestUtils.getJsonRequest("user", "del_address", true,params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }



    /**
     * Get the transaction record interface
     * @param page page
     * @param type  0 SMT Account Trading   1 ETH Account Trading
     * @param address  Account address
     */
    public void getTxlist(int page,int type,String address,RequestListener listener){
        Map<String, String> params = new HashMap<>();
        params.put("page", page + "");
        params.put("address", address);
        params.put("type", type + "");
        JSONObject jsonRequest = requestUtils.getJsonRequest("user", "transaction_list", true,params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }


    /**
     *Get the blacklist list
     * @ param page page
     * */
    public void getBlackList(int page,RequestListener listener){
        Map<String, String> params = new HashMap<>();
        params.put("page", page+"");
        JSONObject jsonRequest = requestUtils.getJsonRequest("blacklist","black_list",true,params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }

    /**
     * blacklist user operation
     * @ param blocalid shielding removed the user id
     * @ param state 0 to join blacklist 1 remove blacklist
     * */
    public void updateBlackState(int state,String blocalid,RequestListener listener){
        Map<String, String> params = new HashMap<>();
        params.put("blocalid", blocalid);
        if (state == 0){
            params.put("isblack", "1");
        }
        JSONObject jsonRequest = requestUtils.getJsonRequest("blacklist",state == 0 ?"add_black_list" : "remove_black_list",true,params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }

    /**
     * report users to join blacklist the same interface
     * @ param blocalid report to the user id
     * @ param type type shielding does not need to pass the parameters (just);
     * */
    public void reportFriend(int type,String blocalid,RequestListener listener){
        Map<String, String> params = new HashMap<>();
        params.put("blocalid", blocalid);
        params.put("type", type + "");
        JSONObject jsonRequest = requestUtils.getJsonRequest("blacklist","add_black_list",true,params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }

    /**
     * create a group chat
     * @ param tolocalids member id
     * @ param listener callback interface
     */
    public void createDiscussionGroups(String tolocalids, RequestListener listener) {
        Map<String, String> params = new HashMap<>();
        params.put("tolocalids", tolocalids);
        JSONObject jsonRequest = requestUtils.getJsonRequest("conversation", "create_conversation",true,params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }

    /**
     * load group chat list
     * @ param listener callback interface
     */
    public void loadGroupList(RequestListener listener) {
        JSONObject jsonRequest = requestUtils.getJsonRequest("conversation","get_conversation",true,null);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }

    /**
     * for group chat member list
     * @ param cid group chat id
     */
    public void getDiscussMumbers(String cid, RequestListener listener) {
        Map<String, String> params = new HashMap<>();
        params.put("cid", cid);
        JSONObject jsonRequest = requestUtils.getJsonRequest("conversation","get_mumbers",true,params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }

    /**
     * modify group chat name
     * @ param name group chat name
     */
    public void renameDicsuss(String name,int cid,RequestListener listener) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("plid", cid + "");
        JSONObject jsonRequest = requestUtils.getJsonRequest("conversation","edit_conversation",true,params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }

    /**
     * delete group members
     * @ param aimid member id
     * @ param cid group chat id
     */
    public void removeDiscussMember(String aimid,int cid,RequestListener listener) {
        Map<String, String> params = new HashMap<>();
        params.put("aimid", aimid);
        params.put("cid", cid + "");
        JSONObject jsonRequest = requestUtils.getJsonRequest("conversation","quit_group",true,params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }

    /**
     * add a group chat
     * @ param tolocalids localid, more uid joining together,,,,
     * @ param cid group chat id
     */
    public void addDiscussMembers(String tolocalids,int cid,RequestListener listener) {
        Map<String, String> params = new HashMap<>();
        params.put("tolocalids", tolocalids.toString());
        params.put("cid", cid + "");
        JSONObject jsonRequest = requestUtils.getJsonRequest("conversation","add_group",true,params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }

    /**
     * switch notification mode
     * @ param isChecked is selected
     * @ param cid group chat id
     */
    public void switchNotify(boolean isChecked,int cid,RequestListener listener) {
        Map<String, String> params = new HashMap<>();
        if (isChecked){
            params.put("mask", "1");
        }else{
            params.put("mask", "0");
        }
        params.put("cid", cid + "");
        JSONObject jsonRequest = requestUtils.getJsonRequest("conversation","mask_group",true,params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }

    /**
     * request to join the group chat
     * @ param cid group chat id
     */
    public void joinDiscussGroup(String cid,RequestListener listener){
        Map<String, String> params = new HashMap<>();
        params.put("cid", cid);
        JSONObject jsonRequest = requestUtils.getJsonRequest("conversation","join_group",true,params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }

    /**
     * upload audio files
     * @ param type, file type
     */
    public void uploadSpeak(int type,String path,String messageid,RequestListener listener) throws Exception{
        Map<String, String> params = new HashMap<>();
        params.put("type", type + "");
        JSONObject jsonRequest = requestUtils.getJsonRequest("conversation","speak",true,params);
        requestUtils.requestFile(jsonRequest,"face",path,messageid,listener);
    }


    /**
     * search friends
     * @ param text keywords
     */
    public void friendSearch(int page,String text, RequestListener listener){
        Map<String, String> params = new HashMap<>();
        params.put("page", page + "");
        params.put("keyword", text);
        JSONObject jsonRequest = requestUtils.getJsonRequest("discover", "search",true,params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }


    /**
     * collect cancel collect file interface
     * @ param count file id
     * @ param filetype file type 0: message file 1: group space file (no groups have been useless)
     * @ param fileurl file path
     * @ param collectState collect state 1 c
     */
    public void fileCollect(String fileid,long filetype,String fileurl,int collectState,RequestListener listener) {
        Map<String, String> params = new HashMap<>();
        params.put("fileid", fileid);
        params.put("filetype", filetype > 0 ? "0" : "1");
        params.put("fileurl", fileurl);
        JSONObject jsonRequest = requestUtils.getJsonRequest("file_transfers", collectState == 1 ? "del_collect" : "file_collect", true,params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }

    /**
     * collect/cancel collect files
     * @ param type 1 0 cancel collection collection file
     * @ param fileurl address file
     * @ param filetype file type 0: message file 1: group space file (have no group the useless)
     * @ param count file id
     */
    public void collectFile(int type,String fileId,int filetype,String fileurl,RequestListener listener) {
        Map<String, String> params = new HashMap<>();
        params.put("fileid", fileId);
        params.put("filetype", filetype == 0 ? "0" : "1");
        if (type == 1){
            params.put("fileurl", fileurl);
        }
        JSONObject jsonRequest = requestUtils.getJsonRequest("file_transfers", type == 1 ? "file_collect" : "del_collect",true, params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }

    /**
     * load friends
     * @ param listener callback interface
     */
    public void loadFriends(RequestListener listener) {
        JSONObject jsonRequest = requestUtils.getJsonRequest("friend", "friend_list", true,null);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }

    /**
     * add buddy
     * @ param flocalid to add the id
     * @ param content added reason (don't fill in)
     * @ param note buddy note (not fill in)
     * @ param listener callback interface
     */
    public void addFriend(String flocalid,String note,String content, RequestListener listener){
        Map<String, String> params = new HashMap<>();
        params.put("flocalid", flocalid);
        params.put("content", content);
        params.put("note", note);
        JSONObject jsonRequest = requestUtils.getJsonRequest("friend", "add_friend",true, params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }


    /**
     * add buddy
     * @ param flocalids to add the id of the multiple ids, segmentation
     * @ param listener callback interface
     */
    public void addOfflineFriend(String flocalids,RequestListener listener){
        Map<String, String> params = new HashMap<>();
        params.put("flocalids", flocalids);
        JSONObject jsonRequest = requestUtils.getJsonRequest("friend", "netless_friend",true, params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }


    /**
     * agree to add buddy
     * @ param flocalid to add the id
     */
    public void addFriendAgree(String flocalid, RequestListener listener){
        Map<String, String> params = new HashMap<>();
        params.put("flocalid", flocalid);
        JSONObject jsonRequest = requestUtils.getJsonRequest("friend", "agree_friend",true,params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }


    /**
     * remove buddy
     * @ param uid friends uid
     */
    public void deleteFriends(final String uid,RequestListener listener) {
        Map<String, String> params = new HashMap<>();
        params.put("flocalid", uid);
        JSONObject jsonRequest = requestUtils.getJsonRequest("friend", "delete_friend",true, params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }

    /**
     * Note modify close friends
     * @ param flocalid to add the id
     * @ param note note information
     * @ param listener callback interface
     */
    public void updateFriendNote(String flocalid,String note, RequestListener listener){
        Map<String, String> params = new HashMap<>();
        params.put("localid", flocalid);
        params.put("note", note);
        JSONObject jsonRequest = requestUtils.getJsonRequest("friend", "edit_note",true, params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }

    /**
     * upload directory
     * @ param typelist contacts list
     */
    public void uploadContactFriend(String typelist,RequestListener listener) {
        Map<String, String> parameter = new HashMap<>();
        parameter.put("type", "1");
        parameter.put("typelist", typelist);
        JSONObject jsonRequest = requestUtils.getJsonRequest("friend", "contact_friend",true, parameter);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }

    /**
     * A report chat messages
     * @ param type report type
     * @ param buid was to report the user id
     * @ param content to report content
     */
    public void sendReportChatMsg(String type,String buid,String content,RequestListener listener) {
        Map<String, String> params = new HashMap<>();
        params.put("buid",buid);
        params.put("type",type);
        params.put("content",content);
        JSONObject jsonRequest = requestUtils.getJsonRequest("friend","report_message", true,params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }


    /**
     * send mail
     * @ param aeskey aes and secret key
     * @ param type email mailbox type 0 binding 1 forget password
     * @ param email box Numbers
     */
    public void sendMail(String aeskey,int type,String email,RequestListener listener){
        Map<String,String> params = new HashMap<>();
        params.put("aeskey", aeskey);
        params.put("encrypt", "1");
        params.put("type",type + "");
        params.put("email",email);
        JSONObject jsonObject =requestUtils.getJsonRequest("mail", "send",false,params);
        requestUtils.requestJsonObject(jsonObject,listener);
    }

    /**
     * validation email
     * @ param code verification code
     * @ param email box Numbers
     */
    public void verifyMail(String code,String email,RequestListener listener){
        Map<String,String> params = new HashMap<>();
        params.put("code",code);
        params.put("email",email);
        JSONObject jsonObject =requestUtils.getJsonRequest("mail", "verify",false,params);
        requestUtils.requestJsonObject(jsonObject,listener);
    }


    /**
     * access to the user blocking state
     * @ param maskid to view the user id
     */
    public void getMaskUser(String maskid,RequestListener listener) {
        Map<String, String> params = new HashMap<>();
        params.put("masklocalid", maskid);
        JSONObject jsonRequest = requestUtils.getJsonRequest("message","get_mask_user",true,params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }

    /**
     * message block
     * @ param maskid shielding user id
     * @ param isChecked is selected (blocked)
     */
    public void maskUser(String maskid,boolean isChecked,RequestListener listener){
        Map<String, String> params = new HashMap<>();
        params.put("masklocalid", maskid);
        params.put("status", isChecked ? "1"  : "0" );
        JSONObject jsonRequest = requestUtils.getJsonRequest("message","mask_user",true,params);
        requestUtils.requestJsonObject(jsonRequest,listener);
    }


    /**
     * send SMS
     * @ param aeskey aes and secret key
     * @ param type text type 0 binding mobile phone number 1 forgot password
     * @ param phonenumber phone number
     */
    public void sendSmsc(String aeskey,int type,String phonenumber,RequestListener listener){
        Map<String,String> params = new HashMap<>();
        params.put("aeskey", aeskey);
        params.put("encrypt", "1");
        params.put("type",type + "");
        params.put("phonenumber",phonenumber);
        JSONObject jsonObject =requestUtils.getJsonRequest("smsc", "send",false,params);
        requestUtils.requestJsonObject(jsonObject,listener);
    }

    /**
     * validation messages
     * @ param code verification code
     * @ param phonenumber phone number
     */
    public void verifySmsc(String code,String phonenumber,RequestListener listener){
        Map<String,String> params = new HashMap<>();
        params.put("code",code);
        params.put("phonenumber",phonenumber);
        JSONObject jsonObject =requestUtils.getJsonRequest("smsc", "verify",false,params);
        requestUtils.requestJsonObject(jsonObject,listener);
    }



    /**
     * version update
     * currentversion version number
     * @ param listener log in listening
     */
    public void versionUpdate(RequestListener listener){
        Map<String,String> params = new HashMap<>();
        params.put("currentversion", Utils.getVersionName(NextApplication.mContext));
        JSONObject jsonRequest = requestUtils.getJsonRequest("system", "version",false,params);
        requestUtils.requestJsonObject(jsonRequest, listener);
    }

    /**
     * Node information
     */
    public void getSyncNode(RequestListener listener){
        JSONObject jsonRequest = requestUtils.getJsonRequest("system", "nodeinfo",false,null);
        requestUtils.requestJsonObject(jsonRequest, listener);
    }

    /**
     * feedBack
     * @param content     content
     * @param email       your email
     * @param type        feed back type
     */
    public void feedBack(String content,String email,int type,RequestListener listener){
        Map<String,String> params = new HashMap<>();
        params.put("content",content);
        params.put("email",email);
        params.put("type",type + "");
        JSONObject jsonRequest = requestUtils.getJsonRequest("system", "feedback",true,params);
        requestUtils.requestJsonObject(jsonRequest, listener);
    }

    /**
     * Query SMT and token balances
     * 查询SMT和token余额
     * @param address        wallet address
     * @param token_address  contact address
     */
    public void getBalance(String address,String token_address,RequestListener listener){
        Map<String,String> params = new HashMap<>();
        params.put("encrypt", "1");
        params.put("address",address);
        params.put("token_address",token_address);
        JSONObject jsonRequest = requestUtils.getJsonRequest("wallet", "get_balance",false,params);
        requestUtils.requestJsonObject(jsonRequest, listener);
    }

    /**
     * Get SMT transaction gas
     * 获取SMT交易gas
     */
    public void getGas(RequestListener listener){
        Map<String,String> params = new HashMap<>();
        params.put("encrypt", "1");
        JSONObject jsonRequest = requestUtils.getJsonRequest("wallet", "get_gas",false,params);
        requestUtils.requestJsonObject(jsonRequest, listener);
    }

    /**
     * Get SMT transaction nonce
     * 获取SMT交易Nonce
     * @param address        wallet address
     */
    public void getNonce(String address,RequestListener listener){
        Map<String,String> params = new HashMap<>();
        params.put("encrypt", "1");
        params.put("address",address);
        JSONObject jsonRequest = requestUtils.getJsonRequest("wallet", "get_nonce",false,params);
        requestUtils.requestJsonObject(jsonRequest, listener);
    }

    /**
     * Send a transaction
     * 发送一笔交易
     * @param data        signed data to send
     */
    public void sendTransaction(String data,RequestListener listener){
        Map<String,String> params = new HashMap<>();
        params.put("encrypt", "1");
        params.put("data",data);
        JSONObject jsonRequest = requestUtils.getJsonRequest("wallet", "send_raw_transaction",false,params);
        requestUtils.requestJsonObject(jsonRequest, listener);
    }

    /**
     * Get the latest block number of SMT
     * 获取SMT当前最新区块号
     */
    public void getBlockNumber(RequestListener listener){
        Map<String,String> params = new HashMap<>();
        params.put("encrypt", "1");
        JSONObject jsonRequest = requestUtils.getJsonRequest("wallet", "get_block_number",false,params);
        requestUtils.requestJsonObject(jsonRequest, listener);
    }

    /**
     * Get the block number of the SMT transaction hash
     * 获取SMT交易Hash所在区块号
     */
    public void getTxBlockNumber(String tx,RequestListener listener){
        Map<String,String> params = new HashMap<>();
        params.put("encrypt", "1");
        params.put("tx", tx);
        JSONObject jsonRequest = requestUtils.getJsonRequest("wallet", "tx_block_number",false,params);
        requestUtils.requestJsonObject(jsonRequest, listener);
    }

    /**
     * Search for Tokens published on SMT
     * 搜索在SMT发布的Token
     * @param keyword     keyword
     * @param address     address
     */
    public void searchToken(String keyword,String address,RequestListener listener){
        Map<String,String> params = new HashMap<>();
        params.put("encrypt", "1");
        params.put("keyword", keyword);
        params.put("address", address);
        JSONObject jsonRequest = requestUtils.getJsonRequest("wallet", "search_token",false,params);
        requestUtils.requestJsonObject(jsonRequest, listener);
    }

    /**
     * Bind the token to the display list
     * 绑定token到展示列表
     * @param address         address
     * @param token_address   contact address
     */
    public void bindTokenToList(String address,String token_address,RequestListener listener){
        Map<String,String> params = new HashMap<>();
        params.put("encrypt", "1");
        params.put("address", address);
        params.put("token_address", token_address);
        JSONObject jsonRequest = requestUtils.getJsonRequest("wallet", "add_token",false,params);
        requestUtils.requestJsonObject(jsonRequest, listener);
    }

    /**
     * Get the background record token list when opening the token list for the first time
     * 首次打开token列表时获取后台记录token列表
     * @param address         address
     */
    public void getTokenList(String address,RequestListener listener){
        Map<String,String> params = new HashMap<>();
        params.put("encrypt", "1");
        params.put("address", address);
        JSONObject jsonRequest = requestUtils.getJsonRequest("wallet", "token_list",false,params);
        requestUtils.requestJsonObject(jsonRequest, listener);
    }
}
