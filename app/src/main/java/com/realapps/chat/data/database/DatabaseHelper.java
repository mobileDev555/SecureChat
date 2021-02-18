package com.realapps.chat.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.realapps.chat.ui.ui.view.java.model.Historymodel;

import java.util.ArrayList;


public class DatabaseHelper {
	public static final String DATABASE_NAME = "sipchat.db";
	public static final int DATABASE_VERSION = 1;
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
	Historymodel user;
	ArrayList<Historymodel> history_model_list = new ArrayList<>();
	//private static final String clog = "CREATE TABLE if not exists calllog (id INTEGER PRIMARY KEY AUTOINCREMENT,call_num TEXT,call_type CHAR(2),call_time TEXT,call_duration TEXT)";
	private static final String TABLE_CALL_HISTORY = "CREATE TABLE if not exists callHistory (id INTEGER PRIMARY KEY AUTOINCREMENT,calldate TEXT,dialnumber TEXT,duration TEXT,callstatus TEXT,country TEXT,debit TEXT,calltype TEXT,uniqueid TEXT)";


	public DatabaseHelper(Context c) {
		this.context = c;
		Log.v("dbhelp","context0");
		if (db == null || !db.isOpen()) {
			Log.v("dbhelp","context1");
			dbOpenHelper = new OpenHelper(context);
			db = dbOpenHelper.getWritableDatabase();
			/*Log.v("dbhelp","context2");
			dynamic_table = PreferenceManager.getDefaultSharedPreferences(
					context).getString(Settings.PREF_DYNAMIC_TABLE + "",
					Settings.DEFAULT_DYNAMIC_TABLE);
			contact = "CREATE TABLE if not exists '"
					+ dynamic_table
					+ "' (id INTEGER PRIMARY KEY AUTOINCREMENT,contact_name TEXT,contact_num TEXT,fav TEXT)";
		db.execSQL(contact);*/
			db.execSQL(TABLE_CALL_HISTORY);

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

			db.execSQL(TABLE_CALL_HISTORY);
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
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALL_HISTORY);
			onCreate(db);
		}
	}


	public long insert_call_history(String calldate, String dialnumber, String duration, String callstatus,
                                    String country, String debit, String calltype,
                                    String uniqueid) {
		// get writable database as we want to write data

		ContentValues values = new ContentValues();
		// `id` and `timestamp` will be inserted automatically.
		// no need to add them

		values.put("calldate", calldate);
		values.put("dialnumber", dialnumber);
		values.put("duration", duration);
		values.put("callstatus", callstatus);
		values.put("country", country);
		values.put("debit", debit);
		values.put("calltype", calltype);
		values.put("uniqueid", uniqueid);

		// insert row
		long id = db.insert(" callHistory ", null, values);
		//long id = db.insertWithOnConflict(StationDetails.TABLE_NAME, null, values,SQLiteDatabase.CONFLICT_REPLACE);
		//System.out.println("New Instered id" + id);
		// close db connection


		// return newly inserted row id
		return id;
	}


	public int getCallHistoryCount() {
		String countQuery = "SELECT  * FROM " + " callHistory ";

		Cursor cursor = db.rawQuery(countQuery, null);

		int count = cursor.getCount();
		cursor.close();


		// return count
		return count;
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

	public ArrayList<Historymodel> getCallHistoryList() {

		String calldate, dialnumber,duration, callstatus,country,debit,calltype,uniqueid;
		Cursor cursor = fetchcalllog("SELECT  calldate,dialnumber,duration,callstatus,country,debit,calltype,uniqueid FROM callHistory ");

		if (cursor != null) {
			if (cursor.getCount() == 0) {

			} else if (cursor.getCount() > 0) {

				while (cursor.moveToNext()) {

					try {

						calldate = cursor.getString(0);
						dialnumber = cursor.getString(1);
						duration = cursor.getString(2);
						callstatus = cursor.getString(3);
						country = cursor.getString(4);
						debit = cursor.getString(5);
						calltype = cursor.getString(6);
						uniqueid = cursor.getString(7);

						user =new Historymodel();
						user.setCallednum(dialnumber);
						user.setCost(debit);
						user.setCalldate(calldate);
						user.setCalltype(calltype);
						user.setCallstatus(callstatus);
						user.setBillseconds(duration);
						user.setCountry(country);
						user.setUniqueId(uniqueid);

						history_model_list.add(user);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}


			}


		}
		return history_model_list;
	}


	public void deleteCallHistory() {

		db.execSQL("delete from " + " callHistory ");

	}

    public void dropCallLogTable() {

       // db.execSQL("DROP TABLE if exists " + " callHistory ");
        db.execSQL("delete from "+ " callHistory ");


    }


    public void deleteRecord(String uniqueId_str){
	    /*db.execSQL("DELETE FROM callHistory WHERE uniqueid equals " + uniqueId_str );
        long id = db.delete(" callHistory ", "", values);*/
        long id =  db.delete(" callHistory ", "uniqueid" + " = ?", new String[] { uniqueId_str });


    }

}
