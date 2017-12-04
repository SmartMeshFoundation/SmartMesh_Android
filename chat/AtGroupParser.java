package com.lingtuan.firefly.chat;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class for annotating a CharSequence with spans to convert textual emoticons
 * to graphical ones.
 */
public class AtGroupParser {
	// Singleton stuff
	private static AtGroupParser sInstance;

	public static AtGroupParser getInstance() {
	      return sInstance;
	}

	public static int select = 0;
	public String[] ids;

	public static void init(String[] nicknames, String[] ids) {
	     sInstance = new AtGroupParser(nicknames,ids);
	}

	private final Pattern mPattern;
	private final HashMap<String, String> mSmileyToRes;
	public String[] nicknames;
	private AtGroupParser(String[] nicknames, String[] ids) {
		this.nicknames = nicknames;
		this.ids = ids;
		mSmileyToRes = buildSmileyToRes();
		mPattern = buildPattern();
	}

	public void addParse(String nickName, String id){
		if(sInstance != null){
			boolean hasId = false;
			for (int i = 0; i < ids.length; i++) {
				if(TextUtils.equals(ids[i], id)){
					hasId = true;
					break;
				}
			}
			if(!hasId){
				String[] _nickName = new String[nicknames.length + 1];
				String[] _ids = new String[ids.length + 1];
				for (int i = 0; i < _ids.length; i++) {
					if(i == 0){
						_nickName[i] = nickName;
						_ids[i] = id;
					}else{
						_nickName[i] = nicknames[i - 1];
						_ids[i] = ids[i - 1];
					}
				}
				nicknames = _nickName;
				ids = _ids;
			}
		}else{
			init(new String[]{nickName}, new String[]{id});
		}
	}
	
	/**
	 * Builds the hashtable we use for mapping the string version of a smiley
	 * (e.g. ":-)") to a resource ID for the icon version.
	 */
	private HashMap<String, String> buildSmileyToRes() {
		if (ids.length != nicknames.length) {
			// Throw an exception if someone updated DEFAULT_SMILEY_RES_IDS
			// and failed to update arrays.xml
			throw new IllegalStateException("Smiley resource ID/text mismatch");
		}
		HashMap<String, String> smileyToRes = new HashMap<String, String>(
				nicknames.length);
		for (int i = 0; i < nicknames.length; i++) {
			smileyToRes.put(nicknames[i], ids[i]);
		}
		return smileyToRes;
	}

	/**
	 * Builds the regular expression we use to find smileys in
	 */
	// Build a regular expression
	private Pattern buildPattern() {
		// Set the StringBuilder capacity with the assumption that the average
		// smiley is 3 characters long.
		StringBuilder patternString = new StringBuilder(nicknames.length * 3);
		// Build a regex that looks like (:-)|:-(|...), but escaping the smilies
		// properly so they will be interpreted literally by the regex matcher.
		patternString.append('(');
		for (String s : nicknames) {
			patternString.append(Pattern.quote(s));
			patternString.append('|');
		}
		// Replace the extra '|' with a ')'
		patternString.replace(patternString.length() - 1,patternString.length(), ")");
		return Pattern.compile(patternString.toString());
	}

	/**
	 * Adds ImageSpans to a CharSequence that replace textual emoticons such as
	 * :-) with a graphical version.
	 * 
	 * @param text A CharSequence possibly containing emoticons
	 * @return A CharSequence annotated with ImageSpans covering any recognized emoticons.
	 */
	public String parser(final CharSequence text) {
		StringBuffer sb = new StringBuffer();
		if(TextUtils.isEmpty(text) || !text.toString().contains("@")) return sb.toString();
		Matcher matcher = mPattern.matcher(text);
		while (matcher.find()) {
			sb.append(mSmileyToRes.get(matcher.group())).append(",");
		}
		if(sb.length() != 0){
			sb.delete(sb.lastIndexOf(","), sb.length());
		}
		return sb.toString();
	}
}
