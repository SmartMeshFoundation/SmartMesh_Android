package com.lingtuan.firefly.util;

import android.content.Context;

import com.lingtuan.firefly.R;
import com.lingtuan.firefly.raiden.RaidenNet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

/**
 * Abnormal log collection classes
 */
public class CrashHandler implements UncaughtExceptionHandler {
	
	/** CrashHandler instance */
	private static CrashHandler instance;
	/** The Context object of the program */
	private Context mContext;
	/** The system default UncaughtException processing class */
	private Thread.UncaughtExceptionHandler mDefaultHandler;

	/** Ensure that only a CrashHandler instance */
	private CrashHandler() { }

	/** Get CrashHandler instance, singleton pattern */
	public static CrashHandler getInstance() {
		if (instance == null)
			instance = new CrashHandler();
		return instance;
	}

	/**
	 * Initialization, register the Context object, access to the system default UncaughtException processor, sets the default this CrashHandler for application processor
	 */
	public void init(Context ctx) {
		mContext = ctx;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * When UncaughtException happened into the function to deal with
	 */
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			//If the user does not deal with the system default exception handler to handle
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

	/**
	 * custom error handling, collection error message send error reports are completed in this operation. The developer can customize according to the exception handling logic
	 * @ param ex
	 * @ return true: if the exception handling information;Otherwise it returns false
	 */
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return true;
		}
		ex.printStackTrace();
	    saveCrashInfoToFile(ex);
		return true;
	}

	/**
	 * Save error information to a file
	 */
	private String saveCrashInfoToFile(Throwable ex) {
		String chanel = "SmartMesh";
		try {
			RaidenNet.getInatance().stopRaiden();
			String result = getErrorInfo(ex);
			StringBuilder sb = new StringBuilder();
			sb.append(mContext.getString(R.string.normal_mobile_models)).append(android.os.Build.MODEL).append("\n")
			  .append(mContext.getString(R.string.normal_system_version)).append(android.os.Build.VERSION.RELEASE).append("\n")
			  .append(mContext.getString(R.string.normal_app_version)).append(Utils.getVersionName(mContext)).append("\n")
			  .append(mContext.getString(R.string.normal_app_channel)).append(chanel).append("\n")
			  .append(mContext.getString(R.string.normal_crash_time)).append(Utils.eventDetailTime(System.currentTimeMillis() / 1000)).append("\n")
			  .append(result).toString();
			return SDCardCtrl.saveCrashInfoToFile(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/** 
　　* Get the wrong information
　　* @param arg1 
　　* @return 
　　*/ 
	private String getErrorInfo(Throwable arg1) {
		Writer writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		arg1.printStackTrace(pw); 
		pw.close(); 
		String error= writer.toString();
		return error; 
	} 
}