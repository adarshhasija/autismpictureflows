package com.adarshhasija.flows;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.List;

/**
 * Created by adarshhasija on 10/4/15.
 */
public class FlowUserItemsUpdateAdapter extends ArrayAdapter<ParseObject> {

    private static String LOG_TAG = "FlowsUserItemsUpdateAdapter";

    private Context context;
    private int resource;
    private List<ParseObject> mCurrentList;
    private List<ParseObject> mUpdatedList;
    static class ViewHolder {
        TextView original;
        TextView updated;
    }

    public FlowUserItemsUpdateAdapter(Context context, int resource, List<ParseObject> currentList, List<ParseObject> mUpdatedList) {
        super(context, resource, currentList);
        this.context = context;
        this.resource = resource;
        this.mCurrentList = currentList;
        this.mUpdatedList = mUpdatedList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(this.resource, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.original = (TextView) convertView.findViewById(R.id.original);
            viewHolder.updated = (TextView) convertView.findViewById(R.id.updated);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ParseObject original = mCurrentList.get(position);
        ParseObject updated = mUpdatedList.get(position);
        String stringOriginal = original.getString("text");
        String stringUpdated = updated.getString("text");

        if (position == 0) {
            viewHolder.original.setText("Original title: " + stringOriginal);

            if (stringUpdated.equals(stringOriginal)) {
                viewHolder.updated.setText("No change");
                convertView.setContentDescription("Title is " + stringOriginal +
                        " There is no change");
            }
            else {
                viewHolder.updated.setText("New title: " + stringUpdated);
            }
            convertView.setContentDescription("Title is " + stringOriginal +
                    " New title is: " + stringUpdated);
        }
        else if (position > 0) {
            viewHolder.original.setText("Current: " + stringOriginal);

            if (stringUpdated.equals(stringOriginal)) {
                viewHolder.updated.setText("No change");
                convertView.setContentDescription("Current " + stringOriginal +
                        " There is no change");
            }
            else {
                viewHolder.updated.setText("Updated: " + stringUpdated);
                convertView.setContentDescription("Item " + position + ". Current description is " + stringOriginal +
                        " New description is: " + stringUpdated);
            }
        }


        return convertView;
    }
}
