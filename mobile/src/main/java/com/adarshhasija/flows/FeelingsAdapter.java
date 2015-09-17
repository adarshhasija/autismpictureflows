package com.adarshhasija.flows;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.List;

/**
 * Created by adarshhasija on 7/31/15.
 */
public class FeelingsAdapter extends ArrayAdapter<ParseObject> {

    private Context context;
    private int resource;
    private List<ParseObject> objects;
    static class ViewHolder {
        ImageView iconView;
        TextView text;
    }

    public FeelingsAdapter(Context context, int resource, List<ParseObject> values) {
        super(context, resource);
        this.context = context;
        this.resource = resource;
        this.objects = values;
    }

    @Override
    public int getCount() {
        return 7;
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


        switch (position) {
            case 0:
                viewHolder.text.setText("Fever");
                viewHolder.iconView.setImageResource(R.drawable.emoji_fever);
                convertView.setContentDescription("Fever");
                break;

            case 1:
                viewHolder.text.setText("Headache");
                viewHolder.iconView.setImageResource(R.drawable.emoji_headache);
                convertView.setContentDescription("Headache");
                break;

            case 2:
                viewHolder.text.setText("Runny nose");
                viewHolder.iconView.setImageResource(R.drawable.emoji_runny_nose);
                convertView.setContentDescription("Runny nose");
                break;

            case 3:
                viewHolder.text.setText("Cough");
                viewHolder.iconView.setImageResource(R.drawable.emoji_cough);
                convertView.setContentDescription("Cough");
                break;

            case 4:
                viewHolder.text.setText("Stomach pain");
                viewHolder.iconView.setImageResource(R.drawable.emoji_stomach_pain);
                convertView.setContentDescription("Stomach pain");
                break;

            case 5:
                viewHolder.text.setText("Hurt my leg");
                viewHolder.iconView.setImageResource(R.drawable.emoji_hurt_knee);
                convertView.setContentDescription("Hurt my leg");
                break;

            case 6:
                viewHolder.text.setText("Hurt my arm");
                viewHolder.iconView.setImageResource(R.drawable.emoji_hurt_arm);
                convertView.setContentDescription("Hurt my arm");
                break;

            default:
                viewHolder.text.setText("Headache");
                viewHolder.iconView.setImageResource(R.drawable.emoji_headache);
                convertView.setContentDescription("Headache");
                break;
        }


        return convertView;
    }
}
