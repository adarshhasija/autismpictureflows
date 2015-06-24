package com.adarshhasija.flows;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.Locale;


public class ItemActivity extends Activity {

    private static final String LOG_TAG = "ItemActivity";

    private final int REQUEST_IMAGE_CAPTURE = 1;
    private final int REQUEST_IMAGE_GALLERY = 2;
    private final int REQUEST_AUDIO_CAPTURE = 3;

    private Item item=null;
    private TextToSpeech textToSpeech=null;
    private void playAudio() {
        textToSpeech.speak(item.getText(), TextToSpeech.QUEUE_FLUSH, null, "0");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        textToSpeech=new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
                            textToSpeech.setLanguage(Locale.US);
                        }
                    }
                });

        Bundle extras = getIntent().getExtras();
        item = extras.getParcelable("item");
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(item.getText());

        ImageView imageViewEdit = (ImageView) findViewById(R.id.imageViewEdit);
        if (ParseUser.getCurrentUser() != null) {
            imageViewEdit.setVisibility(View.INVISIBLE);
        }
        imageViewEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        byte[] imageData = item.getImage();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        ImageView imageViewMain = (ImageView) findViewById(R.id.imageViewMain);
        imageViewMain.setImageBitmap(bitmap);
        imageViewMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio();
            }
        });

        ImageView imageViewPlay = (ImageView) findViewById(R.id.imageViewPlay);
        imageViewPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio();
            }
        });

        ImageView imageViewLeftArrow = (ImageView) findViewById(R.id.imageViewLeftArrow);
        imageViewLeftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putInt("direction", 0); //back
                intent.putExtras(bundle);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        ImageView imageViewRightArrow = (ImageView) findViewById(R.id.imageViewRightArrow);
        imageViewRightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putInt("direction", 1); //next
                intent.putExtras(bundle);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

    }

    @Override
    protected void onDestroy() {
        if(textToSpeech !=null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView imageViewMain = (ImageView) findViewById(R.id.imageViewMain);
            imageViewMain.setImageBitmap(imageBitmap);
        }

        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK
                && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);

            if (cursor == null || cursor.getCount() < 1) {
                return;
            }

            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

            if(columnIndex < 0) // no column index
                return; // DO YOUR ERROR HANDLING

            String picturePath = cursor.getString(columnIndex);

            cursor.close(); // close cursor

            Bitmap photo = BitmapFactory.decodeFile(picturePath.toString());
            ImageView imageViewMain = (ImageView) findViewById(R.id.imageViewMain);
            imageViewMain.setImageBitmap(photo);

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.action_edit) {
            Intent i = new Intent(this, EditImageActivity.class);
            startActivityForResult(i, REQUEST_AUDIO_CAPTURE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
