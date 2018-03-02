package com.lingtuan.firefly.offline;

import android.net.Uri;


import com.lingtuan.firefly.offline.vo.WifiPeopleVO;
import com.lingtuan.firefly.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
* @TODO Send a new user
*/
public class SendPeerInfoRunable  implements Runnable {

   private WifiPeopleVO peerInfo;
   private AppNetService netService;
   private String host;
   private int port;

   public SendPeerInfoRunable(String host, int port, AppNetService netService, WifiPeopleVO peerInfo) {
       this.host = host;
       this.port = port;
       this.netService = netService;
       this.peerInfo = peerInfo;

   }

   @Override
   public void run() {
       if (sendPeerInfo(host, port,peerInfo))
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
           } catch (JSONException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
           }

           String jsonlength=object.toString().getBytes("utf-8").length+"";
           stream.write(jsonlength.length());
           stream.write(jsonlength.getBytes());
           String temp=object.toString();
           stream.write(temp.getBytes());

           Uri uri= Uri.fromFile(new File(peerInfo.getThumb()));
           InputStream ins = netService.getInputStream(uri);
           byte buf[] = new byte[1024];
           int len;
           while ((len = ins.read(buf)) != -1) {
               stream.write(buf, 0, len);
           }
           ins.close();




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
