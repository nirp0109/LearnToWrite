package com.newvision.learnwrite;

import com.newvision.learnwrite.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class HelpActivity extends Activity {

private static final String PAGE_SHOW="showHelpPage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_help);
        Intent intent = getIntent();
        String urlPage = intent.getStringExtra(PAGE_SHOW);
        if(urlPage!=null && urlPage.trim().length()>0) {
            WebView webView = (WebView)findViewById(R.id.webView);
            webView.loadUrl("file:///android_asset/"+urlPage);
        }

        // Set up an instance of SystemUiHider to control the system UI for

    }


}
