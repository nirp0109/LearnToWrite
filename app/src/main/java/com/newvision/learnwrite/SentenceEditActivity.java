package com.newvision.learnwrite;

import com.newvision.learnwrite.beans.Sentence;
import com.newvision.learnwrite.db.DBConstants;
import com.newvision.learnwrite.db.DBHandler;
import com.newvision.learnwrite.media.MediaAudioManager;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.concurrent.CancellationException;

import javax.xml.transform.OutputKeys;

public class SentenceEditActivity extends Activity implements OnClickListener {
	
	private String sentenceText = "";
	private String recordPath = "";
	private DBHandler handler = null;
	MediaAudioManager mediaManager = null;
	
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit);
		
		sentenceText = null;
		recordPath = null;
		handler = new DBHandler(getApplicationContext(), DBConstants.DB_VERSION);
		mediaManager = new MediaAudioManager(this, R.id.imageButtonPlay, R.id.imageButtonSpeak);
		mediaManager.setStopRecordResourceId(R.drawable.ic_stop);
        mediaManager.setStopPlaySoundResourceId(R.drawable.ic_stop);

		Intent intent = getIntent();
		long id = intent.getLongExtra(DBConstants.DB_COLUMN_ID_NAME, -1);
		if(id != -1) {		
			loadFromDB(id);
		}
		
		EditText sentence = (EditText)findViewById(R.id.editTextSentence);
		sentence.setText(sentenceText);
		ImageButton recordButton = (ImageButton)findViewById(R.id.imageButtonSpeak);
		ImageButton playStopButton = (ImageButton)findViewById(R.id.imageButtonPlay);
		ImageButton saveButton = (ImageButton)findViewById(R.id.imageButtonSave);
		
		changeButtonStates(playStopButton, saveButton);
		
		recordButton.setOnClickListener(this);
		playStopButton.setOnClickListener(this);
		saveButton.setOnClickListener(this);
	}

	private void changeButtonStates(ImageButton playStopButton,	ImageButton saveButton) {
		EditText editText =  (EditText)findViewById(R.id.editTextSentence);
		sentenceText = editText.getText().toString();
		if(sentenceText!=null && recordPath!=null && sentenceText.trim().length() > 0 && recordPath.trim().length() >0) {
			saveButton.setEnabled(true);
			playStopButton.setEnabled(true);
		} else {
			saveButton.setEnabled(false);
			playStopButton.setEnabled(false);
		}
	}

	private void loadFromDB(long id) {
		Sentence sentence = handler.getSentenceById((int)id);
		sentenceText = sentence.getSentence();
		recordPath = sentence.getFeedBack();
		mediaManager.setSoundPath(recordPath);
		
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch (id) {
		case R.id.imageButtonSpeak:
			mediaManager.toggleRecord();
			recordPath = mediaManager.getSoundPath();
			if(recordPath!=null && recordPath.trim().length()>0) {
				ImageButton playStopButton = (ImageButton)findViewById(R.id.imageButtonPlay);
				ImageButton saveButton = (ImageButton)findViewById(R.id.imageButtonSave);
				changeButtonStates(playStopButton, saveButton);
			}
			break;
		case R.id.imageButtonPlay:
			mediaManager.togglePlay();
			break;
			
		case R.id.imageButtonSave:
			EditText editText =  (EditText)findViewById(R.id.editTextSentence);
			sentenceText = editText.getText().toString();
			if(sentenceText != null && recordPath !=null && sentenceText.trim().length() > 0 && recordPath.trim().length() > 0) {
				Sentence sentence = new Sentence(sentenceText, recordPath);
				handler.addSentence(sentence);
				setResult(RESULT_OK);
				finish();
			} else {
				Toast.makeText(this, R.string.save_prerequist, Toast.LENGTH_LONG).show();
			}
			break;
		default:
			break;
		}
		
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
                setResult(RESULT_CANCELED);
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
