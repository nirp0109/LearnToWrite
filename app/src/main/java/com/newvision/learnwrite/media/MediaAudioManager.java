package com.newvision.learnwrite.media;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Queue;


import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;

public class MediaAudioManager {
	private MediaPlayer mediaPlayer = null;
	private MediaRecorder mediaRecorder = null;
	
	private String soundPath = null;
    private int soundResourceInt = -1;

    private int playButtonId = -1;
	private int recordButtonId = -1;
	private int stopPlaySoundResourceId = android.R.drawable.ic_media_pause;
	private int playSoundResourceId = android.R.drawable.ic_media_play;
	private int speakResourceId = android.R.drawable.ic_btn_speak_now;
	private int stopRecordResourceId = android.R.drawable.ic_media_pause;	
    private ArrayList<String> waitingSoundPaths = new ArrayList<String>();

	private Activity activity;
	
	public MediaAudioManager(Activity activity,int playButtonId,int recordButtonId ) {
		this.activity = activity;
		this.playButtonId = playButtonId;
		this.recordButtonId = recordButtonId;
	}

	public void stopAudio() {
		if(mediaPlayer != null) {

			mediaPlayer.release();
			mediaPlayer = null;
            if(playButtonId>0 && playSoundResourceId>0) {
                ImageButton imageButtonPlay = (ImageButton) activity.findViewById(playButtonId);
                imageButtonPlay.setImageResource(playSoundResourceId);
            }
		}
	}

	public void playAudio() {		
		
		if(soundPath!=null && soundPath.trim().length() > 0 || soundResourceInt>-1) {
            //if already playing ignore request
            if(mediaPlayer != null && mediaPlayer.isPlaying()) {

                return;
            }
			mediaPlayer = new MediaPlayer();
	        try {
                if(soundPath !=null) {
                    mediaPlayer.setDataSource(soundPath);
                } else {
                    String soundResource = activity.getResources().getResourceEntryName(soundResourceInt);
                    Uri mp3 = Uri.parse("android.resource://"
                            + activity.getPackageName() + "/raw/" +
                            soundResource.substring(soundResource.indexOf(":") + 1));

                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(activity, mp3);



                }
	        	//listen to complete of audio playing
	        	mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					
					@Override
					public void onCompletion(MediaPlayer mp) {

                        stopAudio();
                        if(waitingSoundPaths.size()>0) {
                            String soundPath = waitingSoundPaths.remove(0);
                            setSoundPath(soundPath);
                            playAudio();
                        }
						
					}
				});

	        	mediaPlayer.prepare();
	        	mediaPlayer.start();
                if(playButtonId>0) {
                    ImageButton imageButtonPlay = (ImageButton) activity.findViewById(playButtonId);

                    if (stopPlaySoundResourceId > 0) {
                        imageButtonPlay.setImageResource(stopPlaySoundResourceId);
                    }
                }

	        } catch (IOException e) {
	            Log.e("playAudio", "prepare() failed");
	        }
		}
		
	}

	public void stopRecord() {
		 mediaRecorder.stop();
		 mediaRecorder.release();
		 mediaRecorder = null;
		 ImageButton imageButtonRecord = (ImageButton)activity.findViewById(recordButtonId);
		 imageButtonRecord.setImageResource(speakResourceId);
		 ImageButton imageButtonPlay = (ImageButton)activity.findViewById(playButtonId);
		 imageButtonPlay.setEnabled(true);
		
	}

	public void recordAudio() {
		try {
			mediaRecorder = new MediaRecorder();
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			
			soundPath = null;
			
			StringBuilder builder = new StringBuilder();
			File externalFilesDir = activity.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
			//builder.append(Environment.getExternalStorageDirectory().getAbsolutePath());
			builder.append(externalFilesDir.getAbsolutePath());
			builder.append("/Sounds/");
            String soundsDirectory = builder.toString();

			builder.append(System.currentTimeMillis());
			builder.append(".3gp");
			
			soundPath = builder.toString();

            File file = new File(soundPath);
            if(!file.exists()) {
                try {
                    File dirFile = new File(soundsDirectory);
                    if(!dirFile.exists()) {
                        dirFile.mkdirs();
                    }
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
			mediaRecorder.setOutputFile(soundPath);
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			
			boolean prepareSuccess = false;
			try {
				mediaRecorder.prepare();
				prepareSuccess = true;
			} catch (IOException e) {
			    Log.e("Audio Record", "prepare() failed");
			}

			mediaRecorder.start();
			if(prepareSuccess) {
				ImageButton imageButtonRecord = (ImageButton)activity.findViewById(recordButtonId);
				imageButtonRecord.setImageResource(stopRecordResourceId);
				ImageButton imageButtonPlay = (ImageButton)activity.findViewById(playButtonId);
				imageButtonPlay.setEnabled(false);
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		
	}

	public String getSoundPath() {
		if(mediaRecorder==null) {
			return soundPath;
		} else 
		return null;
	}

	
	public synchronized void toggleRecord() {
		if(mediaRecorder == null) {
			recordAudio();
		} else {
			stopRecord();
		}
	}
	
	public synchronized void togglePlay() {
		if(mediaPlayer == null) {
			playAudio();
		} else {
			stopAudio();
		}
	}

	public int getStopPlaySoundResourceId() {
		return stopPlaySoundResourceId;
	}

	public void setStopPlaySoundResourceId(int stopPlaySoundResourceId) {
		this.stopPlaySoundResourceId = stopPlaySoundResourceId;
	}

	public int getPlaySoundResourceId() {
		return playSoundResourceId;
	}

	public void setPlaySoundResourceId(int playSoundResourceId) {
		this.playSoundResourceId = playSoundResourceId;
	}

	public int getSpeakResourceId() {
		return speakResourceId;
	}

	public void setSpeakResourceId(int speakResourceId) {
		this.speakResourceId = speakResourceId;
	}

	public int getStopRecordResourceId() {
		return stopRecordResourceId;
	}

	public void setStopRecordResourceId(int stopRecordResourceId) {
		this.stopRecordResourceId = stopRecordResourceId;
	}
	
	public void setSoundPath(String soundPath) {
        this.soundPath = soundPath;
        soundResourceInt = -1;
    }

    public void setSoundPath(int soundPath) {
        this.soundPath = null;
        this.soundResourceInt = soundPath;
    }

    public void stopAndPlay() {
        if(mediaPlayer!=null) {
            if (mediaPlayer.isPlaying()) {
                stopAudio();
                playAudio();
            } else {
                playAudio();
            }
        } else {
            playAudio();
        }

    }

    public void addPlayMusic(String soundPath) {
        if(mediaPlayer!=null) {
            if (mediaPlayer.isPlaying()) {
               this.waitingSoundPaths.add(soundPath);
            } else {
                setSoundPath(soundPath);
                playAudio();
            }
        } else {
            setSoundPath(soundPath);
            playAudio();
        }
    }

    public void clearWaitingSongs() {
        waitingSoundPaths.clear();
    }

}
