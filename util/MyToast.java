package com.lingtuan.firefly.util;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

public class MyToast {
	
	private static Toast toast;

	public static void showToast(Context context, String message) {
		try {
			if(TextUtils.isEmpty(message)){
				return;
			}
			if (context != null) {
				if (toast == null) {
					toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
				} else {
					toast.setText(message);
				}
				toast.setGravity(Gravity.BOTTOM, 0, 250);
				toast.show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}catch(Error e){
			e.printStackTrace();
		}
	}

	public static void showCenterToast(Context context, String message) {
		try {
			if(TextUtils.isEmpty(message)){
				return;
			}
			if (context != null) {
				if (toast == null) {
					toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
				} else {
					toast.setText(message);
				}
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}catch(Error e){
			e.printStackTrace();
		}
	}
}
