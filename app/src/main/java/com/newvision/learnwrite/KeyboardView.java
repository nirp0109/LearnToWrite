package com.newvision.learnwrite;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.newvision.learnwrite.beans.Key;
import com.newvision.learnwrite.beans.Keyboard;
import com.newvision.learnwrite.beans.Row;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by nir on 11/03/2015.
 */
public class KeyboardView extends LinearLayout {
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
    public int actualTextViewWidth =0 ;
    public int actualTextViewHeight = 0;
    public float actualTextViewSize = 0;
    private Activity activity;


    public interface  FinishOnSize {
        public void refreshMe();
    }

    public OnTouchListener getOnTouchListener() {
        return onTouchListener;
    }

    @Override
    public void setOnTouchListener(OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }

    private OnTouchListener onTouchListener = null;


    public KeyboardView(Context context) {
        super(context);
    }

    public KeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Set the keyboard resource xml
     * @param resourceId
     */
    public void setKeyBoard(int resourceId,Activity activity) {
        loadKeyBoardFromResource(resourceId);
        this.activity = activity;

    }

    private void loadKeyBoardFromResource(int resourceId) {
        AssetManager assetManager = getResources().getAssets();

        XmlResourceParser xmlResourceParser = null;
        try {

            xmlResourceParser = getResources().getXml(resourceId);

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
            Log.d("load..Resource", e.getMessage());
        } catch (XmlPullParserException e) {
            Log.d( "load...Resource", e.getMessage());
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d("onSizeChanged","("+w+","+h+")");
        //draw keyboard
        int heightPixels = h;
        int widthPixels = w;

        double keyWidth =  (keyboard.getKeyWidth()  / 100d) * widthPixels;
        double keyHeight = (keyboard.getKeyHeight()  /100d) * heightPixels;


        LinearLayout linearLayout= this; //(LinearLayout)findViewById(R.id.keyboardLayout);
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        ArrayList<Row> rows = keyboard.getRows();
        ArrayList<TextView> textViews = new ArrayList<TextView>();
        for(int indexRow = 0; indexRow <rows.size(); indexRow++) {
            Row row = rows.get(indexRow);


            LinearLayout linearLayoutHorzintal = new LinearLayout(activity);


            linearLayoutHorzintal.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            linearLayoutHorzintal.setLayoutParams(params);

            linearLayout.addView(linearLayoutHorzintal);
            ArrayList<Key> keys = row.getKeys();

            for (int indexKey = 0 ; indexKey < keys.size(); indexKey++) {
                Key key = keys.get(indexKey);

                char label = key.getLabel();
                double horzintalGap = key.getHorzintalGap();
                TextView textView = new TextView(activity);

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
            if(onTouchListener!=null) {
                textView.setOnTouchListener(onTouchListener);
            }
        }
        ((FinishOnSize)activity).refreshMe();


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



    public Keyboard getKeyboard() {
        return keyboard;
    }
}
