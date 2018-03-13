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
 * @TODO Messages sent to remove users
 */
public class SendRemovedPeerInfoRunable  implements Runnable {
	private WifiPeopleVO peerInfo;
	private AppNetService netService;
	private String host;
	private int port;
	
	public SendRemovedPeerInfoRunable(String host, int port, AppNetService netService, WifiPeopleVO peerInfo) {
		this.host = host;
		this.port = port; 
		this.netService = netService;
		this.peerInfo = peerInfo;
	
	}
	
	@Override
	public void run() {
		if (sendPeerInfo(host, port,peerInfo))//successed
		{
			
		}
	}	
	 
  private boolean sendPeerInfo(String host, int port, WifiPeopleVO peerInfo) {
		Socket socket = new Socket();
//		String strIP = getLocalIpAddress();
		boolean result = true;
	
		try {
			socket.bind(null);
			socket.connect((new InetSocketAddress(host, port)), Constants.SOCKET_TIMEOUT);// host
			OutputStream stream = socket.getOutputStream();
			//type|jsonlengthlength｜jsonlength｜json|… data
			stream.write(Constants.COMMAND_ID_SEND_TYPE_SYSTEM);
			JSONObject object=new JSONObject();
			try {
				object.put("type", "1");
				object.put("userid", peerInfo.getLocalId());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			String jsonlength=object.toString().getBytes("utf-8").length+"";
			stream.write(jsonlength.length());
			stream.write(jsonlength.getBytes());
			String temp=object.toString();
			stream.write(temp.getBytes());
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