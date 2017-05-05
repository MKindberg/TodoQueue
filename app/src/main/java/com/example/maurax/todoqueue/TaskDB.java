package com.example.maurax.todoqueue;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.UrlQuerySanitizer;
import android.util.Log;

import java.util.LinkedList;

import static android.R.attr.version;
import static android.os.Build.VERSION_CODES.M;
import static android.os.FileObserver.CREATE;

/**
 * Created by marcus on 30/04/2017.
 */

public class TaskDB extends SQLiteOpenHelper {

    public static final String DEFALT_TABLE_NAME = "List1";

    public static final String TABLE_LISTS_NAME = "Lists";
    public static final String TABLE_OPTIONS_NAME = "Options";

    public static final String COLUMN_LIST_NAME = "name";

    public static final String COLUMN_TASK_NAME = "name";
    public static final String COLUMN_TASK_DESC = "description";
    public static final String COLUMN_TASK_PRIO = "prio";

    public static final String COLUMN_OPTIONS_COLOR = "color";
    public static final String COLUMN_OPTIONS_NOTIFICATION = "notification";
    public static final String COLUMN_OPTIONS_LIST = "list";

    public static final String DB_TASK_NAME = "task.db";
    private static final int DB_TASK_VERSION = 1;

    private static final String DATABASE_LISTS_CREATE = "create table "
            + TABLE_LISTS_NAME  + "( "
            + COLUMN_LIST_NAME + " text);";

    private static final String DATABASE_OPTIONS_CREATE = "create table "
            + TABLE_OPTIONS_NAME  + "( "
            + COLUMN_OPTIONS_COLOR + " text, "
            + COLUMN_OPTIONS_NOTIFICATION + " text, "
            + COLUMN_OPTIONS_LIST + " text);";

    public TaskDB(Context context) {
        super(context, DB_TASK_NAME, null, DB_TASK_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_LISTS_CREATE);
        db.execSQL(DATABASE_OPTIONS_CREATE);
        db.execSQL("insert into " + TABLE_OPTIONS_NAME + " values (0, 0, '"+DEFALT_TABLE_NAME+"');");

        ContentValues cv = new ContentValues();
        addTable(db, DEFALT_TABLE_NAME);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTS_NAME);
        onCreate(db);
    }

    public String[] getAllLists(SQLiteDatabase db){
        Cursor res = db.rawQuery("select * from "+ TABLE_LISTS_NAME, null);
        String[] lists = new String[res.getCount()];
        res.moveToNext();
        for(int i=0;i<lists.length;i++){
            lists[i] = res.getString(0);
            res.moveToNext();
        }
        return lists;
    }

    public void addTable(SQLiteDatabase db, String name){
        // TODO Sanitize input
        String query ="create table if not exists"
                + "'" + name  +"' ( "
                + COLUMN_TASK_NAME + " text, "
                + COLUMN_TASK_DESC + " text, "
                + COLUMN_TASK_PRIO + " integer);";
        db.execSQL(query);

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_LIST_NAME, name);
        db.insert(TABLE_LISTS_NAME, null, cv);
    }

    public void deleteTable(SQLiteDatabase db, String name){
        String query = "drop table "+ "'" + name +"';";
        db.execSQL(query);
        db.delete(TABLE_LISTS_NAME, COLUMN_LIST_NAME + "= '" + name+"'", null);
    }

    public void fillTable(SQLiteDatabase db, String table, Tasks tasks){
        // TODO Sanitize input
        LinkedList<Task> tsks= tasks.getAll();
        db.execSQL("delete from "+ "'" + table+"'");
        for(Task t:tsks){
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_TASK_NAME, t.getName());
            cv.put(COLUMN_TASK_DESC, t.getDescription());
            cv.put(COLUMN_TASK_PRIO, t.getPriority());
            db.insert("'"+table+"'", null, cv);
        }
    }

    public Tasks getTasks(SQLiteDatabase db, String table){
        Cursor res = db.rawQuery("select * from "+ "'" + table+"';", null);
        LinkedList<Task> tasks = new LinkedList<Task>();
        int len = res.getCount();
        while (res.moveToNext()){
            String name = res.getString(res.getColumnIndex(COLUMN_TASK_NAME));
            String desc = res.getString(res.getColumnIndex(COLUMN_TASK_DESC));
            int prio = res.getInt(res.getColumnIndex(COLUMN_TASK_PRIO));
            tasks.add(new Task(name, desc, prio));
        }
        Tasks t = new Tasks(tasks);
        return t;
    }

    public void saveOptions(SQLiteDatabase db, Options options){
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_OPTIONS_COLOR, Boolean.toString(options.colors));
        cv.put(COLUMN_OPTIONS_NOTIFICATION, Boolean.toString(options.notification));
        cv.put(COLUMN_OPTIONS_LIST, options.list);
        db.delete(TABLE_OPTIONS_NAME, null, null);
        db.insert(TABLE_OPTIONS_NAME, null, cv);
    }

    public Options getOptions(SQLiteDatabase db){
        Options options = new Options();
        Cursor res = db.rawQuery("select * from '"+TABLE_OPTIONS_NAME+"';", null);
        res.moveToNext();
        options.colors = Boolean.parseBoolean(res.getString(res.getColumnIndex(COLUMN_OPTIONS_COLOR)));
        options.notification = Boolean.parseBoolean(res.getString(res.getColumnIndex(COLUMN_OPTIONS_NOTIFICATION)));
        options.list = res.getString(res.getColumnIndex(COLUMN_OPTIONS_LIST));

        return options;
    }
}
