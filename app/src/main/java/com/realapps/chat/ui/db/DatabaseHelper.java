package com.realapps.chat.ui.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.realapps.chat.ui.api.SipMessage;
import com.realapps.chat.ui.ui.view.java.model.Settings;


public class DatabaseHelper {
	public static final String DATABASE_NAME = "sipchat.db";
	public static final int DATABASE_VERSION = 2;
	public String KEY_DATE_TIME = "last_chat_time";
	public String KEY_CALL_TIME = "call_time";
	private static SQLiteDatabase db;
	private Context context;
	private static final String CALL_LOG_TABLE = "calllog";
	public OpenHelper dbOpenHelper;
	public static String dynamic_table = "";
	public static String fdynamic_table = "";
	private static String contact = "";
	private static String fcontact = "";

	private static final String activechat = "CREATE TABLE if not exists active_chat (id INTEGER PRIMARY KEY AUTOINCREMENT, c_number TEXT,status INTEGER)";
	private static final String chathistory = "CREATE TABLE if not exists chat_history (id INTEGER PRIMARY KEY AUTOINCREMENT,reciver TEXT,message TEXT,type TEXT,cdate CHAR(25))";
	private static final String ofmsg = "CREATE TABLE if not exists of_msg (id INTEGER PRIMARY KEY AUTOINCREMENT,r_number TEXT,msg INTEGER,last_chat_time TEXT)";
	private static final String clog = "CREATE TABLE if not exists calllog (id INTEGER PRIMARY KEY AUTOINCREMENT,call_num TEXT,call_type CHAR(2),call_time TEXT,call_duration TEXT)";
	private static final String country = "CREATE TABLE if not exists country_code (id INTEGER PRIMARY KEY AUTOINCREMENT,strcode TEXT,intcode TEXT)";
	private static final String offlinemessage = "CREATE TABLE if not exists offlinemessage (id INTEGER PRIMARY KEY AUTOINCREMENT,msgto TEXT,msg TEXT,str TEXT,flag TEXT)";
	
	private final static String TABLE_CURRENCY_CREATE = "CREATE TABLE IF NOT EXISTS "
			+ SipMessage.CURRENCY_TABLE_NAME
			+ " ("
				+ SipMessage.FIELD_ID			+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ SipMessage.FIELD_CURR_CODE	+ " TEXT,"
				+ SipMessage.FIELD_CURR_NAME	+ " TEXT"
				+");";
	
	private final static String TABLE_COUNTRY_CREATE = "CREATE TABLE IF NOT EXISTS "
			+ SipMessage.COUNTRY_TABLE_NAME
			+ " ("
				+ SipMessage.FIELD_ID			+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ SipMessage.FIELD_COUNTRY_ID	+ " TEXT,"
				+ SipMessage.FIELD_COUNTRY_NAME	+ " TEXT,"
				+ SipMessage.FIELD_COUNTRY_CODE	+ " TEXT,"
				+ SipMessage.FIELD_COUNTRY_ISO + " TEXT"
				+");";
	
	public DatabaseHelper(Context c) {
		this.context = c;
		Log.v("dbhelp","context0");
		if (db == null || !db.isOpen()) {
			Log.v("dbhelp","context1");
			dbOpenHelper = new OpenHelper(context);
			db = dbOpenHelper.getWritableDatabase();
			Log.v("dbhelp","context2");
			dynamic_table = PreferenceManager.getDefaultSharedPreferences(
					context).getString(Settings.PREF_DYNAMIC_TABLE + "",
					Settings.DEFAULT_DYNAMIC_TABLE);
			contact = "CREATE TABLE if not exists '"
					+ dynamic_table
					+ "' (id INTEGER PRIMARY KEY AUTOINCREMENT,contact_name TEXT,contact_num TEXT,fav TEXT)";
		db.execSQL(contact);
		Log.v("dbhelp","context3");
			
		}
		// updatedata(contact);

	}

	public DatabaseHelper open() throws SQLException {
		 Log.v("dbhelp","open");
		/*
		 * if (db != null) { db.close(); }
		 */
		if (!db.isOpen()) {
			 Log.v("dbhelp"," not open");
			dbOpenHelper = new OpenHelper(context);
			db = dbOpenHelper.getWritableDatabase();
		}
		// Log.v("dbhelp","in open");
		db.execSQL(activechat);
		db.execSQL(chathistory);
		db.execSQL(ofmsg);
		db.execSQL(clog);
		db.execSQL(contact);
	
		db.execSQL(country);
		db.execSQL(offlinemessage);
		db.execSQL(TABLE_CURRENCY_CREATE);
		db.execSQL(TABLE_COUNTRY_CREATE);
		// Log.v("dbhelp","in last open");
		return this;
	}

	public void reopen() {

		/*
		 * if(db.isOpen()) close();
		 */
		if (!db.isOpen()) {
			dbOpenHelper = new OpenHelper(context);
			db = dbOpenHelper.getWritableDatabase();
		}
		// dbOpenHelper.close();

	}

	public void close() {
		if (dbOpenHelper != null)
			dbOpenHelper.close();
		if (db != null)
			db.close();
	}

	private static class OpenHelper extends SQLiteOpenHelper {

		public OpenHelper(Context context) {

			super(context, DATABASE_NAME, null, DATABASE_VERSION);

		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// Log.v("dbhelp","oncreate");

			db.execSQL(activechat);
			db.execSQL(chathistory);
			db.execSQL(ofmsg);
			db.execSQL(clog);
			//db.execSQL(contact);
			db.execSQL(country);
			db.execSQL(offlinemessage);
			db.execSQL(TABLE_CURRENCY_CREATE);
			db.execSQL(TABLE_COUNTRY_CREATE);
			// Log.v("dbhelp","last oncreate");
		}

		@Override
		public synchronized void close() {
			if (db != null) {
				db.close();
				super.close();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + activechat);
			db.execSQL("DROP TABLE IF EXISTS " + chathistory);
			db.execSQL("DROP TABLE IF EXISTS " + ofmsg);
			db.execSQL("DROP TABLE IF EXISTS " + CALL_LOG_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + contact);

			db.execSQL("DROP TABLE IF EXISTS " + country);
			db.execSQL("DROP TABLE IF EXISTS " + offlinemessage);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_CURRENCY_CREATE);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_COUNTRY_CREATE);
			onCreate(db);
		}
	}

	public long insertcalllog(String num, char type, String ctime,
			String duration) {
		// reopen();
		ContentValues CV = new ContentValues();

		CV.put("call_num", num);
		CV.put("call_type", Character.toString(type));
		CV.put("call_time", ctime);
		CV.put("call_duration", duration);

		long rawId = db.insert(CALL_LOG_TABLE, null, CV);
		// close();

		return rawId;
	}

	public Cursor fetchAllmessage() {
		// reopen();
		Cursor cursor = db.query("of_msg", null, null, null, null, null,
				KEY_DATE_TIME + " DESC");
		/*
		 * if (cursor != null && !cursor.isClosed()) { cursor.close(); }
		 */
		// close();

		return cursor;
	}

	public Cursor fetchdata(String tablename, String value) {
		// reopen();
		Cursor cursor = db
				.query(tablename, null, value, null, null, null, null);
		/*
		 * if (cursor != null && !cursor.isClosed()) { cursor.close(); }
		 */
		// close();

		return cursor;
	}

	public boolean updatedata(String que) {
		// reopen();
		String updata = que;
		db.execSQL(updata);
		// if(db.isOpen())
		// close();

		return true;
	}

	public Cursor fetchcalllog(String str2) {
		// Log.v("dbhelper",str2);
		// reopen();
		String str = str2;
		Cursor cursor = db.rawQuery(str, null);
		/*
		 * if (cursor != null && !cursor.isClosed()) { cursor.close(); }
		 */
		// close();

		return cursor;
	}
	/**
	 * to insert the all currency
	 */
	public long insertCurrency(String curr_name, String curr_code) {
		// reopen();
		ContentValues CV = new ContentValues();

		CV.put(SipMessage.FIELD_CURR_NAME, curr_name);
		CV.put(SipMessage.FIELD_CURR_CODE, curr_code);

		long rawId = db.insert(SipMessage.CURRENCY_TABLE_NAME, null, CV);
		// close();

		return rawId;
	}
	/**
	 * TO get all country data
	 */
	
	public Cursor getAllDatFromCountry(String str2) {
		
		String str = str2;
		Cursor cursor = db.rawQuery(str, null);		

		return cursor;
	}
	/**
	 * to insert the all country
	 */
	public long insertCountry(String country_id, String country_name, String country_code, String country_iso) {
		// reopen();
		ContentValues CV = new ContentValues();
		CV.put(SipMessage.FIELD_COUNTRY_ID, country_id);
		CV.put(SipMessage.FIELD_COUNTRY_NAME, country_name);
		CV.put(SipMessage.FIELD_COUNTRY_CODE, country_code);
		CV.put(SipMessage.FIELD_COUNTRY_ISO, country_iso);

		long rawId = db.insert(SipMessage.COUNTRY_TABLE_NAME, null, CV);
		// close();

		return rawId;
	}
	/**
	 * TO get all country data
	 */
	
	public Cursor getAllDatFromCurrency(String str2) {
		
		String str = str2;
		Cursor cursor = db.rawQuery(str, null);		

		return cursor;
	}
	
	public String getCountryCodeFromCountryId(String countryId) {
		String country_code = "";
		String str = "select "+SipMessage.FIELD_COUNTRY_CODE+" from "+SipMessage.COUNTRY_TABLE_NAME + " WHERE "+ SipMessage.FIELD_COUNTRY_ID +" ='"+countryId+"'";
		Cursor cursor = db.rawQuery(str, null);
		if (cursor.getCount() > 0) {			
			while (cursor.moveToNext()) {
				country_code = cursor.getString(0);
			}
		}
		return country_code;
	}

}
