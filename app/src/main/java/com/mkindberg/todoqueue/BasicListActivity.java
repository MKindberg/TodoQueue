package com.mkindberg.todoqueue;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static android.R.id.input;
import static android.R.id.message;
import static com.mkindberg.todoqueue.Util.message;
import static com.mkindberg.todoqueue.Util.saveOptions;

/**
 * Created by marcus on 05/05/2017.
 */

public abstract class BasicListActivity extends AppCompatActivity {

    Options options;
    Tasks tasks;

    PopupMenu popup;

    ImageView btnNotif;
    Drawable notifOn;
    Drawable notifOff;

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



        btnNotif = (ImageView) findViewById(R.id.buttonNotif);
        notifOn = getDrawable(R.drawable.ic_notifications_white_24dp);
        notifOff = getDrawable(R.drawable.ic_notifications_off_white_24dp);

        setUp();
        findViews();
        createMenu();
        readIntent();
        setListeners();

        setNotif(options.notification);

        Intent i = getIntent();
        if (i.getStringExtra(NotificationReciever.NOTIFICATION_LIST)!=null){
            options.list = i.getStringExtra(NotificationReciever.NOTIFICATION_LIST);
            saveOptions(options, this);
        }

        update();

    }

    void setNotif(boolean notif){
        if (notif)
            btnNotif.setImageDrawable(notifOn);
        else
            btnNotif.setImageDrawable(notifOff);
    }

    abstract void setUp();

    abstract void readIntent();
    abstract void findViews();

    void createMenu() {
        popup = new PopupMenu(this, findViewById(R.id.buttonMenu));
        popup.getMenuInflater().inflate(R.menu.menu_list, popup.getMenu());
        popup.getMenu().findItem(R.id.menu_item_colors).setChecked(options.colors);
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
                sorted();
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

        btnNotif.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showTimeDialog();
                return true;
            }
        });
    }

    protected abstract void sorted();

    public abstract void update();
    public void save() {
        Util.saveTasks(tasks, options.list, this);
        Util.saveOptions(options, this);
        Util.updateWidget(this);
    }
    public void load() {
        options = Util.loadOptions(this);
        tasks = Util.loadTasks(options.list, this);
        Task.loadPrioNames(this);
        update();
    }

    public void importList() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            return;
        }
        String state = Environment.getExternalStorageState();
        if (!(Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))) {
            message("External storage unavailable", this);
            return;
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Import");
        dialog.setMessage("What is the name of the list you want to import?");
        final EditText inp = new EditText(this);
        dialog.setView(inp);
        dialog.setPositiveButton("OK!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                importList2(inp.getText().toString());
            }
        });
        dialog.setNegativeButton("Cancel", null);
        dialog.show();
        //TODO Parse file and add list

    }
    public void importList2(String listname) {
        final List<String> lists = Util.loadLists(this);
        if(!lists.contains(listname)){
            save();
            Util.addTable(listname, this);
        }
        options.list = listname;
        saveOptions(options, this);
        tasks.clear();

        String filename = listname+".txt";
        File file = new File(Environment.getExternalStorageDirectory().getPath()+"/TodoQueue/"+filename);
        if(!file.exists()){
            message("Can't find file, name it \"listname.txt\"", this);
            return;
        }

        StringBuilder data = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file)) ) {
            String line;

            while ((line = br.readLine()) != null) {
                data.append(line);
                data.append('\n');
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] list = data.toString().split("\n----\n");
        for(String task: list){
            String[] parts = task.split(";;;\n");
            if(parts.length==3){
                tasks.add(parts[0], parts[1], Integer.parseInt(parts[2]));
            }
        }
        message("Import complete", this);
        update();
    }
    public void exportList() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
        permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            return;
        }

        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            message("External storage unavailable", this);
            return;
        }

        String filename = options.list+".txt";
        File dir = new File(Environment.getExternalStorageDirectory().getPath()+"/TodoQueue");
        if(!dir.exists())
            dir.mkdir();
        File file = new File(Environment.getExternalStorageDirectory().getPath()+"/TodoQueue/"+filename);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                message("Couldn't create file", this);
                e.printStackTrace();
                return;
            }
        }
        try (FileOutputStream fo = new FileOutputStream(file, false); PrintWriter pw = new PrintWriter(fo)){
            for(Task t:tasks.getAll()){
                pw.append(t.getName()+";;;\n");
                pw.append((t.getDescription()+";;;\n"));
                pw.append(t.getPriority()+"\n----\n");
            }
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            message("Could not write to file", this);
            e.printStackTrace();
        }
        message("Exported "+options.list, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    importList();
                }else
                    message("Read/Write to external storage needed for import/export", this);
                break;
            case 2:
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    exportList();
                }else
                    message("Read/Write to external storage needed for import/export", this);
                break;
        }
    }

    abstract void shareData();

    private String padTime(int time){
        String t = Integer.toString(time);
        return t.length()==1?"0"+t:t;
    }
    void showTimeDialog(){
        final Calendar c = Calendar.getInstance();
        final int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        final int minuteOfDay = c.get(Calendar.MINUTE);
        TimePickerDialog tp = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minute) {
                Calendar calSet = (Calendar) c.clone();
                calSet.set(Calendar.HOUR_OF_DAY, hour);
                calSet.set(Calendar.MINUTE, minute);
                calSet.set(Calendar.SECOND, 0);
                calSet.set(Calendar.MILLISECOND, 0);
                String alarmMessage = "Alarm set for "+padTime(calSet.get(Calendar.HOUR_OF_DAY))+":"+padTime(calSet.get(Calendar.MINUTE));
                if (hour<hourOfDay || (hour==hourOfDay && minute<=minuteOfDay)) {
                    calSet.add(Calendar.HOUR, 24);
                    alarmMessage+=" tomorrow";
                }
                alarmLists(calSet, alarmMessage);
            }
        }, hourOfDay, minuteOfDay, true);

        tp.setMessage("Set new alarm");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String currentAlarm = prefs.getString("Alarm", null);

        if (currentAlarm!=null){
            String setMessage = "Edit existing alarm for " + currentAlarm;
            tp.setMessage(setMessage);


            tp.setButton(DialogInterface.BUTTON_NEUTRAL, "Remove", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setAlarm(null, "Alarm removed", null);
                }
            });
        }

        tp.show();
    }

    static int chosen;
    void alarmLists(final Calendar c, final String m){
        final AlertDialog.Builder builderLists = new AlertDialog.Builder(this);
        builderLists.setTitle("Do you want the notification to show a specific list?");
        final List<String> lists = Util.loadLists(this);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, lists);
        chosen = arrayAdapter.getPosition(options.list);
        builderLists.setSingleChoiceItems(arrayAdapter, arrayAdapter.getPosition(options.list), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                chosen = which;
            }
        });
        builderLists.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String list = lists.get(chosen);
                setAlarm(c, m, list);
            }
        });
        builderLists.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setAlarm(c, m, null);
            }
        });
        builderLists.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builderLists.show();
    }

    void setAlarm(Calendar time, String message, String list){
        Intent alarmIntent = new Intent(BasicListActivity.this, NotificationReciever.class);
        alarmIntent.setAction("Alarm");
        alarmIntent.putExtra(NotificationReciever.NOTIFICATION_LIST, list);
        PendingIntent pi = PendingIntent.getBroadcast(BasicListActivity.this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        if(time!=null) {
            am.cancel(pi);
            am.set(AlarmManager.RTC, time.getTimeInMillis(), pi);
            Util.message(message, this);
            editor.putString("AlarmList", list);
            editor.putString("Alarm", time.get(Calendar.HOUR_OF_DAY)+":"+time.get(Calendar.MINUTE));

        } else if (prefs.getString("Alarm", null)!=null) {
            am.cancel(pi);
            Util.message(message, this);
            editor.putString("Alarm", null);
        }
        editor.apply();
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
                if("Add new".equals(strName)){
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

    boolean edit = false;
    void addDialog(final Task t, final boolean add){
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                edit = false;
            }
        });
        b.setTitle(getResources().getString(add?R.string.add_new_lbl:R.string.edit_lbl));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);


        final EditText inTask = new EditText(this);
        inTask.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        InputFilter[] filters = new InputFilter[2];
        filters[0] = new InputFilter.LengthFilter(15);
        filters[1] = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                boolean keepOriginal = true;
                StringBuilder sb = new StringBuilder(end - start);
                for (int i = start; i < end; i++) {
                    char c = source.charAt(i);
                    if (isCharAllowed(c)) // put your condition here
                        sb.append(c);
                    else
                        keepOriginal = false;
                }
                if (keepOriginal)
                    return null;
                else {
                    if (source instanceof Spanned) {
                        SpannableString sp = new SpannableString(sb);
                        TextUtils.copySpansFrom((Spanned) source, start, end, null, sp, 0);
                        return sp;
                    } else {
                        return sb;
                    }
                }
            }

            private boolean isCharAllowed(char c) {
                return c != '\n';
            }
        };
        inTask.setFilters(filters);
        inTask.setHint(getResources().getString(R.string.name));
        layout.addView(inTask);

        final EditText inDesc = new EditText(this);
        inDesc.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        inDesc.setSingleLine(false);
        inDesc.setHint(getResources().getString(R.string.desc));
        layout.addView(inDesc);

        LinearLayout addLin = new LinearLayout(this);
        TextView prioTitle = new TextView(this);
        prioTitle.setText(R.string.title_add_priority);

        prioTitle.setPadding(20, 20, 20, 20);

        final SeekBar prioBar = new SeekBar(this);
        prioBar.setMax(4);
        prioBar.setProgress(t.getPriority());

        final TextView prioTf = new TextView(this);
        prioTf.setText(Task.prioText(prioBar.getProgress()));

        addLin.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addLin.addView(prioTitle);
        addLin.addView(prioTf);

        layout.addView(addLin);
        layout.addView(prioBar);

        inTask.setText(t.getName());
        inDesc.setText(t.getDescription());


        prioBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prioTf.setText(Task.prioText(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        b.setView(layout);
        b.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                t.setName(inTask.getText().toString());
                t.setDescription(inDesc.getText().toString());
                t.setPriority(prioBar.getProgress());
                edited(add, t);
                edit = false;
            }
        });
        b.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        b.create().show();
    }

    abstract void edited(boolean add, Task t);

    @Override
    protected void onPause() {
        super.onPause();
        Util.running = false;
        save();
        if (options.notification && tasks.size() != 0)
            NotificationReciever.showNotification(tasks.getFirst().getName(), tasks.getFirst().getDescription(), tasks.getFirst().getColorId(), this, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Util.running = true;
        load();
        Log.i("options", Boolean.toString(options.notification));
        setNotif(options.notification);
        NotificationReciever.cancelNotification(this);
    }
}
