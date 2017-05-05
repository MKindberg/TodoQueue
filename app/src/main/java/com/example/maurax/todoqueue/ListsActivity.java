package com.example.maurax.todoqueue;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import static com.example.maurax.todoqueue.Util.message;

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

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
        final String[] lists = Util.loadLists(this);
        arrayAdapter.addAll(lists);
        arrayAdapter.add("Add new");

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
                            else if(Util.contains(lists, name)) {
                                Util.message("List name must be unique", con);
                                Log.i("Contains", "True");
                            }
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
                dialog.dismiss();
            }
        });

        builderSingle.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setNeutralButton("Delete", null);

        final AlertDialog dialog = builderSingle.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (arrayAdapter.getCount() == 2) {
                    Util.message("You can't delete the last list", con);
                    return;
                }
                dialog.dismiss();
                final AlertDialog.Builder builderDelete = new AlertDialog.Builder(con);
                builderDelete.setTitle("Are you sure you want to delete the list " + options.list + "?");
                builderDelete.setNegativeButton("Calcel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
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
                    }
                });
                builderDelete.show();
            }
        });

        //builderSingle.show();

    }
}
