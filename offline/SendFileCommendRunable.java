package com.lingtuan.firefly.offline;


import com.lingtuan.firefly.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author Create on 2014-11-17
 * @TODO Messages sent to remove users
 */
public class SendFileCommendRunable implements Runnable {
	private AppNetService netService;
	private String host;
	private int port;
	private String msgid;
    private int commend;
	private String touid;

	public SendFileCommendRunable(String host, int port, AppNetService netService, String touid, String msgid, int commend) {
		this.host = host;
		this.port = port; 
		this.netService = netService;
		this.msgid = msgid;
		this.commend = commend;
		this.touid = touid;
	
	}
	
	@Override
	public void run() {
		if (sendFileCommend(host, port,touid, msgid,commend))//successed
		{
			
		}
		
	}	
	 
  private boolean sendFileCommend(String host, int port, String touid, String msgid, int commend) {
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
				object.put("type", "2");
				object.put("commend",commend+"");
				object.put("msgid",msgid);
				object.put("touserid", touid);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
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
						// Give up
						e.printStackTrace();
					}
				}
			}
		}
		return result;
	}
}