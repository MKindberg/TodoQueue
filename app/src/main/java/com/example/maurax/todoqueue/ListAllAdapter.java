package com.example.maurax.todoqueue;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
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

class ListAllAdapter extends ArrayAdapter<Task> {

    private final List<Task> data;
    private final Context context;

    public ListAllAdapter(Context context, int listitem, List<Task> objects) {
        super(context, R.layout.listitem, objects);
        data = objects;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        ViewHolderItem viewHolder;

        Task t = data.get(position);
        if(convertView==null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listitem, null);

            viewHolder = new ViewHolderItem();

            TextView tTitle = (TextView) convertView.findViewById(R.id.text1);
            TextView tDesc = (TextView) convertView.findViewById(R.id.text2);
            TextView tLine = (TextView) convertView.findViewById(R.id.itemLine);
            viewHolder.title = tTitle;
            viewHolder.desc = tDesc;
            viewHolder.line = tLine;

            viewHolder.desc.setEllipsize(TextUtils.TruncateAt.END);


            convertView.setTag(viewHolder);

        }else
            viewHolder = (ViewHolderItem) convertView.getTag();
        //int w = convertView.getWidth();
        //viewHolder.desc.setWidth(w/2);
        if(!t.getName().equals("")) {
            viewHolder.title.setVisibility(View.VISIBLE);
            viewHolder.title.setText(t.getName());
        }else{
            viewHolder.title.setVisibility(View.GONE);}
        if(!t.getDescription().equals("")) {
            viewHolder.desc.setVisibility(View.VISIBLE);
            viewHolder.desc.setText(t.getDescription().replace("\n", "  //  "));
        }else
            viewHolder.desc.setVisibility(View.GONE);

        if(t.getDescription().equals("") || t.getName().equals(""))
            viewHolder.line.setVisibility(View.GONE);
        else
            viewHolder.line.setVisibility(View.VISIBLE);

        return convertView;
    }
    private static class ViewHolderItem {
        TextView title;
        TextView desc;
        TextView line;
    }

}

