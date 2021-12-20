package com.newvision.learnwrite.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;



/**
 * Created by nir on 10/04/2015.
 */
public class TextViewEffects extends TextView implements Runnable{

    private Handler handler = new Handler();
    private int frameRefresh = 50;
    private int[] colors = {Color.RED, Color.GREEN,Color.BLUE};
    int colorIndex = 0;
    int count = 0;
    private boolean sucess = false;

    public TextViewEffects(Context context) {
        super(context);
    }

    public TextViewEffects(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextViewEffects(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(sucess) {
            float measuredHeight = getMeasuredHeight();
            float measuredWidth = getMeasuredWidth();
            float widthStep = measuredWidth / 9f;
            float heightStep = measuredHeight / 9f;
            for (int i = 0; i < 9; i++) {
                TextPaint paint = getPaint();
                paint.setColor(colors[(i + count) % colors.length]);
                canvas.drawRect(i * widthStep, 0, (i + 1) * widthStep, 10, paint);
                paint.setColor(colors[(8 + i + count) % colors.length]);
                canvas.drawRect(measuredWidth - 10, i * heightStep, measuredWidth, (i + 1) * heightStep, paint);
                paint.setColor(colors[(i +1+ count) % colors.length]);
                canvas.drawRect((measuredWidth - (i + 1) * widthStep), measuredHeight - 10,(measuredWidth - i * widthStep) , measuredHeight, paint);
                paint.setColor(colors[(i + count) % colors.length]);
                canvas.drawRect(0,(measuredHeight -(i + 1) * heightStep) , 10, (measuredHeight - i * heightStep), paint);
            }
        }

    }

    @Override
    public void run() {
        sucess = true;
        if(count++<20) {
            invalidate();
            handler.postDelayed(this,frameRefresh);
        }
    }
}
