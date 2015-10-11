package com.adarshhasija.flows;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class FlowUserItemsUpdateActivity extends ActionBarActivity {

    public static String LOG_TAG = "FlowUserItemsActivity";

    private ParseObject flowParseObject=null;
    private ParseObject oldFlow=null;
    private List<ParseObject> mCurrentList=null;
    private List<ParseObject> mUpdatedList=null;

    private ListView listView;


    private void downloadUpdate() {
        flowParseObject.put("text", oldFlow.getString("text"));
        for (int i = 0; i < mCurrentList.size(); i++) {
            ParseObject current = mCurrentList.get(i);
            String tmp = current.getString("text");
            current.put("text", tmp);
            current.saveEventually();
        }

        try {
            flowParseObject.pin("Flow");
            flowParseObject.saveEventually();
            ParseObject.pinAll("Item", mCurrentList);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Intent returnIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("id", flowParseObject.getObjectId());
        returnIntent.putExtras(bundle);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow_user_items_update);

        mCurrentList = new ArrayList<ParseObject>();
        mUpdatedList = new ArrayList<ParseObject>();

        Bundle extras = getIntent().getExtras();
        String id = extras.getString("id");

        ParseQuery queryFlow = ParseQuery.getQuery("Flow");
        queryFlow.fromLocalDatastore();
        try {
            flowParseObject = queryFlow.get(id);
            mCurrentList.add(flowParseObject);

            ParseQuery query = ParseQuery.getQuery("Item");
            query.whereEqualTo("flowId", id);
            query.orderByAscending("index");
            query.fromLocalDatastore();
            mCurrentList.addAll(query.find());

            oldFlow = flowParseObject.getParseObject("oldFlow");
            oldFlow.fetchFromLocalDatastore();
            mUpdatedList.add(oldFlow);

            query = ParseQuery.getQuery("Item");
            query.whereEqualTo("flowId", oldFlow.getObjectId());
            query.orderByAscending("index");
            query.fromLocalDatastore();
            mUpdatedList.addAll(query.find());

        } catch (ParseException e) {
            e.printStackTrace();
        }



        FlowUserItemsUpdateAdapter adapter = new FlowUserItemsUpdateAdapter(getApplicationContext(), R.layout.row_update_items, mCurrentList, mUpdatedList);
        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_flow_user_items_update, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_accept) {
            downloadUpdate();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
