package com.lingtuan.firefly.offline;


import com.lingtuan.firefly.offline.vo.WifiPeopleVO;
import com.lingtuan.firefly.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author Create on 2014-11-17
 * @TODO Send text chat messages
 */
public class SendStringRunable  implements Runnable {
	private String content;
	private AppNetService netService;
	private String host;
	private int port;
	private String touid;
	private boolean isGroupMsg;
	private WifiPeopleVO peerInfo;
  	private String msgid;

  public SendStringRunable(String host, int port, AppNetService netService, WifiPeopleVO peerInfo, String content, boolean isGroupMsg, String touid, String msgid) {
		this.host = host;
		this.port = port; 
		this.netService = netService;
		this.content = content;
		this.touid=touid;
		this.isGroupMsg=isGroupMsg;
		this.peerInfo=peerInfo;
	    this.msgid=msgid;
	}
	
	@Override
	public void run() {
		if (sendString(host, port,peerInfo,content,isGroupMsg,touid)){//successed
			netService.postSendMsgResult(true,0,this.msgid);
		}else{//failed
			netService.postSendMsgResult(false,0,this.msgid);
		}
	}	
	 
	private boolean sendString(String host, int port, WifiPeopleVO peerInfo, String content, boolean isGroupMsg, String touid) {
		Socket socket = new Socket();
//		String strIP = getLocalIpAddress();
		boolean result = true;
	
		try {
			socket.bind(null);
			socket.connect((new InetSocketAddress(host, port)), Constants.SOCKET_TIMEOUT);// host
			OutputStream stream = socket.getOutputStream();
			//type|jsonlengthlength｜jsonlength｜json|… data
			if(isGroupMsg){
			    stream.write(Constants.COMMAND_ID_SEND_TYPE_ROOMCHAT);
			}else{
				stream.write(Constants.COMMAND_ID_SEND_TYPE_NORMALCHAT);
			}
		
			JSONObject object=new JSONObject();
			try {
				object.put("type", "0");
				object.put("userid", peerInfo.getLocalId());
				object.put("host", peerInfo.getHost());
				object.put("port", peerInfo.getPort()+"");
				object.put("deviceaddress", peerInfo.getDeviceAddress()+"");
				object.put("username", peerInfo.getUserName());
				object.put("usergender", peerInfo.getGender());
				object.put("age", peerInfo.getAge());
				object.put("sightml", peerInfo.getSightml());
				object.put("address", peerInfo.getAddress());
				object.put("msgid",msgid);
				if(!isGroupMsg){
					object.put("touserid", touid);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			String jsonlength=object.toString().getBytes("utf-8").length+"";
			stream.write(jsonlength.length());
			stream.write(jsonlength.getBytes());
			String temp=object.toString();
			stream.write(temp.getBytes());
			stream.write(content.getBytes());
			stream.close();

		} catch (IOException e) {
			result = false;
		} finally {
			if (socket != null) {
				if (socket.isConnected()) {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return result;
	}
}
