package com.lingtuan.firefly.offline;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.lingtuan.firefly.NextApplication;
import com.lingtuan.firefly.util.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;


public class ThreadPoolManager extends HandlerThread {
	private final ServerSocket serverSocket;
	private final ExecutorService pool;
	private final AppNetService netService;
	private final static String TAG = "ServiceThread";
	private boolean isServiceRun = true;
	private Handler handler = null;

	public ThreadPoolManager(AppNetService service, int port, int poolSize)throws Exception {
		super(TAG, Process.THREAD_PRIORITY_FOREGROUND);
		assert (poolSize > 0);
		this.netService = service;
		serverSocket = new ServerSocket(port);
		pool = Executors.newFixedThreadPool(poolSize);
	}

	public Handler getHandler() {
		return handler;
	}

	final void setServiceRun(boolean isRun) {
		this.isServiceRun = isRun; 
	}

	final boolean isServiceRun() {
		return isServiceRun; 
	}

	static private class ServiceThreadHandler extends Handler {
		private ThreadPoolManager sThread;
		ServiceThreadHandler(ThreadPoolManager service) {
			super(service.getLooper());
			this.sThread = service;
		}
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.MSG_SERVICE_POOL_START:
				while (sThread.isServiceRun()) {
					try {
						Socket sock = sThread.serverSocket.accept();
						sThread.pool.execute(new HandleAcceptSocket(sThread.netService, sock));
					} catch (Exception ex) {
						sThread.pool.shutdown();
						break;
					}			
				}
				break;
			}
			super.handleMessage(msg);
		}
	}
	
	public void init() {
		setServiceRun(true);
		if (!this.isAlive()) {
			this.start();
			handler = new ServiceThreadHandler(this);
		}
		Message msg = new Message();
		msg.what = Constants.MSG_SERVICE_POOL_START;
		getHandler().sendMessage(msg);
	}

	public void uninit() {
		setServiceRun(false);
	}
	
	public void execute (Runnable command) {
		pool.execute(command);
	}

	public void destory() {
		setServiceRun(false);
		shutdownAndAwaitTermination();
		this.quit();
	}
	
	private void shutdownAndAwaitTermination() {// ExecutorService pool
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(60, TimeUnit.SECONDS))
					System.err.println("Pool did not terminate");
			}
			if (!serverSocket.isClosed()) {
				serverSocket.close();
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class HandleAcceptSocket implements Runnable {
	private final Socket socket;
	private final AppNetService netService;
	private static final ReentrantLock lockRecvFile = new ReentrantLock();

	HandleAcceptSocket(AppNetService service, Socket socket) {
		this.netService = service;
		this.socket = socket;
	}
	
	public void closeSocket() {
		if (!socket.isClosed()) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void run() {
		try {
			InputStream ins = socket.getInputStream();
			int iCommand = ins.read();
			InetSocketAddress sockAddr = (InetSocketAddress) socket.getRemoteSocketAddress();

			if (iCommand == Constants.COMMAND_ID_SEND_TYPE_SYSTEM) {
				netService.handleRecvSystemMsg(ins,sockAddr);
			}else if (iCommand == Constants.COMMAND_ID_SEND_TYPE_NORMALCHAT) {
				netService.handleRecvChatMsg(ins, false,sockAddr);
			}else if (iCommand == Constants.COMMAND_ID_SEND_TYPE_ROOMCHAT) {
				synchronized (NextApplication.lock){
					 netService.handleRecvChatMsg(ins, true,sockAddr);
				}
			}
			ins.close();
		} catch (IOException e) {
			return;
		}
	}

}
