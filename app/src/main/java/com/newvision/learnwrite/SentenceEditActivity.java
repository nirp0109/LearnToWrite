package com.newvision.learnwrite;

import com.google.android.material.snackbar.Snackbar;
import com.newvision.learnwrite.beans.Sentence;
import com.newvision.learnwrite.db.DBConstants;
import com.newvision.learnwrite.db.DBHandler;
import com.newvision.learnwrite.media.MediaAudioManager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;


public class SentenceEditActivity extends Activity implements OnClickListener {

	public static final int REQUEST_CODE_FOR_AUDIO_RECORD_AND_SAVE_EXTERNAL = 1;
	public static final int REQUEST_CODE_FOR_AUDIO_RECORD = 2;
	public static final int REQUEST_CODE_FOR_EXTERNAL_SAVE = 3;

	private String sentenceText = "";
	private String recordPath = "";
	private DBHandler handler = null;
	MediaAudioManager mediaManager = null;
	private View mLayout;
	
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit);
		mLayout = findViewById(R.id.imageButtonSpeak);

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
			int[] permissions = {REQUEST_CODE_FOR_AUDIO_RECORD, REQUEST_CODE_FOR_EXTERNAL_SAVE};
			if(permissionOk(permissions)) {
				mediaManager.toggleRecord();
				recordPath = mediaManager.getSoundPath();
				if (recordPath != null && recordPath.trim().length() > 0) {
					ImageButton playStopButton = (ImageButton) findViewById(R.id.imageButtonPlay);
					ImageButton saveButton = (ImageButton) findViewById(R.id.imageButtonSave);
					changeButtonStates(playStopButton, saveButton);
				}
			}

			break;
		case R.id.imageButtonPlay:
			mediaManager.togglePlay();
			break;
			
		case R.id.imageButtonSave:
			EditText editText =  (EditText)findViewById(R.id.editTextSentence);
			sentenceText = editText.getText().toString();
			if(sentenceText != null  && sentenceText.trim().length() > 0 && recordPath !=null && recordPath.trim().length() > 0) {
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

	private boolean permissionOk(int[] permissionCodes) {
		boolean[] permissionsBol = new boolean[permissionCodes.length];
		int index = -1;
		boolean needPermissions = false;
		ArrayList<String> permissionList = new ArrayList<>();
		for (int permission:permissionCodes) {
			index++;
			String permissionStr = "";
			int permissionCode = permissionCodes[index];
			if(permission == REQUEST_CODE_FOR_AUDIO_RECORD) {
				permissionStr = Manifest.permission.RECORD_AUDIO;
			} else {
				permissionStr = Manifest.permission.WRITE_EXTERNAL_STORAGE;
			}

			if (ActivityCompat.checkSelfPermission(
					this, permissionStr) ==
					PackageManager.PERMISSION_GRANTED) {
				// You can use the API that requires the permission.
				permissionsBol[index] = true;
			} else if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionStr)) {
				// In an educational UI, explain to the user why your app requires this
				// permission for a specific feature to behave as expected. In this UI,
				// include a "cancel" or "no thanks" button that allows the user to
				// continue using your app without granting the permission.
				//		showInContextUI(...);
				if(permissionCode == REQUEST_CODE_FOR_AUDIO_RECORD) {
					Snackbar.make(mLayout, R.string.audio_record_required,
							Snackbar.LENGTH_INDEFINITE).setAction(R.string.confirm, new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							// Request the permission
							ActivityCompat.requestPermissions(SentenceEditActivity.this,
									new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_FOR_AUDIO_RECORD);
						}
					}).show();
				} else {
					Snackbar.make(mLayout, R.string.extenal_write_required,
							Snackbar.LENGTH_INDEFINITE).setAction(R.string.confirm, new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							// Request the permission
							ActivityCompat.requestPermissions(SentenceEditActivity.this,
									new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_FOR_EXTERNAL_SAVE);
						}
					}).show();

				}
			} else {
				needPermissions = true;
				permissionList.add(permissionStr);
			}
		}
		if(needPermissions) {
			String[] permissionStrArray = new String[permissionList.size()];
			ActivityCompat.requestPermissions(this,
					permissionList.toArray(permissionStrArray),
					REQUEST_CODE_FOR_AUDIO_RECORD_AND_SAVE_EXTERNAL);
		}


		boolean result;
		result = permissionsBol[0];
		for (int indexBol=1;indexBol<permissionsBol.length;indexBol++) {
			result = result && permissionsBol[indexBol];
		}
		return result;

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

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
}
