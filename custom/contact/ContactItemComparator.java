package com.lingtuan.firefly.custom.contact;

import android.text.TextUtils;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContactItemComparator implements Comparator<ContactItemInterface>{

	@Override
	public int compare(ContactItemInterface lhs, ContactItemInterface rhs){
		if (TextUtils.isEmpty(lhs.getItemForIndex())){//Empty row till the last
			return 1;
		} else if(TextUtils.isEmpty(rhs.getItemForIndex())){
			return -1;
		}
		Pattern pattern = Pattern.compile("^[^a-zA-Z]");
		Matcher matcher1 = pattern.matcher(lhs.getItemForIndex().substring(0, 1));
		Matcher matcher2 = pattern.matcher(rhs.getItemForIndex().substring(0, 1));
		
		if(matcher1.matches()&&matcher2.matches()){
			return lhs.getItemForIndex().compareTo(rhs.getItemForIndex());
		}else if(matcher1.matches()){//The letter to the end
			return 1;
		}else if(matcher2.matches()){//The letter to the end
			return -1;
		}
		return lhs.getItemForIndex().compareTo(rhs.getItemForIndex());
	}
}
