package com.adarshhasija.flows;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class EditAudioActivity extends ActionBarActivity implements View.OnClickListener {

    private static final String LOG_TAG = "EditAudioActivity";

    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private File tempFile=null;
    private FileOutputStream fos=null;
    private FileInputStream fis=null;

    private ParseObject flowParseObject;
    private ParseFile parseFile;

    private MenuItem saveButton;
    private ProgressBar progressBarSave;
    private Chronometer chronometerRecord;
    private ImageView imageViewMic;
    private TextView textViewInstruction;
    private ImageView imageViewStop;
    private ImageView imageViewPlay;
    private ProgressBar progressBar;
    private Chronometer chronometerElapsedTime;
    private Chronometer chronometerTotalTime;
    private Button buttonResetDefault;

    private boolean isSaving=false;
    private boolean recordPressed=false;
    private boolean playPressed=false;
    private boolean savePressed=false;
    private int timeDiff=0;


    int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format

    private void startRecording() {
        recordPressed=true;

        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
        }
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        //mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            fos = new FileOutputStream(tempFile);
            mRecorder.setOutputFile(fos.getFD());
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();

        chronometerRecord.setVisibility(View.VISIBLE);
        imageViewMic.setVisibility(View.INVISIBLE);
        textViewInstruction.setVisibility(View.INVISIBLE);
        imageViewStop.setVisibility(View.VISIBLE);
        imageViewPlay.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        chronometerElapsedTime.setVisibility(View.INVISIBLE);
        chronometerTotalTime.setVisibility(View.INVISIBLE);
        buttonResetDefault.setVisibility(View.INVISIBLE);
        saveButton.setVisible(false);

        chronometerRecord.setBase(SystemClock.elapsedRealtime());
        chronometerRecord.setTextColor(Color.BLACK);
        chronometerRecord.start();
        chronometerRecord.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {

                int currentTime = (int) (SystemClock.elapsedRealtime() - chronometerRecord.getBase())/1000;
                if (currentTime == 10) {
                    stopRecording();
                }
                else if (currentTime > 7) {
                    chronometerRecord.setTextColor(Color.RED);
                }

            }
        });

        //File file = new File(mFileName);
        //if (file != null) {
            //boolean deleted = file.delete();
        //}
    }

    private void stopRecording() {
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;

        chronometerRecord.stop();
        chronometerTotalTime.setBase(chronometerRecord.getBase());
        chronometerElapsedTime.setBase(SystemClock.elapsedRealtime());
        progressBar.setProgress(0);
        timeDiff = (int) (SystemClock.elapsedRealtime() - chronometerTotalTime.getBase())/1000;
        recordPressed = false;
        chronometerRecord.setVisibility(View.VISIBLE);
        imageViewMic.setVisibility(View.VISIBLE);
        textViewInstruction.setVisibility(View.VISIBLE);
        imageViewStop.setVisibility(View.INVISIBLE);
        imageViewPlay.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        chronometerElapsedTime.setVisibility(View.VISIBLE);
        chronometerTotalTime.setVisibility(View.VISIBLE);
        buttonResetDefault.setVisibility(View.VISIBLE);
        saveButton.setVisible(true);
    }

    private void startPlaying() {
        playPressed=true;
        chronometerRecord.setVisibility(View.INVISIBLE);
        imageViewMic.setVisibility(View.INVISIBLE);
        textViewInstruction.setVisibility(View.INVISIBLE);
        imageViewStop.setVisibility(View.VISIBLE);
        imageViewPlay.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        chronometerElapsedTime.setVisibility(View.VISIBLE);
        chronometerTotalTime.setVisibility(View.VISIBLE);
        buttonResetDefault.setVisibility(View.INVISIBLE);
        saveButton.setVisible(false);

        chronometerElapsedTime.setBase(SystemClock.elapsedRealtime());
        progressBar.setProgress(0);
        chronometerElapsedTime.start();
        chronometerElapsedTime.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {

                int diff = (int) (progressBar.getMax() / timeDiff);
                int newProgress = progressBar.getProgress() + diff;
                progressBar.setProgress(newProgress);
                if ((progressBar.getProgress() + diff) > progressBar.getMax()) {
                    progressBar.setProgress(100);
                    stopPlaying();
                }

            }
        });

        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }
        try {
            fis = new FileInputStream(tempFile);
            mPlayer.setDataSource(fis.getFD());
            //mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;

        chronometerElapsedTime.stop();
        chronometerElapsedTime.setOnChronometerTickListener(null);
        playPressed = false;
        chronometerRecord.setVisibility(View.VISIBLE);
        imageViewMic.setVisibility(View.VISIBLE);
        textViewInstruction.setVisibility(View.VISIBLE);
        imageViewStop.setVisibility(View.INVISIBLE);
        imageViewPlay.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        chronometerElapsedTime.setVisibility(View.VISIBLE);
        chronometerTotalTime.setVisibility(View.VISIBLE);
        buttonResetDefault.setVisibility(View.VISIBLE);
        saveButton.setVisible(true);
    }

    private void saveAndExit() {
        savePressed = true;
        Intent intent = new Intent();
        intent.putExtra("audioFile", tempFile);
        setResult(RESULT_OK, intent);
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_edit);

        progressBarSave = (ProgressBar) findViewById(R.id.progressBarSave);
        chronometerRecord = (Chronometer) findViewById(R.id.chronometerRecord);
        imageViewMic = (ImageView) findViewById(R.id.imageViewMic);
        textViewInstruction = (TextView) findViewById(R.id.textViewInstruction);
        imageViewStop = (ImageView) findViewById(R.id.imageViewStop);
        imageViewPlay = (ImageView) findViewById(R.id.imageViewPlay);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        chronometerElapsedTime = (Chronometer) findViewById(R.id.chronometerElapsedTime);
        chronometerTotalTime = (Chronometer) findViewById(R.id.chronometerTotalTime);
        buttonResetDefault = (Button) findViewById(R.id.buttonResetDefault);
        imageViewMic.setOnClickListener(this);
        imageViewStop.setOnClickListener(this);
        imageViewPlay.setOnClickListener(this);
        buttonResetDefault.setOnClickListener(this);

        progressBarSave.setVisibility(View.GONE);
        textViewInstruction.setVisibility(View.VISIBLE);
        imageViewStop.setVisibility(View.INVISIBLE);
        imageViewPlay.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        chronometerElapsedTime.setVisibility(View.INVISIBLE);
        chronometerTotalTime.setVisibility(View.INVISIBLE);
        buttonResetDefault.setVisibility(View.INVISIBLE);

        Bundle extras = getIntent().getExtras();
        String id = extras.getString("id");
        ParseQuery query = ParseQuery.getQuery("Item");
        query.fromLocalDatastore();
        try {
            flowParseObject = query.get(id);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (flowParseObject.getParseFile("audio") != null) {
            buttonResetDefault.setVisibility(View.VISIBLE);
        }
            try {
                tempFile = File.createTempFile(flowParseObject.getObjectId(),"3gp");
            } catch (IOException e) {
                e.printStackTrace();
            }
      
    }

    @Override
    public void onDestroy() {
     //   if (!savePressed) {
            if (tempFile != null) {
                tempFile.delete();
            }
     //   }

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_audio_edit, menu);

        saveButton = (MenuItem) menu.findItem(R.id.action_save);
        //saveButton.setVisible(false);

        return true;
    }

    private void saveAudio(File audioFile) {
        int size = (int) audioFile.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(audioFile));
            buf.read(bytes, 0, bytes.length);
            buf.close();
            parseFile = new ParseFile(flowParseObject.getObjectId()+".3gp", bytes);
            //try {
            parseFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        flowParseObject.put("audio", parseFile);
                        flowParseObject.saveEventually();
                        flowParseObject.pinInBackground("Item", new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                Intent intent = new Intent();
                                Bundle bundle = new Bundle();
                                bundle.putString("id", flowParseObject.getObjectId());
                                intent.putExtras(bundle);
                                setResult(Activity.RESULT_OK, intent);
                                finish();
                            }
                        });
                    }
                    else {
                        e.printStackTrace();
                    }
                }
            }, new ProgressCallback() {
                @Override
                public void done(Integer integer) {
                    progressBarSave.setProgress(integer);
                }
            });
            //} catch (ParseException e) {
            //    e.printStackTrace();
            //}
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void isSaving() {
        isSaving = true;
        saveButton.setIcon(R.drawable.ic_action_cancel);
        progressBarSave.setVisibility(View.VISIBLE);
        progressBarSave.setProgress(0);
        setTitle("Saving...");
        if (tempFile != null) {
            saveAudio(tempFile);
        }
    }

    private void isNotSaving() {
        isSaving = false;
        saveButton.setIcon(R.drawable.ic_action_save);
        progressBarSave.setVisibility(View.GONE);
        setTitle(R.string.title_activity_audio_record);
        if (parseFile != null) parseFile.cancel();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_save) {
            if (!isSaving) {
                isSaving();
            }
            else {
                isNotSaving();
            }
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (buttonResetDefault == v) {
            isNotSaving();
            flowParseObject.remove("audio");
            flowParseObject.saveEventually();
            flowParseObject.pinInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("id", flowParseObject.getObjectId());
                    intent.putExtras(bundle);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            });
        }
        if (imageViewMic == v) {
            isNotSaving();
            startRecording();
        }
        if (imageViewStop == v) {
            if (recordPressed == true) {
                stopRecording();
            }
            else if (playPressed == true) {
                stopPlaying();
            }

        }
        if (imageViewPlay == v) {
            isNotSaving();
            startPlaying();

        }
    }
}
