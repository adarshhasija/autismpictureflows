package com.adarshhasija.flows;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;


public class EditImageActivity extends ActionBarActivity {

    private static final String LOG_TAG = "EditImageActivity";

    private final int REQUEST_IMAGE_CAPTURE = 1;
    private final int REQUEST_IMAGE_GALLERY = 2;
    private final int REQUEST_AUDIO_CAPTURE = 3;

    private ParseObject flowParseObject=null;
    private ParseFile parseFile;
    private boolean isSaving=false;

    private ProgressBar progressBarSave;
    MenuItem saveButton;



    private ParseFile saveImage(Bitmap bitmap) {
        parseFile = Parse.bitmapToParseFile(bitmap, flowParseObject.getObjectId());
        parseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                flowParseObject.put("image", parseFile);
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
        }, new ProgressCallback() {
            @Override
            public void done(Integer integer) {
                progressBarSave.setProgress(integer);
            }
        });
        return parseFile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);


        Bundle extras = getIntent().getExtras();
        String id = extras.getString("id");
        byte[] bytes;
        Bitmap bitmap=null;
        ParseQuery query = ParseQuery.getQuery("Item");
        query.fromLocalDatastore();
        try {
            flowParseObject = query.get(id);
            bytes = flowParseObject.getParseFile("image").getData();
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        progressBarSave = (ProgressBar) findViewById(R.id.progressBarSave);
        final Button buttonPlayStop = (Button) findViewById(R.id.buttonPlayStop);
        Button buttonDefaults = (Button) findViewById(R.id.buttonDefaults);
        ImageView imageViewMain = (ImageView) findViewById(R.id.imageViewMain);
        if(bitmap != null) {
            imageViewMain.setImageBitmap(bitmap);
            imageViewMain.setContentDescription("Main image: choose between camera button and gallery button to change image");
            registerForContextMenu(imageViewMain);
        }
        Button buttonCamera = (Button) findViewById(R.id.buttonCamera);
        Button buttonGallery = (Button) findViewById(R.id.buttonGallery);
        final ImageView imageViewPlay = (ImageView) findViewById(R.id.imageViewPlay);
        final Button buttonChangeAudio = (Button) findViewById(R.id.buttonChangeAudio);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add("Camera");
        menu.add("Gallery");
        menu.add("Default");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getTitle().equals("Camera")) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
        else if (item.getTitle().equals("Gallery")) {
            Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, REQUEST_IMAGE_GALLERY);
        }
        else {
            ParseObject oldItem = flowParseObject.getParseObject("oldItem");
            try {
                oldItem.fetchFromLocalDatastore();
                byte[] bytes = oldItem.getParseFile("image").getData();
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                ImageView imageViewMain = (ImageView) findViewById(R.id.imageViewMain);
                if(bitmap != null) {
                    imageViewMain.setImageBitmap(bitmap);
                    saveButton.setVisible(true);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView imageViewMain = (ImageView) findViewById(R.id.imageViewMain);
            imageViewMain.setImageBitmap(imageBitmap);
            saveButton.setVisible(true);
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
            saveButton.setVisible(true);
        }
        if (requestCode == REQUEST_AUDIO_CAPTURE && resultCode == RESULT_OK
                && null != data) {
            //audioFile = (File) data.getExtras().get("audioFile");

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_edit, menu);

        saveButton = (MenuItem) menu.findItem(R.id.action_save);
        saveButton.setVisible(false);

        return true;
    }

    private void isSaving() {
        isSaving = true;
        saveButton.setIcon(R.drawable.ic_action_cancel);
        progressBarSave.setVisibility(View.VISIBLE);
        progressBarSave.setProgress(0);
        setTitle("Saving...");
        ImageView imageViewMain = (ImageView) findViewById(R.id.imageViewMain);
        Bitmap bitmap = ((BitmapDrawable)imageViewMain.getDrawable()).getBitmap();
        saveImage(bitmap);
    }

    private void isNotSaving() {
        isSaving = false;
        saveButton.setIcon(R.drawable.ic_action_save);
        progressBarSave.setVisibility(View.GONE);
        setTitle(R.string.title_activity_item_edit);
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

}
