package com.adarshhasija.flows;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class FlowUserItemsActivity extends ActionBarActivity {

    public static String LOG_TAG = "FlowUserItemsActivity";

    private ListView listView;

    private ParseObject flowParseObject=null;
    private TextToSpeech textToSpeech=null;
    private void playAudio(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "0");
    }



    private FindCallback findCallbackCloud = new FindCallback<ParseObject>() {
        @Override
        public void done(List<ParseObject> list, ParseException e) {
            if (e == null) {
                try {
                    ParseObject.pinAll("Item", list);
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
                populateList(list);
            }
            else {
                e.printStackTrace();
            }
        }
    };

    private void populateList(List<ParseObject> list) {
        FlowItemsAdapter adapter = new FlowItemsAdapter(getApplicationContext(), R.layout.row_flow_items, list);
        listView.setAdapter(adapter);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow_user_items);

        textToSpeech=new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
                            textToSpeech.setLanguage(Locale.US);
                        }
                    }
                });

        listView = (ListView) findViewById(R.id.list);
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParseObject object = (ParseObject) listView.getAdapter().getItem(position);
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
      /*  listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ParseUser currentUser = ParseUser.getCurrentUser();
                if (currentUser != null &&
                        flowParseObject.getParseUser("owner") == currentUser) {
                    ParseObject object = (ParseObject) listView.getAdapter().getItem(position);
                    Intent intent = new Intent(getApplicationContext(), EditImageActivity.class);
                    Bundle bundle = new Bundle();
                    //bundle.putParcelable("item", item);
                    bundle.putString("id", object.getObjectId());
                    intent.putExtras(bundle);
                    startActivityForResult(intent, position);
                }
                else {
                    new AlertDialog.Builder(FlowUserItemsActivity.this)
                            .setTitle("No editing")
                            .setMessage("If you would like to edit, you must download this activity flow to your account")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }

                return true;
            }
        }); */


        Bundle bundle = getIntent().getExtras();
        //flow = bundle.getParcelable("flow");
        String id = bundle.getString("id");

        List<ParseObject> list = new ArrayList<ParseObject>();
        ParseQuery query = ParseQuery.getQuery("Item");
        query.whereEqualTo("flowId", id);
        query.orderByAscending("index");
        query.fromLocalDatastore();
        try {
            list = query.find();
            populateList(list);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        if(cm.getActiveNetworkInfo() != null && list.size() < 1) {
            try {
                ParseObject.unpinAll("Item", list);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ParseQuery queryCloud = ParseQuery.getQuery("Item");
            queryCloud.whereEqualTo("flowId", id);
            queryCloud.orderByAscending("index");
            queryCloud.findInBackground(findCallbackCloud);
        }

        ParseQuery queryFlow = ParseQuery.getQuery("Flow");
        queryFlow.fromLocalDatastore();
        try {
            flowParseObject = queryFlow.get(id);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        setTitle(flowParseObject.getString("text"));
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        ParseObject obj = (ParseObject) listView.getItemAtPosition(acmi.position);

        menu.add("Change Image");
        menu.add("Change Audio");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        ParseObject object = (ParseObject) listView.getAdapter().getItem(position);

        Intent intent=null;
        if (item.getTitle().equals("Change Image")) {
            intent = new Intent(getApplicationContext(), EditImageActivity.class);
        }
        else if (item.getTitle().equals("Change Audio")) {
            intent = new Intent(getApplicationContext(), EditAudioActivity.class);
        }

        if (intent != null) {
            Bundle bundle = new Bundle();
            bundle.putString("id", object.getObjectId());
            intent.putExtras(bundle);
            startActivityForResult(intent, position);
        }

        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK && null != data) {
            Bundle bundle = data.getExtras();
            String id = bundle.getString("id");
            ParseObject object=null;
            ParseQuery query = ParseQuery.getQuery("Item");
            query.fromLocalDatastore();
            try {
                object = query.get(id);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            FlowItemsAdapter adapter = (FlowItemsAdapter) listView.getAdapter();
            adapter.remove(adapter.getItem(requestCode));
            adapter.insert(object, requestCode);
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_flow_user_items, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }
}
