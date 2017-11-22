package com.lingtuan.firefly.custom;


import com.lingtuan.firefly.vo.CountryCodeVo;

import java.util.Comparator;

/**
 * Country code first letter ordering
 */
public class CountryCodeComparator implements Comparator<CountryCodeVo> {

	public int compare(CountryCodeVo o1, CountryCodeVo o2) {
		if (o1.getSortLetters().equals("@")
				|| o2.getSortLetters().equals("#")) {
			return -1;
		} else if (o1.getSortLetters().equals("#")
				|| o2.getSortLetters().equals("@")) {
			return 1;
		} else {
			return o1.getSortLetters().compareTo(o2.getSortLetters());
		}
	}

}
