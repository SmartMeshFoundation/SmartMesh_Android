package com.lingtuan.firefly.offline;


import com.lingtuan.firefly.offline.vo.WifiPeopleVO;
import com.lingtuan.firefly.util.Constants;
import com.lingtuan.firefly.util.FileSizeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @TODO  Send text chat messages
 */
public class SendFileRunable implements Runnable {
	private String path;
	private AppNetService netService;
	private String host;
	private int port;
	private String touid;
	private WifiPeopleVO peerInfo;
	private String msgid;

	private boolean isStop;
	private boolean cancelSend;
	public SendFileRunable(String host, int port, AppNetService netService, WifiPeopleVO peerInfo, String path, String touid,String msgid) {
		this.host = host;
		this.port = port;
		this.netService = netService;
		this.path = path;
		this.touid = touid;
		this.peerInfo = peerInfo;
		this.msgid = msgid;
	}

	@Override
	public void run() {
		if (sendFile(host, port, peerInfo, path, touid)){// successed
			if(!cancelSend){
				netService.postSendMsgResult(true, 2,this.msgid);
			}
		} else {// failed
			netService.postSendMsgResult(false,1,this.msgid);
		}

	}

	public String getRunableID(){
		return  msgid;
	}

	public boolean isStop(){
		return  isStop;
	}

	public void cancelSend(){
		cancelSend = true;
		synchronized (this){
			notify();
		}
	}

	public  void startSend(){
		synchronized (this){
			notify();
		}
	}
	private boolean sendFile(String host, int port, WifiPeopleVO peerInfo,String path, String touid) {
			Socket socket = new Socket();
			// String strIP = getLocalIpAddress();
			boolean result = true;
			try {
				socket.bind(null);
				socket.connect((new InetSocketAddress(host, port)),Constants.SOCKET_TIMEOUT);// host
				OutputStream stream = socket.getOutputStream();
				stream.write(Constants.COMMAND_ID_SEND_TYPE_NORMALCHAT);

				File file = new File(path);
				long totalLength= 0;

				try {
					totalLength= FileSizeUtils.getFileSize(file);
				}catch (Exception e){
					e.printStackTrace();
				}
				JSONObject object = new JSONObject();
				try {
					object.put("type", "1009");
					object.put("userid", peerInfo.getLocalId());
					object.put("host", peerInfo.getHost());
					object.put("port", peerInfo.getPort()+"");
					object.put("deviceaddress", peerInfo.getDeviceAddress()+"");
					object.put("username", peerInfo.getUserName());
					object.put("usergender", peerInfo.getGender());
					object.put("age", peerInfo.getAge());
					object.put("sightml", peerInfo.getSightml());
					object.put("address", peerInfo.getAddress());
					object.put("touserid", touid);
					object.put("title", file.getName());
					object.put("number", totalLength+"");
					object.put("msgid",msgid);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				String jsonlength = object.toString().getBytes("utf-8").length + "";
				stream.write(jsonlength.length());
				stream.write(jsonlength.getBytes());
				String temp = object.toString();
				stream.write(temp.getBytes());

				InputStream ins = new FileInputStream(path);
				byte buf[] = new byte[1024*10];//Accept the file every time to write more
				int len;

				long currentLength = 0;

				synchronized (this) {
					try {
						wait();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if(!cancelSend){
					netService.postSendMsgResult(true, 0, this.msgid);
				}
				while (!cancelSend && (len = ins.read(buf)) != -1) {
					stream.write(buf, 0, len);
					currentLength+= len;
					//Progress notification interface to send/receive file.
					netService.postSendBytes(currentLength,msgid);

				}
				ins.close();
				stream.close();

			} catch (IOException e) {
				result = false;
				isStop = true;
			} finally {
				isStop = true;
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