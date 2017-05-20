package com.example.maurax.todoqueue;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends BasicListActivity {

    private String name;
    private String desc;
    private int prio;

    private Animation in;
    private Animation out;

    private final static boolean FRONT = true;
    private final static boolean BACK = false;

    private float x1 = 0, x2 = 0, y1 = 0, y2 = 0;

    void findViews(){

    }

    void setUp(){
        ((Button) findViewById(R.id.buttonBot)).setText(R.string.btn_add_new);
        ((Button) findViewById(R.id.buttonTop)).setText(R.string.btn_put_last);
        ((Button) findViewById(R.id.buttonLeft)).setText(R.string.btn_postpone);
        ((Button) findViewById(R.id.buttonRight)).setText(R.string.btn_complete);
    }

    void setListeners(){
        super.setListeners();
        ((Button) findViewById(R.id.buttonBot)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add();
            }
        });
        ((Button) findViewById(R.id.buttonTop)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                putLast();
            }
        });
        ((Button) findViewById(R.id.buttonLeft)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postpone();
            }
        });
        ((Button) findViewById(R.id.buttonRight)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                complete();
            }
        });

        ((RelativeLayout) findViewById(R.id.mainView)).setOnTouchListener(new View.OnTouchListener() {

            private final GestureDetector gestureDetector = new GestureDetector(MainActivity.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Intent i = new Intent(MainActivity.this, ListAllActivity.class);
                    i.putExtra("tasks", tasks);
                    i.putExtra("options", options);
                    MainActivity.this.startActivity(i);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return super.onDoubleTap(e);
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    edit();
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                gestureDetector.onTouchEvent(event);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        y1 = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();
                        y2 = event.getY();
                        if (x1 == x2 && y1 == y2) //click
                            return false;

                        float diffX = x2 - x1;
                        float diffY = y2 - y1;

                        if (Math.abs(diffX) < 100 && Math.abs(diffY) < 100)
                            return false;

                        if (Math.abs(diffX) > Math.abs(diffY)) {
                            if (x1 < x2)//swipe right
                                complete();
                            else//swipe left
                                postpone();
                        } else {
                            if (y1 < y2)//swipe down
                                add();
                            else//swipe up
                                putLast();
                        }
                }
                return true;
            }
        });
    }

    void readIntent() {
        Intent in = getIntent();
        if ("listAll".equals(in.getStringExtra("sender"))) {
            tasks = in.getParcelableExtra("list");
            options = in.getParcelableExtra("options");



        }
    else {
            load();
            //checkTutorial();
        }
    }
//    private void checkTutorial() {
//        File f = new File(filePath + "tutorial");
//        if (!f.exists()) {
//            try {
//                f.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            tutorial();
//        }
//    }

    private void tutorial() {
//        final File f = new File(filePath + "tutorial");
//        AlertDialog.Builder b = new AlertDialog.Builder(this);
//        b.setMessage("Swipe towards the edges or press them to handle the list of tasks.\n" +
//                "Double tap to enter another interface where you can edit the items and list order\n" +
//                "The notification will only show when you're out of the app and the list isn'tasks empty");
//        /*b.setMessage(getResources().getString(R.string.instructions) + ":\n" +
//                "1. " + getResources().getString(R.string.tutorial_down) +
//                "\n2. " + getResources().getString(R.string.tutorial_right) +
//                "\n3. " + getResources().getString(R.string.tutorial_up) +
//                "\n4. " + getResources().getString(R.string.tutorial_left) +
//                "\n5. " + getResources().getString(R.string.tutorial_double) +
//                "\n6. " + getResources().getString(R.string.tutorial_long));*/
//        b.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });
//
//        b.setCancelable(true);
//        b.create().show();
        Util.message("Tutorial", this);
    }

    private void edit(){
        Task tsk = tasks.getFirst();
        addDialog(tsk, false);
    }

    private void add() {
        addDialog(new Task("", "", 2), true);
    }

    @Override
    void edited(boolean add, Task t) {
        if (add){
            name = t.getName();
            desc = t.getDescription();
            prio = t.getPriority();
            tasks.add(name, desc, prio);
            animate("add");

        }
        else
            update();
    }

    void complete() {
        if (tasks.complete()) {
            animate("complete");
            Snackbar s = Snackbar.make(findViewById(R.id.mainView), "Task completed", Snackbar.LENGTH_LONG);
            s.setAction("Undo", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tasks.undo();
                    update();
                }
            });
            s.show();
        }
        else
            Util.message(getResources().getString(R.string.list_empty_toast), this);
    }

    private void putLast() {
        Task tsk = tasks.getFirst();
        if (tasks.toLast()) {
            name = tsk.getName();
            desc = tsk.getDescription();
            prio = tsk.getPriority();
            animate("putLast");
        } else
            Util.message(getResources().getString(R.string.list_empty_toast), this);
    }

    private void postpone() {
        Task tsk = tasks.getFirst();
        if (tasks.postpone()) {
            name = tsk.getName();
            desc = tsk.getDescription();
            prio = tsk.getPriority();
            animate("postpone");
        } else
            Util.message(getString(R.string.list_empty_toast), this);
    }

    public void update(){
        update(FRONT);
        update(BACK);
    }

    private void update(boolean card) {
        Task task = tasks.getFirst();
        if (task != null) {
            update(task.getName(), task.getDescription(), task.getPriority(), card);
        } else {
            update(getString(R.string.empty), getString(R.string.please_add),-1, card);
        }
    }

    private void update(String name, String desc, int prio, boolean card) {
        TextView tvTask;
        TextView tvDesc;
        TextView tvPrio;
        TextView tvList = (TextView) findViewById(R.id.ListText);
        View back;
        if(card==FRONT) {
            tvTask = (TextView) findViewById(R.id.TaskView);
            tvDesc = (TextView) findViewById(R.id.DescView);
            tvPrio = (TextView) findViewById(R.id.PrioText);
            back = findViewById(R.id.textGroup);
        }else{
            tvTask = (TextView) findViewById(R.id.TaskViewBack);
            tvDesc = (TextView) findViewById(R.id.DescViewBack);
            tvPrio = (TextView) findViewById(R.id.PrioTextBack);
            back = findViewById(R.id.textGroupBack);
        }

        assert tvTask != null;
        assert tvDesc != null;
        assert tvPrio != null;
        assert tvList != null;
        tvTask.setText(name);
        tvDesc.setText(desc);
        if(prio!=-1)
            tvPrio.setText(getString(R.string.priority) +" "+ Task.prioText(prio));
        else
            tvPrio.setText("");
        tvList.setText(options.list);
        color(back, prio);
    }

    private void color(View tg, int prio) {
        GradientDrawable gd = new GradientDrawable();
        gd.setStroke(3, Color.BLACK);
        gd.setCornerRadius(5);
        int colId;
        if (options.colors) {
                colId=Task.prioColor(prio);
        } else {
            colId = R.color.noPrio;
        }
        if(prio==-1)
            colId=R.color.light_grey;
        gd.setColor(ContextCompat.getColor(this, colId));
        tg.setBackground(gd);
    }

    private void animate(String outDir) {

        final View tf = findViewById(R.id.textGroup);
        final View tfb = findViewById(R.id.textGroupBack);
        assert tf != null;
        assert tfb != null;
        switch (outDir) {
            case "postpone":
                out = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
                in = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
                break;
            case "complete":
                out = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
                in = null;
                break;
            case "putLast":
                out = AnimationUtils.loadAnimation(this, R.anim.slide_out_up);
                in = AnimationUtils.loadAnimation(this, R.anim.add_from_top);
                break;
            case "add":
                out = AnimationUtils.loadAnimation(this, R.anim.add_from_top);
                in = null;
                break;
        }
        out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                tf.setElevation(50);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                update(FRONT);
                tf.setElevation(0);
                if (in != null) {
                    update(name, desc, prio, BACK);
                    tf.clearAnimation();
                    tfb.startAnimation(in);
                } else {
                    update(BACK);
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        if (in != null)
            in.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    tf.setElevation(50);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    tf.setElevation(0);
                    update(BACK);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        if (tasks.size() == 1)
            if (outDir.equals("complete")) {
                update(BACK);
                tf.startAnimation(out);
            } else if (outDir.equals("add")) {
                update(FRONT);
                tf.startAnimation(out);
            } else
                Util.message("Last item", this);
        else if (outDir.equals("add")) {
            update(name, desc, prio, BACK);
            tfb.startAnimation(out);
        } else {
            update(BACK);
            tf.startAnimation(out);
        }

    }


    public void menu(View v) {
        popup.show();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_item_tutorial:
                        tutorial();
                        return true;
                    case R.id.menu_item_sort:
                        tasks.sort();
                        update(FRONT);
                        update(BACK);
                        return true;
                    case R.id.menu_item_colors:
                        item.setChecked(!item.isChecked());
                        options.colors = item.isChecked();
                        update(FRONT);
                        update(BACK);
                        popup.show();
                        return true;
                    case R.id.menu_item_lists:
                        listDialog(MainActivity.this);
                        return true;
                    case R.id.menu_item_settings:
                        Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(i);
                        return true;
                    case R.id.menu_item_share:
                        shareData();

                    default:
                        return false;
                }
            }
        });

    }

    @Override
    protected void sorted() {

    }

    void shareData() {
        Task task = tasks.getFirst();
        Intent i = new Intent(android.content.Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(android.content.Intent.EXTRA_SUBJECT, task.getName());
        i.putExtra(android.content.Intent.EXTRA_TEXT, task.getDescription());
        startActivity(Intent.createChooser(i, "Share via"));

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
