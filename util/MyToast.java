package com.lingtuan.firefly.util;
import android.content.Context;
import android.text.TextUtils;
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
					toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
				} else {
					toast.setText(message);
				}
				toast.show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}catch(Error e){
			e.printStackTrace();
		}
	}
}
