package com.example.maurax.todoqueue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;

import java.util.List;

import static android.R.attr.action;

/**
 * Created by marcus on 23/04/2017.
 */

public class AddPopup extends Activity {
    private EditText inTask;
    private EditText inDesc;
    private SeekBar inSeek;
    private Spinner inLists;
    private Tasks t;
    private Options options;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        setContentView(R.layout.popup_add);

        inTask = (EditText) findViewById(R.id.popup_task);
        inDesc = (EditText) findViewById(R.id.popup_desc);
        inSeek = (SeekBar) findViewById(R.id.popup_seek);
        inLists = (Spinner) findViewById(R.id.popup_lists);

        options = Util.loadOptions(this);
        t = Util.loadTasks(options.list, this);
        List<String> lists = Util.loadLists(this);

        ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, lists);
        inLists.setAdapter(aa);
        inLists.setSelection(aa.getPosition(options.list));

        if (Intent.ACTION_SEND.equals(action)) {
            inTask.setText(i.getStringExtra(Intent.EXTRA_SUBJECT));
            inDesc.setText(i.getStringExtra(Intent.EXTRA_TEXT));
        }

    }

    public void pop_add(View v){
        String name = inTask.getText().toString();
        String desc = inDesc.getText().toString();
        int prio = inSeek.getProgress() + 1;
        t.add(name, desc, prio);
        options.list = (String) inLists.getSelectedItem();
        Util.saveTasks(t, options.list, this);
        Util.updateWidget(this);
        if(t.size()==1 && options.notification)
            Util.updateNotification(this);
        finish();
    }

    public void pop_cancel(View v){
        finish();
    }
}
