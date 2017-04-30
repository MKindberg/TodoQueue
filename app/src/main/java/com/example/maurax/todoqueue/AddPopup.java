package com.example.maurax.todoqueue;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;

/**
 * Created by marcus on 23/04/2017.
 */

public class AddPopup extends Activity {
    private EditText inTask;
    private EditText inDesc;
    private SeekBar inSeek;
    private Tasks t;
    private Options options;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.popup_add);

        inTask = (EditText) findViewById(R.id.popup_task);
        inDesc = (EditText) findViewById(R.id.popup_desc);
        inSeek = (SeekBar) findViewById(R.id.popup_seek);

        options = Util.loadOptions(this);
        t = Util.loadTasks(options.list, this);
    }

    public void pop_add(View v){
        String name = inTask.getText().toString();
        String desc = inDesc.getText().toString();
        int prio = inSeek.getProgress() + 1;
        t.add(name, desc, prio);
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
