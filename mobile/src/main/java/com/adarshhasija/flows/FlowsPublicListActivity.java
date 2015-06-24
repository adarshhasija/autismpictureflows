package com.adarshhasija.flows;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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

public class FlowsPublicListActivity extends ActionBarActivity {

    private static final String LOG_TAG = "FlowsPublicListActivity";

    private ListView listView;


    private FindCallback findCallbackCloud = new FindCallback<ParseObject>() {
        @Override
        public void done(List<ParseObject> list, ParseException e) {
            if (e == null) {
                boolean changesMade=false;
                FlowsPublicArrayAdapter adapter = (FlowsPublicArrayAdapter) listView.getAdapter();
                for (ParseObject listObject : list) {
                    int position = adapter.getPosition(listObject);
                    if (position == -1) {
                        adapter.insert(listObject, 0);
                        changesMade = true;
                        listObject.pinInBackground("Flow");
                    }
                    else {
                        ParseObject adapterObject = adapter.getItem(position);
                        if (adapterObject.getUpdatedAt().after(listObject.getUpdatedAt())) {
                            adapter.remove(adapterObject);
                            adapter.insert(listObject, 0);
                            changesMade = true;
                            listObject.pinInBackground("Flow");
                        }
                    }
                }
                if (changesMade) {
                    adapter.notifyDataSetChanged();
                }
            }
            else {
                e.printStackTrace();
            }
        }
    };

    private void populateList(List<ParseObject> list) {
        FlowsPublicArrayAdapter adapter = new FlowsPublicArrayAdapter(getApplicationContext(), R.layout.row_flows_public, list);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flows_public_list);

        listView = (ListView) findViewById(R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParseObject object = (ParseObject) listView.getAdapter().getItem(position);
                Intent intent = new Intent(getApplicationContext(), FlowPublicItemsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("id", object.getObjectId());
                intent.putExtras(bundle);
                startActivityForResult(intent, position);
            }
        });

        List<ParseObject> list = new ArrayList<ParseObject>();
        ParseQuery query = ParseQuery.getQuery("Flow");
        query.whereEqualTo("owner", null);
        query.fromLocalDatastore();
        try {
            list = query.find();
            populateList(list);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        if(cm.getActiveNetworkInfo() != null) {
          /*  try {
                ParseObject.unpinAll("Flow", list);
            } catch (ParseException e) {
                e.printStackTrace();
            }   */
            ParseQuery queryCloud = ParseQuery.getQuery("Flow");
            queryCloud.whereEqualTo("owner", null);
            queryCloud.findInBackground(findCallbackCloud);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && null != data) {
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_flows_public_list, menu);
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
