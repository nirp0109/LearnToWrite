package com.newvision.learnwrite;


import android.app.Activity;

import android.content.ClipData;
import android.content.Intent;

import android.graphics.Color;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.newvision.learnwrite.view.TextViewEffects;

import com.newvision.learnwrite.beans.Sentence;
import com.newvision.learnwrite.db.DBConstants;
import com.newvision.learnwrite.db.DBHandler;
import com.newvision.learnwrite.media.MediaAudioManager;

public class SentenceWriteActivity extends Activity implements  KeyboardView.FinishOnSize{
    private String sentence = null;


    private MyDragListener myDragListener = null;
    private MyTouchListener myTouchListener = null;
    private String[] particles;
    private char[] allSentence = null;
    private  MediaAudioManager mediaAudioManager = new MediaAudioManager(this,0,0);
    private KeyboardView keyBoardView = null;
    private String finishSentenceAudioFilPath = null;
    private  boolean complete = false;

    public void refreshMe() {
        DBHandler handler = new DBHandler(this,DBConstants.DB_VERSION);
        Intent intent = getIntent();
        long recordID = intent.getLongExtra(DBConstants.DB_COLUMN_ID_NAME, -1);
        if(recordID>-1) {
            Sentence sentenceById = handler.getSentenceById((int) recordID);
            String sentenceFromIntent = sentenceById.getSentence();
            finishSentenceAudioFilPath = sentenceById.getFeedBack();


            sentence = sentenceFromIntent.trim();
            particles = sentence.split(" ");
            int countCharacters = 0;
            for (int i=0; i<particles.length;i++) {
                countCharacters += particles[i].length();
            }
            if(allSentence==null) {
                allSentence = new char[countCharacters];
                for (int i = 0; i < countCharacters; i++) {
                    allSentence[i] = ' ';
                }
            }

            LinearLayout fillSentenceLayout = (LinearLayout)findViewById(R.id.fillSentenceLayout);
            boolean isLTR = Utils.isLTR();

            fillSentenceLayout.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

            LinearLayout linearLayoutHorzLinearLayout = new LinearLayout(this);
            fillSentenceLayout.addView(linearLayoutHorzLinearLayout);

            linearLayoutHorzLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            linearLayoutHorzLinearLayout.setLayoutParams(layoutParams);
            int charCounter=-1;
            int counter = 0;

            for (int particleIndex=0; particleIndex<particles.length; particleIndex++) {
                String particle = particles[particleIndex];

                counter = counter + particle.length();
                if(counter>10) {
                    counter = particle.length();
                    linearLayoutHorzLinearLayout = new LinearLayout(this);
                    fillSentenceLayout.addView(linearLayoutHorzLinearLayout);
                    linearLayoutHorzLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                    layoutParams.topMargin =2;
                    layoutParams.bottomMargin = 2;
                    linearLayoutHorzLinearLayout.setLayoutParams(layoutParams);
                 }
                for (int charIndex=0;charIndex<particle.length();charIndex++) {
                    TextViewEffects textView = new TextViewEffects(this);
                    textView.setWidth(keyBoardView.actualTextViewWidth);
                    textView.setHeight(keyBoardView.actualTextViewHeight);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, keyBoardView.actualTextViewSize);
                    textView.setBackgroundColor(Color.GRAY);
                    textView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                    textView.setOnDragListener(myDragListener);
                    if(isLTR) {
                        linearLayoutHorzLinearLayout.addView(textView);
                    } else {
                        linearLayoutHorzLinearLayout.addView(textView,0);
                    }
                    LinearLayout.LayoutParams textViewLayoutParams = (LinearLayout.LayoutParams)textView.getLayoutParams();
                    textViewLayoutParams.rightMargin = 2;
                    charCounter ++;
                    textView.setId(charCounter);
                    //  textView.setText(""+ charCounter);
                    textView.setText(""+allSentence[charCounter]);
                    if(Utils.isLTR()) {
                        if (charIndex == 0) {
                            textViewLayoutParams.leftMargin = 12;
                        }
                    } else {
                        if (charIndex == particle.length()-1) {
                            textViewLayoutParams.leftMargin = 12;
                        }
                    }
                }

            }

        }//finish if

    }


    @Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);

		setContentView(R.layout.write);
        if(savedInstanceState!=null) {
            String data = savedInstanceState.getString("data");
            if(data!=null && data.trim().length()>0) {
                allSentence = new char[data.length()];
                for (int index=0;index<allSentence.length;index++) {
                    allSentence[index] = data.charAt(index);
                }
            }
        }
        myDragListener = new MyDragListener();
        myTouchListener = new MyTouchListener();

        keyBoardView = (KeyboardView)findViewById(R.id.keyboardLayout);
        keyBoardView.setOnTouchListener(myTouchListener);
        keyBoardView.setKeyBoard(R.xml.keyboard,this);
        mediaAudioManager.clearWaitingSongs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.menu_options, menu);
        MenuItem item = menu.findItem(R.id.menu_option_edit_mode);
        if(item!=null) {
           item.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.menu_option_exit:
                finish();
                break;
            case R.id.menu_option_help:
                //  openOptionsMenu();
                break;
            case R.id.menu_option_back:

                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private  class MyTouchListener implements View.OnTouchListener {
	    public boolean onTouch(View view, MotionEvent motionEvent) {

	      if (motionEvent.getAction() == MotionEvent.ACTION_DOWN ) {
	    	//  List<Key> keys = mKeyboardView.getk.getKeys();

	    	 Log.d("MyTouchListener", "down");
	        ClipData data = ClipData.newPlainText("", "");
	        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
	        view.startDrag(data, shadowBuilder, view, 0);
              Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
              if(vibrator!=null && vibrator.hasVibrator()) {
                  vibrator.vibrate(80);
              }

	        //view.setVisibility(View.INVISIBLE);
	        return true;
	      } else {
	        return false;
	      }
	    }
	  }

	private class MyDragListener implements View.OnDragListener {



	    @Override
	    public boolean onDrag(View v, DragEvent event) {
	      int action = event.getAction();
	      switch (action) {
	      case DragEvent.ACTION_DRAG_STARTED:
	        // do nothing
              return true;

	      case DragEvent.ACTION_DRAG_ENTERED:

	        return true;
	      case DragEvent.ACTION_DRAG_EXITED:

	        return true;

	      case DragEvent.ACTION_DROP:
	        // Dropped, reassign View to ViewGroup
              TextView view = (TextView) event.getLocalState();
	        ViewGroup owner = (ViewGroup) view.getParent();
	        //owner.removeView(view);
              TextViewEffects container = (TextViewEffects) v;
              int textViewID = container.getId();
              int counter=0;
              int lastCounter =0;
              for(int index=0;index<particles.length;index++) {
                  counter += particles[index].length();
                  if(textViewID<counter) {
                      int offset = textViewID - lastCounter;
                      if(Character.toUpperCase(particles[index].charAt(offset)) == Character.toUpperCase(view.getText().charAt(0))){
                          if(container.getText().toString().trim().length() ==0) {
                              container.setText(view.getText());
                              container.run();
                              allSentence[textViewID] = view.getText().charAt(0);

                              mediaAudioManager.setSoundPath(R.raw.appaluse_sound);
                              mediaAudioManager.stopAndPlay();

                          }
                      } else {
                          Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
                          if(vibrator!=null && vibrator.hasVibrator()) {
                              vibrator.vibrate(500);
                          }
                          mediaAudioManager.setSoundPath(R.raw.moomoo);
                          mediaAudioManager.stopAndPlay();

                      }
                      break;
                  } else {
                      lastCounter = counter;
                  }
              }

	        view.setVisibility(View.VISIBLE);
	        return true;
	      case DragEvent.ACTION_DRAG_ENDED:
                testIfComplete();
                return true;
	      default:
	        break;
	      }
	      return true;
	    }
	  }

    private void testIfComplete() {

        if(complete) {
            return;
        }
        complete = true;
        if(allSentence!=null && allSentence.length>1) {
            for (int index = 0; index < allSentence.length; index++) {
                if (allSentence[index] == ' ') {
                    complete = false;
                }
            }
            if (complete) {
                LinearLayout fillSentenceLayout = (LinearLayout)findViewById(R.id.fillSentenceLayout);
                Drawable drawable = getResources().getDrawable(R.drawable.background);

                //  drawable.setBounds(0,0,fillSentenceLayout.getMeasuredWidth(),fillSentenceLayout.getMeasuredHeight()/2);
                drawable.setAlpha(100);
                LinearLayout parent =(LinearLayout) fillSentenceLayout.getParent().getParent();
                parent.setBackgroundDrawable(drawable);
                mediaAudioManager.addPlayMusic(finishSentenceAudioFilPath);

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String text=new String(allSentence);
        outState.putString("data",text);

    }
}
