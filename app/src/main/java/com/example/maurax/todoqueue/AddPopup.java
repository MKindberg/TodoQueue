package com.example.maurax.todoqueue;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.maurax.todoqueue.R.string.desc;
import static com.example.maurax.todoqueue.Util.loadTasks;
import static com.example.maurax.todoqueue.Util.updateNotification;
import static com.example.maurax.todoqueue.Util.updateWidget;

/**
 * Created by marcus on 23/04/2017.
 */

public class AddPopup extends Activity {
    private EditText inTask;
    private EditText inDesc;
    private SeekBar inSeek;
    private Tasks t;
    private boolean notificaion;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.popup_add);

        inTask = (EditText) findViewById(R.id.popup_task);
        inDesc = (EditText) findViewById(R.id.popup_desc);
        inSeek = (SeekBar) findViewById(R.id.popup_seek);

        t = Util.loadTasks(this);

        notificaion = Util.loadOptions(this)[1];

    }

    public void pop_add(View v){
        String name = inTask.getText().toString();
        String desc = inDesc.getText().toString();
        int prio = inSeek.getProgress() + 1;
        t.add(name, desc, prio);
        Util.saveTasks(t, this);
        Util.updateWidget(this);
        if(t.size()==1 && notificaion)
            Util.updateNotification(this);
        finish();
    }

    public void pop_cancel(View v){
        finish();
    }
}
