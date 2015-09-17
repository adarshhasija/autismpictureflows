package com.adarshhasija.flows;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class FlowUserItemsActivity extends ActionBarActivity {

    public static String LOG_TAG = "FlowUserItemsActivity";

    private ListView listView;

    private List<ParseObject> mList=null;
    private ParseObject flowParseObject=null;
    private TextToSpeech textToSpeech=null;
    private void playAudio(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "0");
        }
        else {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }



    private FindCallback findCallbackCloud = new FindCallback<ParseObject>() {
        @Override
        public void done(List<ParseObject> list, ParseException e) {
            if (e == null) {
                try {
                    ParseObject.pinAll("Item", list);
                    mList = list;
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
                Intent intent = new Intent(getApplicationContext(), FullscreenActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("id", object.getObjectId());
                intent.putExtras(bundle);
                startActivityForResult(intent, position);
            }
        });


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
            mList = list;
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

        if (id == R.id.action_discard) {
            new AlertDialog.Builder(FlowUserItemsActivity.this)
                    .setTitle(R.string.delete)
                    .setMessage(R.string.delete_confirm)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                for (ParseObject item : mList) {
                                    item.deleteEventually();
                                }
                                ParseObject.unpinAll("Item", mList);
                                Intent returnIntent = new Intent();
                                Bundle bundle = new Bundle();
                                bundle.putString("type", "delete");
                                returnIntent.putExtras(bundle);
                                setResult(Activity.RESULT_OK, returnIntent);
                                finish();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }


        return super.onOptionsItemSelected(item);
    }
}
