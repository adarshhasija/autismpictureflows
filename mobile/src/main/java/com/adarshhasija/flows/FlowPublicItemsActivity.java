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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FlowPublicItemsActivity extends ActionBarActivity {

    public static String LOG_TAG = "FlowPublicItemsActivity";

    private ProgressBar progressBarDownload;
    private ProgressBar progressBarLoading;
    private Button buttonReturnToProfile;
    private ListView listView;

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
            progressBarLoading.setVisibility(View.GONE);
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


    private void downloadFlow() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseObject newFlow = new ParseObject("Flow");
        newFlow.put("oldFlow", flowParseObject);
        newFlow.put("text", flowParseObject.getString("text"));
        newFlow.put("image", flowParseObject.getParseFile("image"));
        newFlow.put("owner", currentUser);
        ParseACL acl = new ParseACL();
        acl.setPublicReadAccess(false);
        acl.setPublicWriteAccess(false);
        acl.setReadAccess(currentUser, true);
        acl.setWriteAccess(currentUser, true);
        newFlow.setACL(acl);

        try {
            newFlow.save();
            newFlow.pinInBackground("Flow");
        } catch (ParseException e) {
            progressBarDownload.setVisibility(View.GONE);
            e.printStackTrace();
        }

        List<ParseObject> newItems = new ArrayList<ParseObject>();
        FlowItemsAdapter adapter = (FlowItemsAdapter) listView.getAdapter();
        for (int i=0;i < adapter.getCount(); i++) {
            ParseObject oldItem = adapter.getItem(i);
            ParseObject newItem = new ParseObject("Item");
            newItem.put("oldItem", oldItem);
            newItem.put("text", oldItem.getString("text"));
            if (oldItem.getParseFile("image") != null) {
                newItem.put("image", oldItem.getParseFile("image"));
            }
            newItem.put("index", oldItem.getInt("index"));
            newItem.put("owner", ParseUser.getCurrentUser());
            newItem.put("flowId", newFlow.getObjectId());
            newItem.setACL(acl);
            newItem.saveEventually();
            //newItem.pinInBackground("Item");
            newItems.add(newItem);
        }
        //newFlow.put("items", newItems);
        ParseObject.pinAllInBackground("Item", newItems);
        Intent returnIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("id", newFlow.getObjectId());
        returnIntent.putExtras(bundle);
        setResult(Activity.RESULT_OK, returnIntent);
        buttonReturnToProfile.setVisibility(View.VISIBLE);
        //finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow_public_items);

        textToSpeech=new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
                            textToSpeech.setLanguage(Locale.US);
                        }
                    }
                });

        progressBarDownload = (ProgressBar) findViewById(R.id.progressBarDownload);
        progressBarLoading = (ProgressBar) findViewById(R.id.progressBarLoading);
        buttonReturnToProfile = (Button) findViewById(R.id.buttonReturnToProfile);
        buttonReturnToProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        listView = (ListView) findViewById(R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParseObject object = (ParseObject) listView.getAdapter().getItem(position);
                playAudio(object.getString("text"));
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                new AlertDialog.Builder(getApplicationContext())
                        .setTitle("No editing")
                        .setMessage("If you would like to edit, you must download this activity flow to your account")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                return true;
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
            populateList(list);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        if(cm.getActiveNetworkInfo() != null) {
            progressBarLoading.setVisibility(View.VISIBLE);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_flow_public_items, menu);
        final Menu localMenu = menu;

        ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser != null) {
            ParseQuery query = ParseQuery.getQuery("Flow");
            query.whereEqualTo("oldFlow", flowParseObject);
            query.whereEqualTo("owner", currentUser);
            query.fromLocalDatastore();
            query.getFirstInBackground(new GetCallback() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (e == null) {
                        if (parseObject != null) {
                            MenuItem downloadButton = (MenuItem) localMenu.findItem(R.id.action_download);
                            downloadButton.setVisible(false);
                        }
                    } else {
                        e.printStackTrace();
                    }
                }
            });
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_download) {
            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser == null) {
                new AlertDialog.Builder(FlowPublicItemsActivity.this)
                        .setTitle(R.string.no_user_found)
                        .setMessage(R.string.no_user_found_message)
                        .setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.signup, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return false;
            }

            ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
            if(cm.getActiveNetworkInfo() == null) {
                new AlertDialog.Builder(FlowPublicItemsActivity.this)
                        .setTitle(R.string.no_internet_connection)
                        .setMessage(R.string.no_internet_connection_message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                return false;
            }


            new AlertDialog.Builder(FlowPublicItemsActivity.this)
                    .setTitle(R.string.download_activity_flow)
                    .setMessage(R.string.download_activity_flow_message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            progressBarDownload.setVisibility(View.VISIBLE);
                            downloadFlow();
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

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
