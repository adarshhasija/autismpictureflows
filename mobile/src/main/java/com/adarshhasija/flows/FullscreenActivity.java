package com.adarshhasija.flows;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

public class FullscreenActivity extends Activity {

    private static final String LOG_TAG = "FullscreenActivity";

    private ParseObject object = null;
    private TextToSpeech textToSpeech=null;
    private void playAudio(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "0");
        }
        else {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        textToSpeech=new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
                            textToSpeech.setLanguage(Locale.US);
                        }
                    }
                });

        ImageView imageView = (ImageView) findViewById(R.id.imageView);

        Bundle extras = getIntent().getExtras();
        String id = extras.getString("id");
        byte[] bytes;
        Bitmap bitmap=null;
        ParseQuery query = ParseQuery.getQuery("Item");
        query.fromLocalDatastore();
        try {
            object = query.get(id);
            bytes = object.getParseFile("image").getData();
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if(bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.setContentDescription("Main image: choose between camera button and gallery button to change image");
                registerForContextMenu(imageView);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (object.getParseFile("audio") == null) {
                    playAudio(object.getString("text"));
                }
                else {
                    ParseFile parseFileAudio = object.getParseFile("audio");
                    if(parseFileAudio != null) {
                        File audioFile = Parse.parseFileToJavaFile(parseFileAudio, "audioFile", "3gp");
                        final MediaPlayer mPlayer = new MediaPlayer();
                        try {
                            FileInputStream fis = new FileInputStream(audioFile);
                            mPlayer.setDataSource(fis.getFD());
                            mPlayer.prepare();
                            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    mPlayer.setOnCompletionListener(null);
                                    mPlayer.release();
                                }
                            });
                            mPlayer.start();
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "prepare() failed");
                        }
                    }
                }
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fullscreen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
