package com.lingtuan.firefly.custom.contact;


import java.util.ArrayList;
import com.lingtuan.firefly.custom.contact.HanziToPinyin3.Token;

public class PinYin{
	// The same return return pinyin Chinese characters, letters, are converted to lowercase
	public static String getPinYin(String input){
		ArrayList<Token> tokens = HanziToPinyin3.getInstance().get(input);
		StringBuilder sb = new StringBuilder();
		if (tokens != null && tokens.size() > 0){
			for (Token token : tokens){
				if (Token.PINYIN == token.type){
					sb.append(token.target);
				} else{
					sb.append(token.source);
				}
			}
		}
		return sb.toString().toLowerCase();
	}
}
