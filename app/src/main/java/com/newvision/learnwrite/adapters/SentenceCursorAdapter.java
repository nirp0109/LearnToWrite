package com.newvision.learnwrite.adapters;

import com.newvision.learnwrite.R;
import com.newvision.learnwrite.beans.Sentence;
import com.newvision.learnwrite.db.DBHandler;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
//import android.support.v4.widget.CursorAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class SentenceCursorAdapter extends CursorAdapter {
	Context context = null;
	public SentenceCursorAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		this.context = context;
	}

	public SentenceCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		this.context = context;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Sentence sentence = DBHandler.createSentenceFromCursor(cursor);
		PlaceSentenceHolder holder = (PlaceSentenceHolder)view.getTag(); 
		holder.textView.setText(sentence.getSentence());

	}

	@Override
	public View newView(Context context, Cursor c, ViewGroup parent) {
		LayoutInflater inflater = ((Activity)context).getLayoutInflater();
		View view = inflater.inflate(R.layout.row,parent,false);
		
		PlaceSentenceHolder placeSentenceHolder = new PlaceSentenceHolder();		
		placeSentenceHolder.textView = (TextView) view.findViewById(R.id.textViewRowSentence);
		
		view.setTag(placeSentenceHolder);
		return view;
	}
	
	private static class PlaceSentenceHolder {
		private TextView textView = null;
	}

}
