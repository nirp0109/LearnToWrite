package com.newvision.learnwrite;


import com.newvision.learnwrite.adapters.SentenceCursorAdapter;
import com.newvision.learnwrite.db.DBConstants;
import com.newvision.learnwrite.db.DBHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Locale;

/**
 * Represent the main activity
 * @author nir
 *
 */
public class MainActivity extends Activity implements OnClickListener {

    public static final String LOCALE = "Locale" ;
    private SentenceCursorAdapter adapter = null;
	private DBHandler handler = null;
	private long sentenceId = -1;
	private boolean editMode;
	
	public static int EDIT_SENTENCE_REQUEST = 10;
	
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
       // testHebrew();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editMode = pref.getBoolean(Constants.EDIT_MODE, false);

        super.onCreate(savedInstanceState);
        Utils.updateApplicationWithLanguageFromSetting(this);
		setContentView(R.layout.main);
		//list view for sentences to pick up
		ListView lv = (ListView)findViewById(R.id.listViewSentences);
		//handler for DB operation
		handler = new DBHandler(getApplicationContext(), DBConstants.DB_VERSION);
		Cursor cursor = handler.getAllSentencesByCursor();

        if(editMode) {

        }
		lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				sentenceId = id;
				return false;
			}
		});
		
		adapter = new SentenceCursorAdapter(this, cursor, false);
		lv.setAdapter(adapter);


		ImageButton button = (ImageButton)findViewById(R.id.imageButtonAddSentence);
        button.setOnClickListener(this);
		//set button and list policy according to mode of edit
		setEditModePolice(lv, button);
		


	}

    private void testHebrew() {
        Locale[] availableLocales = Locale.getAvailableLocales();
        Locale locale = Locale.getDefault();
        for (int index=0;index<availableLocales.length;index++) {
            if(availableLocales[index].getLanguage().equals("iw")) {
                locale = availableLocales[index];
                Log.d("onCreate", "found langugae!!!!");
                break;
            } else {
                Log.d("onCreate","no found langugae :-(");
            }
        }
        Locale.setDefault(locale);
        Resources res = getResources();
        // Change locale settings in the app.
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
    }

    /**
	 * Set policy
	 * @param lv
	 * @param button
	 */
	private void setEditModePolice(ListView lv, ImageButton button) {
		if(editMode) {//if edit mode			
			button.setVisibility(View.VISIBLE);	//show button
			//list item press allow edit or delete
			lv.setOnItemClickListener(new OnItemClickListener() {
				
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position,
						long id) {
					sentenceId = id;
				}});

			registerForContextMenu(lv);
		} else {//not edit mode
			button.setVisibility(View.GONE);	//dont show add sentence button
            unregisterForContextMenu(lv);
			//list item press start the game, allow kid to make the sentence
			lv.setOnItemClickListener(new OnItemClickListener() {
				
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position,
						long id) {
					
					Intent intent = new Intent(MainActivity.this, SentenceWriteActivity.class);
					intent.putExtra(DBConstants.DB_COLUMN_ID_NAME, id);
					startActivity(intent);
				}
			});		
			
				
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == EDIT_SENTENCE_REQUEST && resultCode == RESULT_OK) {
			refreshSentences();
			Toast.makeText(this, R.string.sentence_added, Toast.LENGTH_SHORT).show();
		} else if (requestCode == EDIT_SENTENCE_REQUEST && resultCode == RESULT_CANCELED) {
			Toast.makeText(this, R.string.sentence_add_canceled, Toast.LENGTH_SHORT).show();
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
		
	}

	private void refreshSentences() {
		Cursor allSentencesByCursor = handler.getAllSentencesByCursor();
		if(adapter != null) {
			adapter.swapCursor(allSentencesByCursor);
			
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = new MenuInflater(this);
		menuInflater.inflate(R.menu.menu_options, menu);
        MenuItem item = menu.findItem(R.id.menu_option_edit_mode);
        if(item!=null) {
            if(editMode) {
                item.setTitle(getString(R.string.switch_to_play_mode));
            } else {
                item.setTitle(getString(R.string.switch_to_edit_mode));
            }
        }
        MenuItem menuItem = menu.findItem(R.id.menu_option_back);
        if(menuItem!=null) {
            menuItem.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		if(view.getId() == R.id.listViewSentences) {
			MenuInflater menuInflater = new MenuInflater(this);
			menuInflater.inflate(R.menu.menu_context_sentence, menu);
		}
		super.onCreateContextMenu(menu, view, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case R.id.menu_delete_sentence:
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
			alertDialog.setTitle(R.string.alert);
			alertDialog.setMessage(R.string.dialog_delete_confirm_message);
			alertDialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					handler.deleteSentence("" + sentenceId);
					Toast.makeText(MainActivity.this, R.string.sentence_removed,	Toast.LENGTH_SHORT).show();
					sentenceId = -1;
					refreshSentences();
					dialog.dismiss();
				}
			});
			alertDialog.setNegativeButton(R.string.not_confirm, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					
				}
			});
			alertDialog.create().show();
			break;
		case R.id.menu_edit_sentence:			
			Intent intent = new Intent(MainActivity.this, SentenceEditActivity.class);
			intent.putExtra(DBConstants.DB_COLUMN_ID_NAME, sentenceId);
			startActivityForResult(intent, EDIT_SENTENCE_REQUEST);			
			break;

		default:
			break;
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case R.id.menu_option_exit:
				finish();
				break;
			case R.id.menu_option_edit_mode:
				editMode = !editMode;
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				Editor editor = pref.edit();
				editor.putBoolean(Constants.EDIT_MODE, editMode);
				editor.commit();
				if(editMode) {
					item.setTitle(getString(R.string.switch_to_play_mode));
                    item.setIcon(getResources().getDrawable(android.R.drawable.ic_media_play));
				} else {
					item.setTitle(getString(R.string.switch_to_edit_mode));
                    item.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_edit));
				}
				ListView lv = (ListView)findViewById(R.id.listViewSentences);
				ImageButton button = (ImageButton)findViewById(R.id.imageButtonAddSentence);		
				//set button and list policy according to mode of edit
				setEditModePolice(lv, button);
				break;
            case R.id.menu_option_iw:
                setAndSaveLocale("iw");

                break;
            case R.id.menu_option_en:
                setAndSaveLocale("en");

                break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

    private void setAndSaveLocale(String locale) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Editor edit = sharedPreferences.edit();
        edit.putString(MainActivity.LOCALE,locale);
        edit.commit();

        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
	public void onClick(View v) {

		int id = v.getId();
		
		switch (id) {
			case R.id.imageButtonAddSentence:
				Intent intent = new Intent(MainActivity.this, SentenceEditActivity.class);			
				startActivityForResult(intent, EDIT_SENTENCE_REQUEST);				
				break;

			default:
				break;
		}
		
	}

}
