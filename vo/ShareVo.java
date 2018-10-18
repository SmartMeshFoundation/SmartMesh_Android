package com.lingtuan.firefly.vo;

public class ShareVo {

	private String text = null;

	private int iconId;
	
	public ShareVo(){}
	
	public ShareVo(String text, int iconId){
		this.text = text;
		this.iconId = iconId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getIconId() {
		return iconId;
	}

	public void setIconId(int iconId) {
		this.iconId = iconId;
	}
}
