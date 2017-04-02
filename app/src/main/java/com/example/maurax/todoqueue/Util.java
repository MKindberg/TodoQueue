package com.example.maurax.todoqueue;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;

/**
 * Created by marcus on 2017-04-01.
 */

public class Util {

    public static void updateWidget(Context con) {
        Intent i = new Intent(con, SimpleBackAppWidget.class);
        i.setAction(SimpleBackAppWidget.ACTION_UPDATE);
        con.sendBroadcast(i);
    }

    public static void saveTasks(Tasks t, Context con) {
        String filePath = con.getFilesDir().toString();

        LinkedList<Task> tsks = t.getAll();
        try {
            new File(filePath + "data").createNewFile();
            OutputStreamWriter os = new OutputStreamWriter(con.openFileOutput("data", Context.MODE_PRIVATE));
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
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveOptions(boolean colors, boolean notification, Context con) {
        String filePath = con.getFilesDir().toString();

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("colors ").append(Boolean.toString(colors));
            sb.append("\n");
            sb.append("notification ").append(Boolean.toString(notification));

            new File(filePath + "settings").createNewFile();
            OutputStreamWriter os = new OutputStreamWriter(con.openFileOutput("settings", Context.MODE_PRIVATE));
            os.write(sb.toString());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Tasks loadTasks(Context con) {
        if (!new File(con.getFilesDir().toString() + "data").exists()) {
            return new Tasks();
        }

        StringBuilder temp = new StringBuilder();
        try {
            InputStream is = con.openFileInput("data");
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String rec;

                while ((rec = br.readLine()) != null) {
                    temp.append(rec + "\n");
                }
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String d = temp.toString();
        String[] data = d.split("--");
        LinkedList<Task> ll = new LinkedList<>();
        for (int i = 0; i < data.length - 2; i += 3)
            ll.add(new Task(data[i].trim().substring(1), data[i + 1].trim().substring(1), Integer.parseInt(data[i + 2].trim())));
        return new Tasks(ll);
    }

    public static boolean[] loadOptions(Context con) {
        boolean colors = false;
        boolean notification = false;
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
                        colors = Boolean.parseBoolean(set[1]);
                        break;
                    case "notification":
                        notification = Boolean.parseBoolean(set[1]);
                        break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new boolean[]{colors, notification};
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
}
