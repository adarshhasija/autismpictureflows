package com.adarshhasija.flows;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by adarshhasija on 5/17/15.
 */
public class FlowsPublicArrayAdapter extends ArrayAdapter<ParseObject> {

    private static String LOG_TAG = "FlowsPublicArrayAdapter";

    private Context context;
    private int resource;
    private List<ParseObject> objects;
    static class ViewHolder {
        ImageView iconView;
        TextView text;
    }

    public FlowsPublicArrayAdapter(Context context, int resource, List<ParseObject> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(this.resource, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.text = (TextView) convertView.findViewById(R.id.text);
            viewHolder.iconView = (ImageView) convertView.findViewById(R.id.icon);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ParseObject object = objects.get(position);

        //viewHolder.iconView.setImageResource(R.drawable.microsoftexcel);
        viewHolder.text.setText(object.getString("text"));

        new AsyncTask<ViewHolder, Void, Bitmap>() {
            private ViewHolder v;

            @Override
            protected Bitmap doInBackground(ViewHolder... params) {
                v = params[0];
                //File image = Parse.parseFileToJavaFile(object.getParseFile("image"));
                //Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
                byte[] imageData = new byte[0];
                try {
                    imageData = object.getParseFile("image").getData();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                v.iconView.setImageBitmap(result);
                super.onPostExecute(result);
            }
        }.execute(viewHolder);

        convertView.setContentDescription(object.getString("text"));

        return convertView;
    }
}
