package com.example.maurax.todoqueue;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class SimpleBackAppWidget extends AppWidgetProvider {

    private Tasks t;
    private Context con;

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        CharSequence widgetText = "";
        int col = R.color.transparent;
        if (t.getFirst() != null) {
            widgetText = t.getFirst().getName();
            col = ContextCompat.getColor(con, t.getFirst().getColorId());
        }
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.simple_app_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        views.setInt(R.id.appwidget_text, "setBackgroundColor", col);


        Intent intent = new Intent(con, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);

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

    private void load() {
        Options op = Util.loadOptions(con);
        t = Util.loadTasks(op.list, con);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().equals(Util.ACTION_UPDATE)) {
            AppWidgetManager awm = AppWidgetManager.getInstance(context);
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), SimpleBackAppWidget.class.getName());
            int[] appWidgetIds = awm.getAppWidgetIds(thisAppWidget);
            onUpdate(context, awm, appWidgetIds);
        }
    }
}

