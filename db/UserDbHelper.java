package com.lingtuan.firefly.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDbHelper extends SQLiteOpenHelper {

	public static final String DB_NAME = "user.db";
	public static final int DB_VERSION = 0;
	//---------------------- The user information
	public static final String TABLE_USER_NAME = "userinfo";
	public static final String FIELD_USER_ID = "_id";
	public static final String FIELD_USER_UID = "uid";
	public static final String FIELD_USER_USERNAME = "uname";
	public static final String FIELD_USER_ACCOUNTID = "accountid";
	public static final String FIELD_USER_NICKNAME = "nickname";
	public static final String FIELD_USER_SEX = "sex";
	public static final String FIELD_USER_AVATAR_IMG = "avatar";
	public static final String FIELD_USER_MONEY = "money";
	public static final String FIELD_USER_TOKENCOIN = "tokencoin";
	public static final String FIELD_USER_VIPID = "vipId";
	public static final String FIELD_USER_VIPLEVEL = "viplevel";
	public static final String FIELD_USER_CARID = "carId";
	public static final String FIELD_USER_DATA = "data";//password
	public static final String FIELD_USER_DATA1 = "data1";//token
	public static final String FIELD_USER_OPENID = "ipenid";
	public static final String FIELD_USER_THIRD = "third";
	
	
	public UserDbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}


	@Override
	public void onCreate(SQLiteDatabase db) {
		createTable(db);
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		dropTable(db);
		createTable(db);
	}
	private void createTable(SQLiteDatabase db) {
		
		String sql_userinfo = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_USER_NAME + "( "
				+ FIELD_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ FIELD_USER_UID + " text,"
				+ FIELD_USER_USERNAME + " text,"
				+ FIELD_USER_ACCOUNTID + " text,"
				+ FIELD_USER_NICKNAME + " text,"
				+ FIELD_USER_SEX + " text,"
				+ FIELD_USER_AVATAR_IMG + " text,"
				+ FIELD_USER_MONEY + " text,"
				+ FIELD_USER_TOKENCOIN + " text,"
				+ FIELD_USER_VIPID + " text,"
				+ FIELD_USER_VIPLEVEL + " text,"
				+ FIELD_USER_CARID + " text,"
				+ FIELD_USER_OPENID + " text,"
				+ FIELD_USER_THIRD + " text,"
				+ FIELD_USER_DATA + " text,"
				+ FIELD_USER_DATA1 + " text"
				+ ")";
		db.execSQL(sql_userinfo);
	}
	
	private void dropTable(SQLiteDatabase db){
		String sql_userinfo = "DROP TABLE IF EXISTS " + TABLE_USER_NAME;
		db.execSQL(sql_userinfo);
	}
}
