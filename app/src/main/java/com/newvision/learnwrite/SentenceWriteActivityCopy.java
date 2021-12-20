package com.newvision.learnwrite;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.newvision.learnwrite.beans.Key;
import com.newvision.learnwrite.beans.Keyboard;
import com.newvision.learnwrite.beans.Row;
import com.newvision.learnwrite.beans.Sentence;
import com.newvision.learnwrite.db.DBConstants;
import com.newvision.learnwrite.db.DBHandler;
import com.newvision.learnwrite.media.MediaAudioManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class SentenceWriteActivityCopy extends Activity {
    public static final String KEY_HEIGHT = "keyHeight";
    private final static String LOG_TAG = "SentenceWriteActivity";

    private final static int KEYBOARD_PHASE = 1;
    private final static int ROW_PHASE = 2;
    private final static int KEY_PHASE = 3;
    public static final String KEYBOARD = "Keyboard";
    public static final String ROW = "Row";
    public static final String KEY = "Key";
    public static final String KEY_WIDTH = "keyWidth";
    public static final String LABEL = "Label";
    public static final String KEY_EDGE_FLAGS = "keyEdgeFlags";

    private Keyboard keyboard = null;
    private String sentence = null;

    private int actualTextViewWidth = 0;
    private int actualTextViewHeight = 0;
    private float actualTextViewSize = 0;
    private MyDragListener myDragListener = null;
    private MyTouchListener myTouchListener = null;
    private String[] particles;
    private char[] allSentence = null;
    private  MediaAudioManager mediaAudioManager = new MediaAudioManager(this,0,0);


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
        updateKeyBoardView();
        DBHandler handler = new DBHandler(this,DBConstants.DB_VERSION);
        Intent intent = getIntent();
        long recordID = intent.getLongExtra(DBConstants.DB_COLUMN_ID_NAME, -1);
        if(recordID>-1) {
            Sentence sentenceById = handler.getSentenceById((int) recordID);
            String sentenceFromIntent = sentenceById.getSentence();
            mediaAudioManager.setSoundPath(sentenceById.getFeedBack());

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
            int spaces = particles.length - 1;
            int totalSpaceDPI = 2 * spaces;
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int widthPixels = displayMetrics.widthPixels;
            int heightPixels = displayMetrics.heightPixels;
            float xdpi = displayMetrics.xdpi;
            float spaceDPIUnit = totalSpaceDPI * (xdpi / 160);



            fillSentenceLayout.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

            LinearLayout linearLayoutHorzLinearLayout = new LinearLayout(this);
            fillSentenceLayout.addView(linearLayoutHorzLinearLayout);

            linearLayoutHorzLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            linearLayoutHorzLinearLayout.setLayoutParams(layoutParams);
            int partcleStart,particleEnd,particleStep,charCounter;
            if(Utils.isLTR()) {
                partcleStart = 0;
                particleEnd = particles.length;
                particleStep =1 ;
                charCounter = -1;
            } else {
                partcleStart = particles.length -1;
                particleEnd = 0;
                particleStep = -1;
                charCounter = countCharacters;
            }
            int counter = 0;


            for (int particleIndex=partcleStart; check(particleIndex,particleEnd,particleStep); particleIndex+=particleStep) {
                String particle = particles[particleIndex];
                TextView lastTextView = null;
                int charStart,charEnd,charStep;
                if(Utils.isLTR()) {
                    charStart = 0;
                    charEnd = particle.length();
                    charStep =1 ;
                } else {
                    charStart = particle.length() -1;
                    charEnd = 0;
                    charStep = -1;
                }
                counter = counter + particle.length();
                if(counter>10) {
                    counter = particle.length();
                    linearLayoutHorzLinearLayout = new LinearLayout(this);
                    if(particleStep>0) {
                        fillSentenceLayout.addView(linearLayoutHorzLinearLayout);
                    } else {
                        fillSentenceLayout.addView(linearLayoutHorzLinearLayout, 0);
                    }


                    linearLayoutHorzLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                    layoutParams.topMargin =2;
                    layoutParams.bottomMargin = 2;
                    linearLayoutHorzLinearLayout.setLayoutParams(layoutParams);


                }
                for (int charIndex=charStart;check(charIndex,charEnd,charStep);charIndex+=charStep) {
                    TextView textView = new TextView(this);
                    textView.setWidth(actualTextViewWidth);
                    textView.setHeight(actualTextViewHeight);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, actualTextViewSize);
                    textView.setBackgroundColor(Color.GRAY);
                    textView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                    textView.setOnDragListener(myDragListener);
                    linearLayoutHorzLinearLayout.addView(textView);
                        LinearLayout.LayoutParams textViewLayoutParams = (LinearLayout.LayoutParams)textView.getLayoutParams();
                        textViewLayoutParams.rightMargin = 2;
                    charCounter +=charStep;
                    textView.setId(charCounter);
                   //  textView.setText(""+ charCounter);
                 textView.setText(""+allSentence[charCounter]);
                    if(Utils.isLTR()) {
                        if (charIndex == 0) {
                            textViewLayoutParams.leftMargin = 12;
                        }
                    } else {
                        if (charIndex == charEnd) {
                            textViewLayoutParams.rightMargin = 12;
                        }
                    }
                }


            }

        }


    }

    private boolean check(int charIndex, int charEnd, int charStep) {
        if(charStep>0) {
            return charIndex<charEnd;
        } else {
            return charIndex>=charEnd;
        }
    }

    private void updateKeyBoardView() {
        createKeyboard();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels = displayMetrics.widthPixels;

        double keyWidth =  (keyboard.getKeyWidth()  / 100d) * widthPixels;
        double keyHeight = (keyboard.getKeyHeight()  /100d) * heightPixels;


        LinearLayout linearLayout= (LinearLayout)findViewById(R.id.keyboardLayout);
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        ArrayList<Row> rows = keyboard.getRows();
        ArrayList<TextView> textViews = new ArrayList<TextView>();
        for(int indexRow = 0; indexRow <rows.size(); indexRow++) {
            Row row = rows.get(indexRow);


            LinearLayout linearLayoutHorzintal = new LinearLayout(this);


            linearLayoutHorzintal.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            linearLayoutHorzintal.setLayoutParams(params);

            linearLayout.addView(linearLayoutHorzintal);
            ArrayList<Key> keys = row.getKeys();

            for (int indexKey = 0 ; indexKey < keys.size(); indexKey++) {
                Key key = keys.get(indexKey);

                char label = key.getLabel();
                double horzintalGap = key.getHorzintalGap();
                TextView textView = new TextView(this);

                textView.setWidth((int)keyWidth);
                actualTextViewWidth = (int)keyWidth;
                textView.setHeight((int)keyHeight);

                textView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                textView.setText(""+label);

                textView.setId(indexRow * rows.size() + indexKey);
                linearLayoutHorzintal.addView(textView);
                TextPaint paint = textView.getPaint();
                paint.setSubpixelText(true);

                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,1.0f);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                int leftMargin = 0;
                if(key.getHorzintalGap() >0 && indexKey == 0) {
                  leftMargin = (int)(key.getHorzintalGap() * Math.min(heightPixels,widthPixels)/ 100d)   ;
                }
                if(indexRow<rows.size()) {
                    params.setMargins(0, 0, 0, 2);
                    layoutParams.leftMargin = leftMargin;


                }
                textView.setLayoutParams(layoutParams);
                textViews.add(textView);
                textView.setBackgroundResource(R.drawable.gadient);
            }



        }

        float min = 1.0f;
        float max = 1.0f;
        float check = min;
        boolean flag = false;
        for(int iterationNumber=0;iterationNumber<12;iterationNumber++) {
            flag = false;
            for (int index=0;index<textViews.size();index++) {
                TextView textView = textViews.get(index);
                TextPaint paint = textView.getPaint();
                paint.setSubpixelText(true);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, check);
                int i = paint.breakText(textView.getText().toString().toCharArray(), 0, 1, (float) (Math.min(keyWidth,keyHeight) ), null);
                if (i == 0) {
                    flag = true;
                }
            }
            if(!flag) {
                if(check == max) {
                    max = check * 2;
                    min = check;
                    check = max;
                } else {
                    min = check;
                    check = (max + min) / 2;
                }
            } else {
                if(check == max) {
                    check = (min + max) /2 ;
                } else {
                    max = check;
                    check = (max + min)/2;
                }

            }

        }
        actualTextViewSize = min;
        for (int index=0;index<textViews.size();index++) {
            TextView textView = textViews.get(index);
            TextPaint paint = textView.getPaint();
            paint.setSubpixelText(true);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, min);
            Paint.FontMetrics fontMetrics = paint.getFontMetrics();
            actualTextViewHeight = (int)(fontMetrics.bottom - fontMetrics.top);
            textView.setHeight(actualTextViewHeight);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)textView.getLayoutParams();
            layoutParams.rightMargin = 2;
            textView.setOnTouchListener(myTouchListener);


        }
    }

    private void createKeyboard() {
        AssetManager assetManager = getResources().getAssets();

        XmlResourceParser xmlResourceParser = null;
        try {

            xmlResourceParser = getResources().getXml(R.xml.keyboard);

            keyboard = null;
            int eventType = xmlResourceParser.getEventType();
            Row row = null;
            Key key = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_TAG) {
                    String tagName = xmlResourceParser.getName();
                    if(KEYBOARD.equals(tagName)) {
                        keyboard = new Keyboard();

                        int attributeCount = xmlResourceParser.getAttributeCount();
                        for (int attributexIndex = 0; attributexIndex < attributeCount ; attributexIndex++) {
                            String attributeName = xmlResourceParser.getAttributeName(attributexIndex);
                            double parseDouble = 0;
                            String testAttribute = KEY_WIDTH;
                            if(attributeName.indexOf(testAttribute) >= 0) {
                                parseDouble = extractKeyDimension(xmlResourceParser, attributexIndex, attributeName, testAttribute);
                                keyboard.setKeyWidth(parseDouble);
                            }
                            testAttribute = KEY_HEIGHT;
                            if(attributeName.indexOf(testAttribute) >= 0) {
                                parseDouble = extractKeyDimension(xmlResourceParser, attributexIndex, attributeName, testAttribute);
                                keyboard.setKeyHeight(parseDouble);
                            }

                        }
                    } else if(ROW.equals(tagName)) {
                        row = new Row();

                    } else if(KEY.equals(tagName)) {

                         key = new Key();
                        int attributeCount = xmlResourceParser.getAttributeCount();
                        for (int attributexIndex = 0; attributexIndex < attributeCount ; attributexIndex++) {
                            String attributeName = xmlResourceParser.getAttributeName(attributexIndex);
                            double parseDouble = 0;
                            String testAttribute = LABEL;
                            if(attributeName.indexOf(testAttribute) >= 0) {
                                String attributeValue = xmlResourceParser.getAttributeValue(attributexIndex);
                                key.setLabel(attributeValue.charAt(0));
                            }
                            testAttribute = KEY_EDGE_FLAGS;
                            if(attributeName.indexOf(testAttribute) >= 0) {
                                String attributeValue = xmlResourceParser.getAttributeValue(attributexIndex);
                                if("left".equalsIgnoreCase(attributeValue)) {
                                    key.setEdge(Key.LEFT_EDGE);
                                } else  if("right".equalsIgnoreCase(attributeValue)) {
                                    key.setEdge(Key.RIGHT_EDGE);
                                } else {
                                    key.setEdge(Key.NO_EDGE);
                                }
                            }
                            testAttribute = "horizontalGap";
                          if(attributeName.indexOf(testAttribute) >= 0) {
                                parseDouble = extractKeyDimension(xmlResourceParser, attributexIndex, attributeName, testAttribute);
                                key.setHorzintalGap(parseDouble);
                            }
                        }

                    }
                } else if(eventType == XmlPullParser.END_TAG) {
                    String tagName = xmlResourceParser.getName();
                    if(ROW.equalsIgnoreCase(tagName)) {
                        keyboard.getRows().add(row);
                    } else if(KEY.equalsIgnoreCase(tagName)) {
                        row.getKeys().add(key);
                    }
                }
                eventType =  xmlResourceParser.next();
            }

        } catch (IOException e) {
            Log.d(LOG_TAG + " onCreate", e.getMessage());
        } catch (XmlPullParserException e) {
            Log.d(LOG_TAG + " onCreate", e.getMessage());
        }
    }

    private double extractKeyDimension(XmlResourceParser xmlResourceParser, int attributexIndex, String attributeName, String testAttribute) {
        double parseDouble = 0;

            String attributeValue = xmlResourceParser.getAttributeValue(attributexIndex);
            int percentageOffset = attributeValue.indexOf("%");
            if(percentageOffset>0) {
                 parseDouble = Double.parseDouble(attributeValue.substring(0, percentageOffset));
            } else {
                parseDouble = Double.parseDouble(attributeValue);
            }

        return parseDouble;
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
              TextView container = (TextView) v;
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
                              allSentence[textViewID] = view.getText().charAt(0);
                          }
                      } else {
                          Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
                          if(vibrator!=null && vibrator.hasVibrator()) {
                              vibrator.vibrate(500);
                          }

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
        boolean complete = true;
        if(allSentence!=null && allSentence.length>1) {
            for (int index = 0; index < allSentence.length; index++) {
                if (allSentence[index] == ' ') {
                    complete = false;
                }
            }
            if (complete) {

                mediaAudioManager.playAudio();
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
