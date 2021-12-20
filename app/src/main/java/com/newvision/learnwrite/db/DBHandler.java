package com.newvision.learnwrite.db;

import java.util.ArrayList;

import com.newvision.learnwrite.beans.Sentence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class DBHandler {

	private DBHelper helper;

	public DBHandler(Context act, int version) {
		helper = new DBHelper(act, DBConstants.DB_NAME, null, version);
	}

	public void addSentence(Sentence sentence) {

		SQLiteDatabase db = helper.getWritableDatabase();

		try {

			ContentValues values = new ContentValues();
			values.put(DBConstants.DB_COLUMN_SENTENCE_NAME, sentence.getSentence());
			values.put(DBConstants.DB_COLUMN_FEEDBACK_NAME, sentence.getFeedBack());

			db.insert(DBConstants.DB_SENTENCE_TABLE_NAME, null, values);
		} catch (SQLiteException e) {
			Log.d("DBHandler addNote",
					"insert sentence " + sentence + "+failed:" + e.getMessage());
		} finally {
			if (db.isOpen())
				db.close();
		}

	}

	public void deleteSentence(String id) {
		SQLiteDatabase db = helper.getWritableDatabase();

		try {

			String[] whereArgs = { id };

			db.delete(DBConstants.DB_SENTENCE_TABLE_NAME, "_id=?", whereArgs);

		} catch (SQLiteException e) {
			Log.d("DBHandler , deleteSentence","message:"+e.getMessage());
		} finally {
			if (db.isOpen())
				db.close();
		}

	}

	public void deleteAllSentences() {
		SQLiteDatabase db = helper.getWritableDatabase();

		try {

			db.delete(DBConstants.DB_SENTENCE_TABLE_NAME, null, null);

		} catch (SQLiteException e) {
			Log.d("DBHandler , deleteAllSentences","message:"+e.getMessage());
		} finally {
			if (db.isOpen())
				db.close();
		}

	}
	public void updateSentence(ContentValues values, String id) {
		SQLiteDatabase db = helper.getWritableDatabase();

		try {

			String[] whereArgs = { id };

			db.update(DBConstants.DB_SENTENCE_TABLE_NAME, values, "_id=?",
					whereArgs);

		} catch (SQLiteException e) {
			Log.d("DBHandler updateNote(ContentValues values, String id)","exception:"+e.getMessage());
		} finally {
			if (db.isOpen())
				db.close();
		}
	}

	public void updateSentence(Sentence sentence) {
		SQLiteDatabase db = helper.getWritableDatabase();

		try {

			String[] whereArgs = { String.valueOf(sentence.get_id()) };

			ContentValues values = new ContentValues();
			values.put(DBConstants.DB_COLUMN_SENTENCE_NAME, sentence.getSentence());
			values.put(DBConstants.DB_COLUMN_FEEDBACK_NAME, sentence.getFeedBack());
			
			db.update(DBConstants.DB_SENTENCE_TABLE_NAME, values, "_id=?",
					whereArgs);

		} catch (SQLiteException e) {
			Log.d("DBHandler updateSentence","exception:"+e.getMessage());
		} finally {
			if (db.isOpen())
				db.close();
		}
	}

	public Cursor getAllSentencesByCursor() {

		Cursor cursor = null;
		SQLiteDatabase db = helper.getReadableDatabase();
		try {

			cursor = db.query(DBConstants.DB_SENTENCE_TABLE_NAME, null, null, null,
					null, null, null);

		} catch (SQLiteException e) {
			Log.d("DBHandler getAllSentencesByCursor","exception:"+e.getMessage());
		}
		return cursor;
	}

	public ArrayList<Sentence> getAllSentences() {

		ArrayList<Sentence> list = new ArrayList<Sentence>();
		Cursor cursor = null;
		SQLiteDatabase db = helper.getReadableDatabase();
		try {

			cursor = db.query(DBConstants.DB_SENTENCE_TABLE_NAME, null, null, null,
					null, null, null);

		} catch (SQLiteException e) {
			Log.d("DBHandler getAllSentences","exception:"+e.getMessage());
		}

		addMultiSentencesToArrayListFromCursor(list, cursor);

		return list;
	}
	
	public Sentence getSentenceById(int id) {
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = null;
		try {
			String whereExpression = "_id=?";
			String[] whereArgs = {Integer.toString(id)}; 
			cursor = db.query(DBConstants.DB_SENTENCE_TABLE_NAME, null, whereExpression, whereArgs,
					null, null, null);
			if(cursor.moveToFirst()) {
	
				String sentenceText = cursor
						.getString(DBConstants.DB_COLUMN_SENTENCE_INDEX);
				String feedBcak = cursor.getString(DBConstants.DB_COLUMN_FEEDBACK_INDEX);
				

				Sentence sentence = new Sentence(sentenceText, feedBcak, id);
				return sentence;
			} else {
				return null;
			}

		} catch (SQLiteException e) {
			Log.d("DBHandler getSentenceById","exception:"+e.getMessage());
			return null;
		}
		
	}
	
	
	
	
	public static void addMultiSentencesToArrayListFromCursor(ArrayList<Sentence> list,	Cursor cursor) {
		while (cursor.moveToNext()) {

			Sentence note = createSentenceFromCursor(cursor);
			list.add(note);

		}
	}

	public static Sentence createSentenceFromCursor(Cursor cursor) {
		int id = cursor.getInt(DBConstants.DB_COLUMN_ID_INDEX);
		String sentenceStr = cursor
				.getString(DBConstants.DB_COLUMN_SENTENCE_INDEX);
		String feedback = cursor.getString(DBConstants.DB_COLUMN_FEEDBACK_INDEX);
		

		Sentence sentence = new Sentence(sentenceStr, feedback, id);
		return sentence;
	}

}
