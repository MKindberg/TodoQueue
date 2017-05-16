package com.example.maurax.todoqueue;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import static android.R.id.edit;

public class ListAllActivity extends BasicListActivity {

    private ListView lv;
    private ListAllAdapter<Task> aa;

    private int focused = -1;

    private float x1;
    private float y1;


    void findViews(){

    }

    void setUp(){
        ((Button) findViewById(R.id.buttonBot)).setText(R.string.btn_move_down);
        ((Button) findViewById(R.id.buttonTop)).setText(R.string.btn_move_up);
        ((Button) findViewById(R.id.buttonLeft)).setText(R.string.btn_edit);
        ((Button) findViewById(R.id.buttonRight)).setText(R.string.btn_complete);

        lv = (ListView) findViewById(R.id.listView);
        assert lv != null;
        aa = new ListAllAdapter(this, R.layout.listitem, tasks.getAll());
        lv.setAdapter(aa);

        lv.post(new Runnable() {
            @Override
            public void run() {
                color();
            }
        });
    }

    void readIntent() {
        Intent i = getIntent();
        tasks = i.getParcelableExtra("tasks");
        options = i.getParcelableExtra("options");
    }

    void setListeners() {
        super.setListeners();
        ((Button) findViewById(R.id.buttonBot)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveDown();
            }
        });
        ((Button) findViewById(R.id.buttonTop)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveUp();
            }
        });
        ((Button) findViewById(R.id.buttonLeft)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit();
            }
        });
        ((Button) findViewById(R.id.buttonRight)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                complete();
            }
        });
        final View filler = findViewById(R.id.filler);
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
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                focused = position;
                setFocus(position, true);
                edit();
                return true;
            }
        });

        lv.setOnTouchListener(new View.OnTouchListener() {
            private final GestureDetector gestureDetector = new GestureDetector(relLay.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    back();
                    return super.onDoubleTap(e);
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    if (focused!=-1)
                        edit();

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
            private final GestureDetector gestureDetector = new GestureDetector(relLay.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    back();
                    return super.onDoubleTap(e);
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

//    private void checkTutorial() {
//        File f = new File(filePath + "tutorial2");
//        if (!f.exists()) {
//            try {
//                f.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            tutorial();
//        }
//    }

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
                        //tasks = new Tasks(l);
                        tasks.sort();
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
                        options.colors = item.isChecked();
                        color();
                        popup.show();
                        return true;
                    case R.id.listOp:
                        listDialog(ListAllActivity.this);
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
                float x2 = event.getX();
                float y2 = event.getY();

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

    private void setFocus(int pos, boolean focus) {
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

        if(lv.getChildAt(pos)==null)
            return;
        if (focus) {
            int col = ((ColorDrawable) lv.getChildAt(pos).getBackground()).getColor();
            float[] hsv = new float[3];
            Color.colorToHSV(col, hsv);
            hsv[2] *= 0.8;
            lv.getChildAt(pos).setBackgroundColor(Color.HSVToColor(hsv));
            //lv.getChildAt(pos).setElevation(30); //Causes weird bug when editing last task
        } else {
            int col = ((ColorDrawable) lv.getChildAt(pos).getBackground()).getColor();
            float[] hsv = new float[3];
            Color.colorToHSV(col, hsv);
            hsv[2] *= 1.25;
            lv.getChildAt(pos).setBackgroundColor(Color.HSVToColor(hsv));
            //lv.getChildAt(pos).setElevation(0);
        }
    }

    private void color() {
        if (options.colors) {
            for (int i = 0; i < aa.getCount(); i++) {
                View v = lv.getChildAt(i);
                if(v==null)
                    continue;
                int colId=Task.prioColor(aa.getItem(i).getPriority());
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

    private void moveUp() {
        if (focused > 0) {
            setFocus(focused, false);
            tasks.moveUp(focused);
            focused--;
            update();
            //setFocus(focused, true);
        } else if (focused == -1)
            Util.message(getResources().getString(R.string.lv_please_select), this);
        else
            Util.message(getResources().getString(R.string.lv_cant_up), this);
    }

    private void moveDown() {
        if (focused != -1 && focused != tasks.size() - 1) {
            tasks.moveDown(focused);
            setFocus(focused, false);
            focused++;
            update();
            //setFocus(focused, true);
        } else if (focused == -1)
            Util.message(getResources().getString(R.string.lv_please_select), this);
        else
            Util.message(getResources().getString(R.string.lv_cant_down), this);
    }

    void complete() {
        if (focused != -1) {
            tasks.complete(focused);
            setFocus(focused, false);
            focused = -1;
            update();
            Snackbar s = Snackbar.make(findViewById(R.id.listView), "Task completed", Snackbar.LENGTH_LONG);
            s.setAction("Undo", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tasks.undo();
                    update();
                }
            });
            s.show();
        } else
            Util.message(getResources().getString(R.string.lv_please_select), this);
    }

    private void edit() {
        if(edit)
            return;
        edit = true;
        if (focused != -1) {
            Task tsk = tasks.get(focused);
            addDialog(tsk, false);
        } else
            Util.message(getResources().getString(R.string.lv_please_select), this);

    }

    @Override
    void edited(boolean add, Task t) {
        update();
    }

    private void back() {
        Intent i = new Intent(ListAllActivity.this, MainActivity.class);
        i.putExtra("sender", "listAll");
        i.putExtra("list", tasks);
        i.putExtra("options", options);

        ListAllActivity.this.startActivity(i);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void update() {
        aa.setData(tasks.getAll());
        aa.notifyDataSetChanged();
        lv.invalidateViews();
        color();
        TextView tvList = (TextView) findViewById(R.id.ListText);
        tvList.setText(options.list);
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void tutorial() {
        Util.message("Tutorial", this);
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



    public void load() {
        options = Util.loadOptions(this);
        tasks = Util.loadTasks(options.list, this);
    }




}

