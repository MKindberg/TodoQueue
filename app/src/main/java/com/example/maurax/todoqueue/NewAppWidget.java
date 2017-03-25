package com.example.maurax.todoqueue;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

/**
 * Implementation of App Widget functionality.
 */
public class NewAppWidget extends AppWidgetProvider {

    private Tasks t;
    private Context con;

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = t.getFirst().getName();
        int col = t.getFirst().getColorId();
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);
        views.setTextColor(R.id.appwidget_text, ContextCompat.getColor(con, col));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        con = context;
        load();
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public void load(){
        StringBuilder temp = new StringBuilder();
        try {
            InputStream is = con.openFileInput("data");
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String rec;

                while ((rec = br.readLine()) != null) {
                    temp.append(rec);
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
            ll.add(new Task(data[i].substring(1), data[i + 1].substring(1), Integer.parseInt(data[i + 2])));
        t = new Tasks(ll);
    }
}

