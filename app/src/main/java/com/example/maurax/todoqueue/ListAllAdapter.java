package com.example.maurax.todoqueue;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by marcus on 2016-11-13.
 */

public class ListAllAdapter extends ArrayAdapter<Task> {

    List<Task> data;
    Context context;

    public ListAllAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);
        data = objects;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Task t = data.get(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.listitem, null);

        TextView tTitle = (TextView) view.findViewById(R.id.text1);
        TextView tDesc = (TextView) view.findViewById(R.id.text2);
        if(!t.getName().equals(""))
            tTitle.setText(t.getName());
        else{
            tTitle.setVisibility(View.GONE);
            view.findViewById(R.id.itemLine).setVisibility(View.GONE);
        }
        if(!t.getDescription().equals(""))
            tDesc.setText(t.getDescription().replace("\n", "  //  "));
        else{
            tDesc.setVisibility(View.GONE);
            view.findViewById(R.id.itemLine).setVisibility(View.GONE);
        }
        return view;
    }
}
