package com.lingtuan.firefly.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * @description Test Dialog
 */
public class MyDialogFragment extends DialogFragment {


	public static final int DIALOG_LIST = 1;

    private int dialogType = -1 ; 

    private ItemClickCallback itemClickCallback = null ; 
    
    private int itemArrayId = 0 ;


    
    public interface ItemClickCallback{
		void itemClickCallback(int which);
	}
    
	public MyDialogFragment() { }
	
	public MyDialogFragment(int dialogType,int itemArrayId) {
		this.dialogType = dialogType;
		this.itemArrayId = itemArrayId;
	}

	public void setItemClickCallback(ItemClickCallback itemClickCallback){
		this.itemClickCallback = itemClickCallback;
	}

	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		if(isAdded()){
			getDialog().setCanceledOnTouchOutside(true);
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

        switch (dialogType) {
			case DIALOG_LIST:
				return new AlertDialog.Builder(getActivity())
					.setItems(itemArrayId, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if(itemClickCallback!=null){
								itemClickCallback.itemClickCallback(which);
							}
						}
					})
					.create();
			}
        return null;
	}

}
