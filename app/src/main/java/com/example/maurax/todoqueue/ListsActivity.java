package com.example.maurax.todoqueue;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.RadioButton;

import java.util.List;

import static android.R.id.list;
import static com.example.maurax.todoqueue.Util.message;
import static com.example.maurax.todoqueue.Util.saveLists;
import static java.util.Collections.swap;

/**
 * Created by marcus on 05/05/2017.
 */

public abstract class ListsActivity extends AppCompatActivity {

    Options options;
    Tasks tasks;

    public abstract void update();
    public abstract void save();
    public abstract void load();
    

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
                                Log.i("Contains", "False");
                                save();
                                options.list = name;
                                Util.addTable(name, con);
                                Util.saveOptions(options, con);
                                load();
                                arrayAdapter.insert(name, arrayAdapter.getCount()-1);
                                arrayAdapter.notifyDataSetChanged();
                                Log.i("ListsAct", "update");
                                update();
                            }
                        }
                    });
                    builderInner.show();
                }else{
                    save();
                    options.list = strName;
                    Util.saveOptions(options, con);
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
                        Util.removeTable(options.list, con);
                        arrayAdapter.remove(options.list);
                        options.list = arrayAdapter.getItem(0);
                        arrayAdapter.notifyDataSetChanged();
                        Util.saveOptions(options, con);
                        load();
                        dialog.getListView().setItemChecked(0, true);
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
                    Util.saveLists(lists, ListsActivity.this);
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

                AlertDialog.Builder editBuilder = new AlertDialog.Builder(ListsActivity.this);
                final EditText edittext = new EditText(ListsActivity.this);
                editBuilder.setMessage("Enter new name");
                final String old = lists.get(position);
                edittext.setText(old);

                editBuilder.setView(edittext);

                editBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String name = edittext.getText().toString();
                        if(name.isEmpty())
                            message("List must have a name", ListsActivity.this);
                        else if(!name.equals(old) && lists.contains(name))
                            message("Name must be unique", ListsActivity.this);
                        else {
                            if (old.equals(options.list))
                                options.list = name;

                            lists.set(position, name);
                            lists.remove(lists.size()-1);
                            Tasks t = Util.loadTasks(old, ListsActivity.this);
                            Util.removeTable(old, ListsActivity.this);
                            Util.addTable(name, ListsActivity.this);
                            Util.saveTasks(t, name, ListsActivity.this);
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
}
