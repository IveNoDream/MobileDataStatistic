package com.mibound.datastatistic;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppItemDbHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "appitem_db";

	private static final String APP_TABLE_NAME = "appitem";
	private static final String APP_ID = "_id";
	private static final String APP_NAME = "name";
	private static final String APP_UID = "uid";
	private static final String APP_PACKAGE = "packagename";
	private static final String APP_START_TIME = "starttime";
	private static final String APP_STOP_TIME = "stoptime";
	private static final String APP_DATA_USAGE = "usage";

	private static final String[] APP_PROJECTION = new String[] { APP_ID,
			APP_NAME, APP_UID, APP_PACKAGE, APP_START_TIME, APP_STOP_TIME,
			APP_DATA_USAGE, };

	public AppItemDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String createRouteTableSql = "CREATE TABLE " + APP_TABLE_NAME + " ("
				+ APP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + APP_NAME
				+ " TEXT," + APP_UID + " TEXT," + APP_PACKAGE + " TEXT,"
				+ APP_START_TIME + " TEXT," + APP_STOP_TIME + " TEXT,"
				+ APP_DATA_USAGE + " TEXT" + ");";
		db.execSQL(createRouteTableSql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldversion, int newversion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + APP_TABLE_NAME);

		// Create tables again
		onCreate(db);
	}

	public int addAppItem(AppItem appItem) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(APP_NAME, appItem.getName());
		values.put(APP_UID, appItem.getUid());
		values.put(APP_PACKAGE, appItem.getPackagename());
		values.put(APP_START_TIME, appItem.getStarttime());
		values.put(APP_STOP_TIME, appItem.getStoptime());
		values.put(APP_DATA_USAGE, appItem.getUsage());

		// Insert to database
		long rowId = db.insert(APP_TABLE_NAME, null, values);

		// Close the database
		db.close();

		return (int) rowId;
	}

	public AppItem getAppItemByPackageName(String name) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(APP_TABLE_NAME, APP_PROJECTION, APP_PACKAGE
				+ "=?", new String[] { name }, null, null, null, null);

		if (cursor != null)
			cursor.moveToFirst();

		AppItem appItem = new AppItem(cursor.getInt(0), cursor.getString(1),
				cursor.getString(2), cursor.getString(3), cursor.getString(4),
				cursor.getString(5),cursor.getLong(6));

		db.close();
		cursor.close();

		return appItem;
	}

	public boolean isAppExist(String packagename) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(APP_TABLE_NAME, APP_PROJECTION, APP_PACKAGE
				+ "=?", new String[] { packagename }, null, null, null, null);

		if (cursor.getCount() > 0) {
			db.close();
			cursor.close();
			return true;
		} else {
			db.close();
			cursor.close();
			return false;
		}
	}

	public int getNewestAppItemId() {
		String sqlLine = "SELECT * FROM " + APP_TABLE_NAME;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(sqlLine, null);
		if (cursor.getCount() > 0) {
			cursor.moveToLast();
			int id = cursor.getInt(cursor.getColumnIndex(APP_ID));
			
			db.close();
			cursor.close();
			
			return id;
		} else {
			db.close();
			cursor.close();
			
			return -1;
		}
	}
	
	public List<AppItem> getAllAppItems() {
		List<AppItem> appItemList = new ArrayList<AppItem>();
		String selectQuery = "SELECT * FROM " + APP_TABLE_NAME;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				AppItem appItem = new AppItem(cursor.getInt(0),
						cursor.getString(1), cursor.getString(2),
						cursor.getString(3), cursor.getString(4),
						cursor.getString(5),cursor.getLong(6));
				appItemList.add(appItem);
			} while (cursor.moveToNext());
		}

		db.close();
		cursor.close();

		// return list
		return appItemList;
	}

	public int updateAppItem(AppItem appItem) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(APP_NAME, appItem.getName());
		values.put(APP_UID, appItem.getUid());
		values.put(APP_PACKAGE, appItem.getPackagename());
		values.put(APP_START_TIME, appItem.getStarttime());
		values.put(APP_STOP_TIME, appItem.getStoptime());
		values.put(APP_DATA_USAGE, appItem.getUsage());

		return db.update(APP_TABLE_NAME, values, APP_PACKAGE + "=?",
				new String[] { String.valueOf(appItem.getId()) });
	}

	public void deleteAppItemByName(String packagename) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(APP_TABLE_NAME, APP_PACKAGE + "=?",
				new String[] { packagename });
		db.close();
	}

	public void deleteAllAppItems() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(APP_TABLE_NAME, null, null);
		db.close();
	}

}
