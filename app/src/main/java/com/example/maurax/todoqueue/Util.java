package com.example.maurax.todoqueue;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;

/**
 * Created by marcus on 2017-04-01.
 */

class Util {

    private static TaskDB mHelper;

    public static final String ACTION_UPDATE = "UPDATE";

    public static void updateNotification(Context con){
        Intent i = new Intent(con, NotificationReciever.class);
        i.setAction(ACTION_UPDATE);
        con.sendBroadcast(i);
    }

    public static void updateWidget(Context con) {
        Intent i = new Intent(con, SimpleBackAppWidget.class);
        i.setAction(ACTION_UPDATE);
        con.sendBroadcast(i);
    }

    public static void saveTasks(Tasks t, String name, Context con){
        if(mHelper==null)
            mHelper = new TaskDB(con);
        SQLiteDatabase db = mHelper.getWritableDatabase();
        mHelper.fillTable(db, name, t);
        db.close();
    }
        /*String filePath = con.getFilesDir().toString();

        LinkedList<Task> tsks = t.getAll();
        try {
            if(!new File(filePath).exists())
                new File(filePath).mkdir();
            File f = new File(filePath + name);
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);
            OutputStreamWriter os = new OutputStreamWriter(fos);
            StringBuilder sb = new StringBuilder();
            for (Task tsk : tsks) {
                sb.append("T").append(tsk.getName());
                sb.append("\n--\n");
                sb.append("D").append(tsk.getDescription());
                sb.append("\n--\n");
                sb.append(tsk.getPriority());
                sb.append("\n--\n");
            }
            os.write(sb.toString());
            os.close();
            fos.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }*/

    /**
     * Loads selected list from file
     * @param name Name of list
     * @param con
     * @return List as Tasks object, new Tasks object if non existing
     */
    public static Tasks loadTasks(String name, Context con) {
        if(mHelper==null)
            mHelper = new TaskDB(con);
        SQLiteDatabase db = mHelper.getWritableDatabase();
        Tasks t = mHelper.getTasks(db, name);
        db.close();return t;
        /*if (!new File(con.getFilesDir().toString() + "lists"+File.pathSeparator+name).exists()) {
            return new Tasks();
        }

        StringBuilder temp = new StringBuilder();
        try {
            FileInputStream fis = new FileInputStream(con.getFilesDir().toString()+"lists"+File.pathSeparator+name);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String rec;

            while ((rec = br.readLine()) != null) {
                temp.append(rec + "\n");
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String d = temp.toString();
        String[] data = d.split("--");
        LinkedList<Task> ll = new LinkedList<>();
        for (int i = 0; i < data.length - 2; i += 3)
            ll.add(new Task(data[i].trim().substring(1), data[i + 1].trim().substring(1), Integer.parseInt(data[i + 2].trim())));
        return new Tasks(ll);*/
    }

    public static void saveOptions(Options options, Context con) {
        if(mHelper==null)
            mHelper = new TaskDB(con);
        SQLiteDatabase db = mHelper.getWritableDatabase();
        mHelper.saveOptions(db, options);
        db.close();
        /*String filePath = con.getFilesDir().toString();

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("colors ").append(Boolean.toString(options.colors));
            sb.append("\n");
            sb.append("notification ").append(Boolean.toString(options.notification));
            sb.append("\n");
            sb.append("list ").append(options.list);

            new File(filePath + "settings").createNewFile();
            OutputStreamWriter os = new OutputStreamWriter(con.openFileOutput("settings", Context.MODE_PRIVATE));
            os.write(sb.toString());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public static Options loadOptions(Context con) {
        if(mHelper==null)
            mHelper = new TaskDB(con);
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Options options = mHelper.getOptions(db);
        db.close();

        return options;
        /*Options res = new Options();
        try {
            InputStream is = con.openFileInput("settings");
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String rec;
            String[] set;
            while ((rec = br.readLine()) != null) {
                set = rec.split(" ");
                switch (set[0]) {
                    case "colors":
                        res.colors = Boolean.parseBoolean(set[1]);
                        break;
                    case "notification":
                        res.notification = Boolean.parseBoolean(set[1]);
                        break;
                    case "list":
                        res.list = set[1];
                        break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;*/
    }

    public static String[] loadLists(Context con){
        if(mHelper==null)
            mHelper = new TaskDB(con);
        SQLiteDatabase db = mHelper.getReadableDatabase();
        String[] l = mHelper.getAllLists(db);
        db.close();
        return l;
        /*
        File f = new File(con.getFilesDir()+"lists/");
        if (!f.exists())
            f.mkdir();
        message(Integer.toString(f.list().length), con);
        return f.list();*/
    }

    public static void addTable(String name, Context con){
        if(mHelper==null)
            mHelper = new TaskDB(con);
        SQLiteDatabase db = mHelper.getWritableDatabase();
        mHelper.addTable(db, name);
        db.close();
    }
    public static void removeTable(String name, Context con){
        if(mHelper==null)
            mHelper = new TaskDB(con);
        SQLiteDatabase db = mHelper.getWritableDatabase();
        mHelper.deleteTable(db, name);
        db.close();
    }


    public static void message(String message, Context con) {

        Toast toast = Toast.makeText(con, message, Toast.LENGTH_SHORT);
        toast.show();


        /*AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setMessage(message);
        b.setPositiveButton("OK!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        b.setCancelable(true);
        b.create().show();*/
    }

    public static boolean contains(String[] list, String item){
        for(String i:list)
            if (i.equals(item))
                return true;
            return false;
    }
}
