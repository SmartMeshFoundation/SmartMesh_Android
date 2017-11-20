package com.lingtuan.firefly.vo;

import java.io.Serializable;

/**
 * Country code
 */
public class CountryCodeVo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;   //State name
	private String sortLetters;  //National pinyin initials
	private String code;//;Country code
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSortLetters() {
		return sortLetters;
	}
	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}
	
	public void parseDate(String date){
		String[] content = date.split(":");
		setCode(content[0].trim());
		setName(content[1]);
		setSortLetters(content[2]);
	}
}
