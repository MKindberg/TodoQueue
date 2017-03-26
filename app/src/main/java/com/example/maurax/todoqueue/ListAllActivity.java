package com.example.maurax.todoqueue;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.PopupMenu;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;

public class ListAllActivity extends AppCompatActivity {
    private LinkedList<Task> l;
    private Tasks t;
    private ListView lv;
    private ArrayAdapter<Task> aa;

    private int focused = -1;

    private PopupMenu popup;
    private boolean colors;
    private boolean notification;

    private String filePath;

    private float x1, x2, y1, y2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_all);
        Log.i("Test", "1");

        readIntent();

        lv = (ListView) findViewById(R.id.listView);
        assert lv != null;
        Log.i("Test", "2");
        aa = new ListAllAdapter(this, R.layout.listitem, l);
        lv.setAdapter(aa);
        Log.i("Test", "3");
        filePath = getFilesDir().toString();



        createMenu();

        setListeners();
        Log.i("Test", "4");
        checkTutorial();
//        update();
        lv.post(new Runnable() {
            @Override
            public void run() {
                color();
            }
        });
        Log.i("Test", "5");
    }

    private void createMenu() {
        popup = new PopupMenu(this, findViewById(R.id.buttonMenu));
        popup.getMenuInflater().inflate(R.menu.menu_list, popup.getMenu());
        popup.getMenu().findItem(R.id.colorsOp).setChecked(colors);
        popup.getMenu().findItem(R.id.notifyOp).setChecked(notification);
    }

    private void readIntent() {
        Intent i = getIntent();
        t = i.getParcelableExtra("list");
        l = t.getAll();
        colors = i.getBooleanExtra("colors", false);
        notification = i.getBooleanExtra("notification", false);
    }

    private void setListeners() {
        View filler = findViewById(R.id.filler);
        final RelativeLayout relLay = (RelativeLayout) findViewById(R.id.relLay2);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (focused == -1) {
                    focused = position;
                    setFocus(position, true);
                } else if (focused == position) {

                    setFocus(focused, false);
                    focused = -1;
                } else {
                    setFocus(position, true);
                    setFocus(focused, false);
                    focused = position;
                }
            }


        });

        assert relLay != null;
        lv.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(relLay.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    back();
                    return super.onDoubleTap(e);
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    tutorial();
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return event.getAction() != MotionEvent.ACTION_UP && focused != -1;
            }
        });

        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                color();
            }
        });

        filler.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(relLay.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    back();
                    return super.onDoubleTap(e);
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    tutorial();
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                if (event.getAction()==MotionEvent.ACTION_UP && focused != -1) {
                    setFocus(focused, false);
                    focused = -1;
                }
                return true;
            }
        });

    }

    private void checkTutorial() {
        File f = new File(filePath + "tutorial2");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            tutorial();
        }
    }

    public void menu(View v) {
        popup.show();

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.tutorialOp:
                        tutorial();
                        return true;
                    case R.id.sortOp:
                        System.out.println("Sorting");
                        Task tsk = null;
                        if (focused != -1) {
                            tsk = aa.getItem(focused);
                        }
                        t = new Tasks(l);
                        t.sort();
                        l = t.getAll();
                        update();
                        if (focused != -1) {
                            setFocus(focused, false);
                            for (int i = 0; i < aa.getCount(); i++)
                                if (tsk == aa.getItem(i)) {
                                    focused = i;
                                    break;
                                }
                            setFocus(focused, true);
                        }
                        return true;
                    case R.id.colorsOp:
                        item.setChecked(!item.isChecked());
                        colors = item.isChecked();
                        color();
                        popup.show();
                        return true;
                    case R.id.notifyOp:
                        item.setChecked(!item.isChecked());
                        notification = item.isChecked();
                        popup.show();
                        return true;
                    default:
                        return false;
                }
            }
        });

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                return super.dispatchTouchEvent(event);
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                y2 = event.getY();

                float diffX = x2 - x1;
                float diffY = y2 - y1;

                if (Math.abs(diffX) < 100 && Math.abs(diffY) < 100)//click
                    return super.dispatchTouchEvent(event);
                if (focused != -1) {
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (x1 < x2)//swipe right
                            complete();
                        else//swipe left
                            edit();
                    } else {
                        if (y1 < y2)//swipe down
                            moveDown();
                        else//swipe up
                            moveUp();
                    }
                    return true;
                }
        }
        return super.dispatchTouchEvent(event);
    }

    public void setFocus(int pos, boolean focus) {
        if(pos == 0)
            lv.setSelectionAfterHeaderView();
        else if(pos<=lv.getFirstVisiblePosition())
            lv.setSelection(pos-1);
        else if(pos>=lv.getLastVisiblePosition())
            lv.setSelection(1+pos-(lv.getLastVisiblePosition()-lv.getFirstVisiblePosition()));
        //else if(pos == lv.getCount()-1)
        //    lv.setSelection
        //if(pos>=lv.getLastVisiblePosition() && pos<lv.getCount()-1)
        //    lv.setSelection(pos);
        //else if(pos<=lv.getFirstVisiblePosition() && pos>0)
        //    lv.setSelection(pos);
        pos -= lv.getFirstVisiblePosition();

        if (focus) {
            int col = ((ColorDrawable) lv.getChildAt(pos).getBackground()).getColor();
            float[] hsv = new float[3];
            Color.colorToHSV(col, hsv);
            hsv[2] *= 0.8;
            lv.getChildAt(pos).setBackgroundColor(Color.HSVToColor(hsv));
            lv.getChildAt(pos).setElevation(30);
        } else {
            int col = ((ColorDrawable) lv.getChildAt(pos).getBackground()).getColor();
            float[] hsv = new float[3];
            Color.colorToHSV(col, hsv);
            hsv[2] *= 1.25;
            lv.getChildAt(pos).setBackgroundColor(Color.HSVToColor(hsv));
            lv.getChildAt(pos).setElevation(0);
        }
    }

    private void color() {
        if (colors) {
            for (int i = 0; i < aa.getCount(); i++) {
                View v = lv.getChildAt(i);
                if(v==null)
                    continue;
                int colId;
                switch (aa.getItem(i).getPriority()) {
                    case 1:
                        colId = R.color.prio1;
                        break;
                    case 2:
                        colId = R.color.prio2;
                        break;
                    case 3:
                        colId = R.color.prio3;
                        break;
                    case 4:
                        colId = R.color.prio4;
                        break;
                    case 5:
                        colId = R.color.prio5;
                        break;
                    default:
                        colId = R.color.noPrio;
                        break;
                }
                v.setBackgroundColor(ContextCompat.getColor(this, colId));

            }
        } else {
            for (int i = 0; i < aa.getCount(); i++) {
                View v = lv.getChildAt(i);
                if(v!=null)
                    v.setBackgroundColor(ContextCompat.getColor(this, R.color.noPrio));
            }
        }
        if (focused != -1)
            setFocus(focused, true);
    }

    public void moveUp() {
        if (focused > 0) {
            setFocus(focused, false);
            l.add(focused - 1, l.remove(focused));
            focused--;
            update();
            //setFocus(focused, true);
        } else if (focused == -1)
            message(getResources().getString(R.string.lv_please_select));
        else
            message(getResources().getString(R.string.lv_cant_up));
    }

    public void moveUp(View v) {
        moveUp();
    }

    public void moveDown() {
        if (focused != -1 && focused != l.size() - 1) {
            l.add(focused + 1, l.remove(focused));
            setFocus(focused, false);
            focused++;
            update();
            //setFocus(focused, true);
        } else if (focused == -1)
            message(getResources().getString(R.string.lv_please_select));
        else
            message(getResources().getString(R.string.lv_cant_down));
    }

    public void moveDown(View v) {
        moveDown();
    }

    public void complete() {
        if (focused != -1) {
            l.remove(focused);
            setFocus(focused, false);
            focused = -1;
            update();
        } else
            message(getResources().getString(R.string.lv_please_select));
    }

    public void complete(View v) {
        complete();
    }

    public void edit() {
        if (focused != -1) {
            final Task tsk = l.get(focused);
            String desc;
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle(getResources().getString(R.string.add_new_lbl));

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);

            final EditText inTask = new EditText(this);
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
            inTask.setText(tsk.getName());
            layout.addView(inTask);

            final EditText inDesc = new EditText(this);
            inDesc.setHint(getResources().getString(R.string.desc));
            inDesc.setText(tsk.getDescription());
            layout.addView(inDesc);

            LinearLayout addLin = new LinearLayout(this);
            TextView prioTitle = new TextView(this);
            prioTitle.setText("Priority (1 is urgent)");
            layout.addView(prioTitle);
            prioTitle.setPadding(20, 20, 20, 20);

            final SeekBar prioBar = new SeekBar(this);
            prioBar.setMax(4);
            prioBar.setProgress(tsk.getPriority() - 1);

            final TextView prioTf = new TextView(this);
            prioTf.setText(Integer.toString(prioBar.getProgress() + 1));

            addLin.addView(prioBar);
            addLin.addView(prioTf);
            layout.addView(addLin);
            prioBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 10));
            prioTf.setLayoutParams(new LinearLayout.LayoutParams(50, ViewGroup.LayoutParams.MATCH_PARENT, 1));

            prioBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    prioTf.setText((progress + 1) + "");
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
                    tsk.setName(inTask.getText().toString());
                    tsk.setDescription(inDesc.getText().toString());
                    tsk.setPriority(prioBar.getProgress() + 1);
                    update();
                }
            });
            b.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            b.create().show();
        } else
            message(getResources().getString(R.string.lv_please_select));
    }

    public void edit(View v) {
        edit();
    }

    public void back() {
        t = new Tasks(l);
        Intent i = new Intent(ListAllActivity.this, MainActivity.class);
        i.putExtra("sender", "listAll");
        i.putExtra("list", (Parcelable) t);
        i.putExtra("colors", colors);
        i.putExtra("notification", notification);

        ListAllActivity.this.startActivity(i);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void update() {
        aa.notifyDataSetChanged();
        color();
    }

    public void message(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    public void tutorial() {
        message("Tutorial");
        /*final File f = new File(filePath + "tutorial2");
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setMessage(getResources().getString(R.string.instructions) + ":\n" +
                "1. " + getResources().getString(R.string.lv_tutorial_down) +
                "\n2. " + getResources().getString(R.string.lv_tutorial_right) +
                "\n3. " + getResources().getString(R.string.lv_tutorial_up) +
                "\n4. " + getResources().getString(R.string.lv_tutorial_left) +
                "\n5. " + getResources().getString(R.string.lv_tutorial_double) +
                "\n6. " + getResources().getString(R.string.tutorial_long));
        b.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        b.setCancelable(true);
        b.create().show();*/
    }

    public void showNotification() {
        NotificationCompat.Builder nBuild = new NotificationCompat.Builder(this);
        nBuild.setContentTitle(t.getFirst().getName());
        nBuild.setContentText(t.getFirst().getDescription());
        nBuild.setSmallIcon(android.R.drawable.ic_popup_reminder);
        nBuild.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        Intent resInt = new Intent(this, MainActivity.class);
        PendingIntent pInt = PendingIntent.getActivity(this, 0, resInt, PendingIntent.FLAG_UPDATE_CURRENT);
        nBuild.setContentIntent(pInt);

        nBuild.setOngoing(true);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, NotificationReciever.class).putExtra("Action", "complete"), PendingIntent.FLAG_UPDATE_CURRENT);
        nBuild.addAction(android.R.drawable.ic_popup_reminder, "complete", pendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, nBuild.build());
    }

    public void cancelNotification() {
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(1);
    }

    public void save() {
        LinkedList<Task> tsks = t.getAll();
        try {
            new File(filePath + "data").createNewFile();
            OutputStreamWriter os = new OutputStreamWriter(this.openFileOutput("data", Context.MODE_PRIVATE));
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
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("colors ").append(Boolean.toString(colors));
            sb.append("\n");
            sb.append("notification ").append(Boolean.toString(notification));

            new File(filePath + "settings").createNewFile();
            OutputStreamWriter os = new OutputStreamWriter(this.openFileOutput("settings", Context.MODE_PRIVATE));
            os.write(sb.toString());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        StringBuilder temp = new StringBuilder();
        try {
            InputStream is = openFileInput("data");
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String rec;

                while ((rec = br.readLine()) != null) {
                    temp.append(rec+"\n");
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
        t = new Tasks(ll);
    }

    @Override
    protected void onPause() {
        super.onPause();
        save();
        if (notification && t.size() != 0)
            NotificationReciever.showNotification(t.getFirst().getName(), t.getFirst().getDescription(), t.getFirst().getColorId(), this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
        NotificationReciever.cancelNotification(this);
    }
}

