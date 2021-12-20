package com.newvision.learnwrite.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

	public DBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE ");
		sql.append(DBConstants.DB_SENTENCE_TABLE_NAME);
		sql.append("(").append(DBConstants.DB_COLUMN_ID_NAME)
				.append(" INTEGER PRIMARY KEY,");
		sql.append(DBConstants.DB_COLUMN_SENTENCE_NAME).append(" TEXT,");	
		sql.append(DBConstants.DB_COLUMN_FEEDBACK_NAME).append(" TEXT)");

		try {
			db.execSQL(sql.toString());
		} catch (SQLException e) {
			Log.d("DBHelper onCreate", e.getMessage());
		}

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
