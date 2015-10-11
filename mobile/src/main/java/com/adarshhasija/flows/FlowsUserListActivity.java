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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class FlowsUserListActivity extends ActionBarActivity {

    private static String LOG_TAG = "FlowsUserListActivity";

    private LinearLayout emptyView;
    private Button buttonViewCloud;
    private ListView listView;
    private TextView textViewEmpty;
    private RelativeLayout greetingsLayout;

    private FindCallback findCallbackCloud = new FindCallback() {
        @Override
        public void done(List list, ParseException e) {
            if (e == null) {
                try {
                    ParseObject.pinAll("Flow", list);
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
        FlowsUserArrayAdapter adapter = (FlowsUserArrayAdapter) listView.getAdapter();
        if (adapter == null) {
            adapter = new FlowsUserArrayAdapter(getApplicationContext(), R.layout.row_flows_user, list);
            listView.setAdapter(adapter);
        }
        else {
            adapter.clear();
            adapter.addAll(list);
            adapter.notifyDataSetChanged();
        }

        if (list.size() > 0) {
            makeListViewVisible();
        }
        else {
            makeListViewInvisible();
        }
    }

    private void makeListViewInvisible() {
        emptyView.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
    }

    private void makeListViewVisible() {
        emptyView.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flows_user_list);

        emptyView = (LinearLayout) findViewById(R.id.emptyView);
        buttonViewCloud = (Button) findViewById(R.id.button_view_cloud);
        buttonViewCloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewCloud();
            }
        });
        greetingsLayout = (RelativeLayout) findViewById(R.id.greetingsLayout);
        greetingsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FeelingsListActivity.class);
                startActivity(intent);
            }
        });
        listView = (ListView) findViewById(R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParseObject object = (ParseObject) listView.getAdapter().getItem(position);
                Intent intent = new Intent(getApplicationContext(), FlowUserItemsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("id", object.getObjectId());
                //bundle.putParcelable("flow", flow);
                intent.putExtras(bundle);
                startActivityForResult(intent, position);
            }
        });

        ParseUser currentUser = ParseUser.getCurrentUser();
        List<ParseObject> list = new ArrayList<ParseObject>();
        setTitle(currentUser.getString("firstName") + " " + currentUser.getString("lastName"));
        ParseQuery query = ParseQuery.getQuery("Flow");
        query.whereEqualTo("owner", currentUser);
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
                ParseObject.unpinAll("Flow", list);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ParseQuery queryCloud = ParseQuery.getQuery("Flow");
            queryCloud.whereEqualTo("owner", currentUser);
            queryCloud.findInBackground(findCallbackCloud);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //returned from cloud
        if (requestCode == 5000 && resultCode == Activity.RESULT_OK && null != data) {
            makeListViewVisible();
            Bundle extras = data.getExtras();
            String id = extras.getString("id");
            ParseQuery query = ParseQuery.getQuery("Flow");
            query.fromLocalDatastore();
            try {
                ParseObject flow = query.get(id);
                FlowsUserArrayAdapter adapter = (FlowsUserArrayAdapter) listView.getAdapter();
                adapter.insert(flow, 0);
                adapter.notifyDataSetChanged();
                makeListViewVisible();
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        //delete
        else if (requestCode > -1 && resultCode == Activity.RESULT_OK && null != data) {
            Bundle extras = data.getExtras();
            String type = extras.getString("type");
            if (type != null) {
                if (type.equals("delete")) {
                    ParseObject flow = (ParseObject) listView.getAdapter().getItem(requestCode);
                    try {
                        flow.unpin("Flow");
                        flow.deleteEventually();
                        FlowsUserArrayAdapter adapter = (FlowsUserArrayAdapter) listView.getAdapter();
                        adapter.remove(adapter.getItem(requestCode));
                        adapter.notifyDataSetChanged();
                        if (adapter.getCount() < 1) {
                            makeListViewInvisible();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                try {
                    String flowId = extras.getString("id");
                    ParseQuery query = ParseQuery.getQuery("Flow");
                    query.fromLocalDatastore();
                    ParseObject flow = query.get(flowId);
                    FlowsUserArrayAdapter adapter = (FlowsUserArrayAdapter) listView.getAdapter();
                    adapter.remove(adapter.getItem(requestCode));
                    adapter.insert(flow, requestCode);
                    adapter.notifyDataSetChanged();
                } catch (ParseException e) {

                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_flows_user_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.action_cloud) {
            viewCloud();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void viewCloud() {
        Intent intent = new Intent(this, FlowsPublicListActivity.class);
        startActivityForResult(intent, 5000);
    }
}
