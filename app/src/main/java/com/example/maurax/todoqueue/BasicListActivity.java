package com.example.maurax.todoqueue;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.DateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;

import java.sql.Time;
import java.util.List;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static com.example.maurax.todoqueue.Util.message;
import static com.example.maurax.todoqueue.Util.saveOptions;

/**
 * Created by marcus on 05/05/2017.
 */

public abstract class BasicListActivity extends AppCompatActivity {

    Options options;
    Tasks tasks;

    PopupMenu popup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.getClass().getName().equals(MainActivity.class.getName())){
            setContentView(R.layout.activity_main);
        }else if(this.getClass().getName().equals(ListAllActivity.class.getName())){
            setContentView(R.layout.activity_list_all);
        }
        tasks = new Tasks();
        options = new Options();

        final ImageView btnNotif = (ImageView) findViewById(R.id.buttonNotif);



        setUp();
        findViews();
        createMenu();
        readIntent();
        setListeners();

        if (options.notification)
            btnNotif.setImageDrawable(getDrawable(R.drawable.ic_notifications_white_24dp));
        else
            btnNotif.setImageDrawable(getDrawable(R.drawable.ic_notifications_off_white_24dp));

        update();

    }

    abstract void setUp();

    abstract void readIntent();
    abstract void findViews();

    void createMenu() {
        popup = new PopupMenu(this, findViewById(R.id.buttonMenu));
        popup.getMenuInflater().inflate(R.menu.menu_list, popup.getMenu());
        popup.getMenu().findItem(R.id.colorsOp).setChecked(options.colors);
    }
    void setListeners(){
        findViewById(R.id.ListText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listDialog(BasicListActivity.this);
            }
        });
        findViewById(R.id.buttonSort).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tasks.sort();
                update();
                Util.message("Sorted", BasicListActivity.this);
            }
        });

        final ImageView btnNotif = (ImageView) findViewById(R.id.buttonNotif);
        btnNotif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                options.notification = !options.notification;
                if (options.notification)
                    btnNotif.setImageDrawable(getDrawable(R.drawable.ic_notifications_white_24dp));
                else
                    btnNotif.setImageDrawable(getDrawable(R.drawable.ic_notifications_off_white_24dp));


            }
        });
    }

    public abstract void update();
    public void save() {
        Util.saveTasks(tasks, options.list, this);
        Util.saveOptions(options, this);
        Util.updateWidget(this);
    }
    public abstract void load();


    void showTimeDialog(){
        final Calendar c = Calendar.getInstance();
        final int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        final int minuteOfDay = c.get(Calendar.MINUTE);
        AlertDialog.Builder builderTime = new AlertDialog.Builder(this);
        builderTime.setTitle("Select a time");
        TimePickerDialog tp = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minute) {
                Calendar calSet = (Calendar) c.clone();
                calSet.set(Calendar.HOUR_OF_DAY, hour);
                calSet.set(Calendar.MINUTE, minute);
                calSet.set(Calendar.SECOND, 0);
                calSet.set(Calendar.MILLISECOND, 0);
                if (hour<hourOfDay || (hour==hourOfDay && minute<=minuteOfDay))
                    calSet.set(Calendar.HOUR, c.get(Calendar.HOUR-hourOfDay+hour+24));
                setAlarm(calSet);

            }
        }, hourOfDay, minuteOfDay, true);
        tp.show();
        tp.setButton(TimePickerDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
    }

    void setAlarm(Calendar time){
        Intent alarmIntent = new Intent(BasicListActivity.this, NotificationReciever.class);
        alarmIntent.putExtra("list", options.list);
        PendingIntent pi = PendingIntent.getBroadcast(BasicListActivity.this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.ELAPSED_REALTIME, time.getTimeInMillis(), pi);
        Util.message("Setting alarm", BasicListActivity.this);
    }

    void listDialog(final Context con){
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle("Select a list:");
        final List<String> lists = Util.loadLists(this);
        lists.add("Add new");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, lists);
        builderSingle.setSingleChoiceItems(arrayAdapter, arrayAdapter.getPosition(options.list), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String strName = arrayAdapter.getItem(which);
                if(strName.equals("Add new")){
                    AlertDialog.Builder builderInner = new AlertDialog.Builder(con);
                    builderInner.setTitle("New list name:");

                    final EditText ETname = new EditText(con);
                    builderInner.setView(ETname);

                    builderInner.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builderInner.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String name = String.valueOf(ETname.getText());
                            if(name.equals(""))
                                Util.message("List must have a name", con);
                            else if(lists.contains(name))
                                Util.message("List name must be unique", con);
                            else{
                                save();
                                options.list = name;
                                Util.addTable(name, con);
                                saveOptions(options, con);
                                load();
                                arrayAdapter.insert(name, arrayAdapter.getCount()-1);
                                arrayAdapter.notifyDataSetChanged();
                                update();
                            }
                        }
                    });
                    builderInner.show();
                }else{
                    save();
                    options.list = strName;
                    saveOptions(options, con);
                    load();
                    update();
                }
            }

        });

        builderSingle.setNegativeButton("Move up", null);

        builderSingle.setNeutralButton("Delete", null);

        builderSingle.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = builderSingle.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (arrayAdapter.getCount() == 2) {
                    Util.message("You can't delete the last list", con);
                    return;
                }
                //dialog.dismiss();
                final AlertDialog.Builder builderDelete = new AlertDialog.Builder(con);
                builderDelete.setTitle("Are you sure you want to delete the list " + options.list + "?");
                builderDelete.setNegativeButton("Calcel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        dialog.getListView().setItemChecked(arrayAdapter.getPosition(options.list), true);
                        d.dismiss();
                    }
                });

                builderDelete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        int pos = arrayAdapter.getPosition(options.list);
                        Util.removeTable(options.list, con);
                        arrayAdapter.remove(options.list);
                        options.list = arrayAdapter.getItem((pos==0?0:pos-1));
                        arrayAdapter.notifyDataSetChanged();
                        saveOptions(options, con);
                        load();
                        dialog.getListView().setItemChecked((pos==0?0:pos-1), true);
                    }
                });
                builderDelete.show();
            }
        });

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = lists.indexOf(options.list);
                if(index>0) {
                    lists.add(index-1, lists.remove(index));
                    lists.remove(lists.size()-1);
                    Util.saveLists(lists, BasicListActivity.this);
                    lists.add("Add new");
                    dialog.getListView().setItemChecked(index-1, true);
                    arrayAdapter.notifyDataSetChanged();

                }
            }

        });

        //Rename
        dialog.getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                AlertDialog.Builder editBuilder = new AlertDialog.Builder(BasicListActivity.this);
                final EditText edittext = new EditText(BasicListActivity.this);
                editBuilder.setMessage("Enter new name");
                final String old = lists.get(position);
                edittext.setText(old);

                editBuilder.setView(edittext);

                editBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String name = edittext.getText().toString();
                        if(name.isEmpty())
                            message("List must have a name", BasicListActivity.this);
                        else if(!name.equals(old) && lists.contains(name))
                            message("Name must be unique", BasicListActivity.this);
                        else {
                            if (old.equals(options.list))
                                options.list = name;

                            lists.set(position, name);
                            lists.remove(lists.size()-1);
                            Tasks t = Util.loadTasks(old, BasicListActivity.this);
                            Util.removeTable(old, BasicListActivity.this);
                            Util.addTable(name, BasicListActivity.this);
                            Util.saveTasks(t, name, BasicListActivity.this);
                            lists.add("Add new");
                            update();
                            arrayAdapter.notifyDataSetChanged();
                        }
                    }
                });

                editBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });

                editBuilder.show();
                return false;
            }
        });

        //builderSingle.show();

    }

    @Override
    protected void onPause() {
        super.onPause();
        save();
        if (options.notification && tasks.size() != 0)
            NotificationReciever.showNotification(tasks.getFirst().getName(), tasks.getFirst().getDescription(), tasks.getFirst().getColorId(), this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
        NotificationReciever.cancelNotification(this);
    }
}
