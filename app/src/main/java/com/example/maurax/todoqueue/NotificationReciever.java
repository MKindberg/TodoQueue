package com.example.maurax.todoqueue;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;

import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

/**
 * Created by marcus on 2016-08-01.
 */
public class NotificationReciever extends BroadcastReceiver {

    private Tasks t;
    private Context con;
    private Options options;

    public static String NOTIFICATION_LIST = "List";

    public static void showNotification(String name, String desc, int colorId, Context con, String list) {
        NotificationCompat.Builder nBuild = new NotificationCompat.Builder(con);
        nBuild.setContentTitle(name);
        nBuild.setContentText(desc);

        nBuild.setSmallIcon(R.drawable.checkmark);
        nBuild.setLargeIcon(BitmapFactory.decodeResource(con.getResources(), R.mipmap.ic_launcher));

        Intent resInt = new Intent(con, MainActivity.class);
        resInt.putExtra(NOTIFICATION_LIST, list);
        PendingIntent pInt = PendingIntent.getActivity(con, 0, resInt, PendingIntent.FLAG_UPDATE_CURRENT);
        nBuild.setContentIntent(pInt);

        nBuild.setOngoing(true);

        Intent intentPostpone = new Intent(con, NotificationReciever.class);
        Intent intentPutLast = new Intent(con, NotificationReciever.class);
        Intent intentComplete = new Intent(con, NotificationReciever.class);
        intentPostpone.putExtra("action", "postpone");
        intentPutLast.putExtra("action", "put last");
        intentComplete.putExtra("action", "complete");
        intentPostpone.putExtra(NOTIFICATION_LIST, list);
        intentPutLast.putExtra(NOTIFICATION_LIST, list);
        intentComplete.putExtra(NOTIFICATION_LIST, list);

        PendingIntent pendingPostpone = PendingIntent.getBroadcast(con, 1, intentPostpone, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingPutLast = PendingIntent.getBroadcast(con, 2, intentPutLast, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingComplete = PendingIntent.getBroadcast(con, 3, intentComplete, PendingIntent.FLAG_UPDATE_CURRENT);

        nBuild.addAction(0, "Postpone", pendingPostpone);
        nBuild.addAction(0, "Put last", pendingPutLast);
        nBuild.addAction(0, "Complete", pendingComplete);

        nBuild.setColor(ContextCompat.getColor(con, colorId));
        NotificationManager mNotificationManager =
                (NotificationManager) con.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, nBuild.build());
    }

    public static void cancelNotification(Context con) {
        ((NotificationManager) con.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(1);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        con = context;
        String list = intent.getStringExtra(NOTIFICATION_LIST);
        load(list);
        if (intent.getAction()=="Alarm") {
            options.notification = true;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(con);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("Alarm", null);
            editor.apply();
            if (Util.running) {
                Util.saveOptions(options, con);
                return;
            }
        }
        if(intent.getStringExtra("action")!=null)
            switch (intent.getStringExtra("action")) {
                case "put last":
                    t.toLast();
                    break;
                case "postpone":
                    t.postpone();
                    break;
                case "complete":
                    t.complete();
                    break;
            }
        Task tsk = t.getFirst();
        if (tsk != null) {
            showNotification(tsk.getName(), tsk.getDescription().replace("\n", "  //  "), tsk.getColorId(), con, list);
        } else
            cancelNotification(con);

        save(list);
    }

    private void load(String list) {
        options = Util.loadOptions(con);
        t = Util.loadTasks(list!=null?list:options.list, con);
    }


    private void save(String list) {
        Util.saveTasks(t, list!=null?list:options.list, con);
        Util.saveOptions(options, con);
        Util.updateWidget(con);
    }
}
