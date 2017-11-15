package com.lingtuan.firefly.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;


import com.lingtuan.firefly.R;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class for annotating a CharSequence with spans to convert textual emoticons
 * to graphical ones.
 */
public class SmileyParser {
	// Singleton stuff
	private static SmileyParser sInstance;

	public static SmileyParser getInstance() {
		return sInstance;
	}

	public static int select = 0;
	public static Integer[] sIconIds;

	public static void init(Context context) {
		sInstance = new SmileyParser(context);
	}

	private final Context mContext;

	private final Pattern mPattern;
	private final HashMap<String, Integer> mSmileyToRes;
	private String[] mSmileyTexts;
	private SmileyParser(Context context) {
		mContext = context;

		mSmileyTexts = mContext.getResources().getStringArray(R.array.chat_smiley_list);
		sIconIds = Utils.getFaceListRes();
		mSmileyToRes = buildSmileyToRes();
		mPattern = buildPattern();
	}

	/**
	 * Builds the hashtable we use for mapping the string version of a smiley
	 * (e.g. ":-)") to a resource ID for the icon version.
	 */
	private HashMap<String, Integer> buildSmileyToRes() {
		if (sIconIds.length != mSmileyTexts.length) {
			// Throw an exception if someone updated DEFAULT_SMILEY_RES_IDS
			// and failed to update arrays.xml
			throw new IllegalStateException("Smiley resource ID/text mismatch");
		}
		HashMap<String, Integer> smileyToRes = new HashMap<String, Integer>(
				mSmileyTexts.length);
		for (int i = 0; i < mSmileyTexts.length; i++) {
			smileyToRes.put(mSmileyTexts[i], sIconIds[i]);
		}
		return smileyToRes;
	}

	/**
	 * Builds the regular expression we use to find smileys in
	 * {@link #addSmileySpans}.
	 */
	// Build a regular expression
	private Pattern buildPattern() {
		// Set the StringBuilder capacity with the assumption that the average
		// smiley is 3 characters long.
		StringBuilder patternString = new StringBuilder(
				mSmileyTexts.length * 3);
		// Build a regex that looks like (:-)|:-(|...), but escaping the smilies
		// properly so they will be interpreted literally by the regex matcher.
		patternString.append('(');
		for (String s : mSmileyTexts) {
			patternString.append(Pattern.quote(s));
			patternString.append('|');
		}
		// Replace the extra '|' with a ')'
		patternString.replace(patternString.length() - 1,
				patternString.length(), ")");
		return Pattern.compile(patternString.toString());
	}

	/**
	 * Adds ImageSpans to a CharSequence that replace textual emoticons such as
	 * :-) with a graphical version.
	 * 
	 * @param text
	 *            A CharSequence possibly containing emoticons
	 * @return A CharSequence annotated with ImageSpans covering any recognized
	 *         emoticons.
	 */
	// According to the textual substitution into images
	public CharSequence addSmileySpans(final CharSequence text) {
		if(TextUtils.isEmpty(text)) return "";

		SpannableStringBuilder builder = new SpannableStringBuilder(text);
		Matcher matcher = mPattern.matcher(text);
		while (matcher.find()) {//Annotation for the dynamic expression Now can only move a way of thinking is like this If you don't change it
			/*final AnimationDrawable mSmile = new AnimationDrawable();
			try {
				InputStream inputStream = mContext.getResources().getAssets().open(matcher.group().substring(1, matcher.group().length() -1) + ".png");
				Bitmap testmap = BitmapFactory.decodeStream(inputStream);
				int tileWidth = testmap.getHeight();
				int count = testmap.getWidth() / tileWidth;
				int x = 0, y = 0;

				for (int i = 0; i < count; i++) {
					mSmile.addFrame(new BitmapDrawable(BitmapClipBitmap(testmap, x + (i * tileWidth), y, tileWidth, tileWidth)), 200);
				}
				mSmile.setBounds(0, 0, tileWidth, tileWidth);
				mSmile.setOneShot(false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			int resId = mSmileyToRes.get(matcher.group());
			/*if(tv != null){
				builder.setSpan(new ImageSpan(mSmile, ImageSpan.ALIGN_BASELINE), matcher.start(), matcher.end(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				mSmile.invalidateSelf();
				new Thread(new Runnable() {
					@Override
					public void run() {
						byte mFrame = 0;
						while (true) {
							mSmile.selectDrawable(mFrame++);
							if (mFrame == mSmile.getNumberOfFrames()) {
								mFrame = 0;
							}
							tv.postInvalidate();
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}).start();
			} else*/
			Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), resId);
			float density = mContext.getResources().getDisplayMetrics().density;
			int w = (int)(30 * density);
			bitmap = Bitmap.createScaledBitmap(bitmap, w, w, true);
			builder.setSpan(new ImageSpan(mContext,bitmap, DynamicDrawableSpan.ALIGN_BOTTOM), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

//				builder.setSpan(new ImageSpan(mContext, resId,
//					ImageSpan.ALIGN_BOTTOM), matcher.start(), matcher.end(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return builder;

	}
	public CharSequence addSmileySpans1(final CharSequence text) {
		if(TextUtils.isEmpty(text)) return "";
		
		SpannableStringBuilder builder = new SpannableStringBuilder(text);
		Matcher matcher = mPattern.matcher(text);
		while (matcher.find()) {//Annotation for the dynamic expression Now can only move a way of thinking is like this If you don't change it
			int resId = mSmileyToRes.get(matcher.group());
			Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), resId);
			float density = mContext.getResources().getDisplayMetrics().density;
			int w = (int)(20 * density);
			bitmap = Bitmap.createScaledBitmap(bitmap, w, w, true);
			builder.setSpan(new ImageSpan(mContext,bitmap, DynamicDrawableSpan.ALIGN_BOTTOM), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		}
		return builder;
	}

	public CharSequence addSmileySpansResource(final CharSequence text) {
		if(TextUtils.isEmpty(text)) return "";

		SpannableStringBuilder builder = new SpannableStringBuilder(text);
		Matcher matcher = mPattern.matcher(text);
		while (matcher.find()) {//Annotation for the dynamic expression Now can only move a way of thinking is like this If you don't change it

			int resId = mSmileyToRes.get(matcher.group());
			Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), resId);
			float density = mContext.getResources().getDisplayMetrics().density;
			int w = (int)(16 * density);
			bitmap = Bitmap.createScaledBitmap(bitmap, w, w, true);
			builder.setSpan(new ImageSpan(mContext,bitmap, DynamicDrawableSpan.ALIGN_BOTTOM), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

//				builder.setSpan(new ImageSpan(mContext, resId,
//					ImageSpan.ALIGN_BOTTOM), matcher.start(), matcher.end(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return builder;
	}
	public CharSequence addSmileySpansLocal(final CharSequence text, String logoPath) {
		if(TextUtils.isEmpty(text)) return "";

		Bitmap bitmap = BitmapFactory.decodeFile(logoPath);
		SpannableStringBuilder builder = new SpannableStringBuilder(text);
		float density = mContext.getResources().getDisplayMetrics().density;
		int w = (int)(16 * density);
		if(bitmap!=null)
		{
			bitmap = Bitmap.createScaledBitmap(bitmap, w, w, true);
			builder.setSpan(new ImageSpan(mContext,bitmap, DynamicDrawableSpan.ALIGN_BOTTOM), text.length()-4, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return builder;
	}

	public CharSequence addSmileySpansLocalRunway(final CharSequence text, String logoPath) {
		if(TextUtils.isEmpty(text)) return "";

		Bitmap bitmap = BitmapFactory.decodeFile(logoPath);
		SpannableStringBuilder builder = new SpannableStringBuilder(text);
		float density = mContext.getResources().getDisplayMetrics().density;
		int w = (int)(50 * density);
		if(bitmap!=null)
		{
			bitmap = Bitmap.createScaledBitmap(bitmap, w, w, true);
			builder.setSpan(new ImageSpan(mContext,bitmap, DynamicDrawableSpan.ALIGN_BOTTOM), text.length()-4, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return builder;
	}
	/**
	 * Application of cutting pictures
	 * 
	 * @param bitmap
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return
	 */
	public Bitmap BitmapClipBitmap(Bitmap bitmap, int x, int y, int w, int h) {
		return Bitmap.createBitmap(bitmap, x, y, w, h);
	}
}