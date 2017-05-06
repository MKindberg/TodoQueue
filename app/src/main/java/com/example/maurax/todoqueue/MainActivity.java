package com.example.maurax.todoqueue;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends ListsActivity {

    private String name;
    private String desc;
    private int prio;

    private Animation in;
    private Animation out;

    private RelativeLayout relLay;

    private String filePath;

    private PopupMenu popup;

    private final static boolean FRONT = true;
    private final static boolean BACK = false;

    private float x1 = 0, x2 = 0, y1 = 0, y2 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tasks = new Tasks();

        filePath = this.getFilesDir().getPath();

        options = new Options();

        createMenu();

        readIntent();



        update(FRONT);
        update(BACK);

        relLay = (RelativeLayout) findViewById(R.id.mainView);

        relLay.setOnTouchListener(new View.OnTouchListener() {

            private final GestureDetector gestureDetector = new GestureDetector(relLay.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Intent i = new Intent(MainActivity.this, ListAllActivity.class);
                    i.putExtra("list", tasks);
                    i.putExtra("options", options);
                    MainActivity.this.startActivity(i);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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

    private void createMenu() {
        popup = new PopupMenu(this, findViewById(R.id.buttonMenu));
        popup.getMenuInflater().inflate(R.menu.menu_list, popup.getMenu());
        popup.getMenu().findItem(R.id.colorsOp).setChecked(options.colors);
        popup.getMenu().findItem(R.id.notifyOp).setChecked(options.notification);
    }

    private void readIntent() {
        Intent in = getIntent();
        if (!"listAll".equals(in.getStringExtra("sender"))) {
            load();
            checkTutorial();

        }
    else {
            tasks = in.getParcelableExtra("list");
            options = in.getParcelableExtra("options");
            popup.getMenu().findItem(R.id.colorsOp).setChecked(options.colors);
            popup.getMenu().findItem(R.id.notifyOp).setChecked(options.notification);
        }
    }
    private void checkTutorial() {
        File f = new File(filePath + "tutorial");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            tutorial();
        }
    }

    private void tutorial() {
        final File f = new File(filePath + "tutorial");
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setMessage("Swipe towards the edges or press them to handle the list of tasks.\n" +
                "Double tap to enter another interface where you can edit the items and list order\n" +
                "The notification will only show when you're out of the app and the list isn'tasks empty");
        /*b.setMessage(getResources().getString(R.string.instructions) + ":\n" +
                "1. " + getResources().getString(R.string.tutorial_down) +
                "\n2. " + getResources().getString(R.string.tutorial_right) +
                "\n3. " + getResources().getString(R.string.tutorial_up) +
                "\n4. " + getResources().getString(R.string.tutorial_left) +
                "\n5. " + getResources().getString(R.string.tutorial_double) +
                "\n6. " + getResources().getString(R.string.tutorial_long));*/
        b.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        b.setCancelable(true);
        b.create().show();
    }

    private void add() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(getResources().getString(R.string.add_new_lbl));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);


        final EditText inTask = new EditText(this);
        inTask.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
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
        layout.addView(inTask);

        final EditText inDesc = new EditText(this);
        inDesc.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        inDesc.setSingleLine(false);
        inDesc.setHint(getResources().getString(R.string.desc));
        layout.addView(inDesc);

        LinearLayout addLin = new LinearLayout(this);
        TextView prioTitle = new TextView(this);
        prioTitle.setText(R.string.title_add_priority);
        layout.addView(prioTitle);
        prioTitle.setPadding(20, 20, 20, 20);

        final SeekBar prioBar = new SeekBar(this);
        prioBar.setMax(4);
        prioBar.setProgress(2);

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
                name = inTask.getText().toString();
                desc = inDesc.getText().toString();
                prio = prioBar.getProgress() + 1;
                tasks.add(name, desc, prio);
                animate("add");
            }
        });
        b.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        b.create().show();
    }

    public void add(View v) {
        add();
    }

    void complete() {
        if (tasks.complete()) {
            animate("complete");
            Snackbar s = Snackbar.make(findViewById(R.id.mainView), "Task completed", Snackbar.LENGTH_SHORT);
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

    public void complete(View v) {
        complete();
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

    public void putLast(View v) {
        putLast();
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

    public void postpone(View v) {
        postpone();
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
            update(getString(R.string.empty), getString(R.string.please_add), 0, card);
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
        if(prio!=0)
            tvPrio.setText(getString(R.string.priority) + Integer.toString(prio));
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
            switch (prio) {
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
        } else {
            colId = R.color.noPrio;
        }
        if(prio==0)
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

    public void load() {
        options = Util.loadOptions(this);
        tasks = Util.loadTasks(options.list, this);

        popup.getMenu().findItem(R.id.colorsOp).setChecked(options.colors);
        popup.getMenu().findItem(R.id.notifyOp).setChecked(options.notification);

        update(FRONT);
    }

    public void save() {
        Util.saveTasks(tasks, options.list, this);
        Util.saveOptions(options, this);
        Util.updateWidget(this);
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
                        tasks.sort();
                        update(FRONT);
                        update(BACK);
                        return true;
                    case R.id.colorsOp:
                        item.setChecked(!item.isChecked());
                        options.colors = item.isChecked();
                        update(FRONT);
                        update(BACK);
                        popup.show();
                        return true;
                    case R.id.notifyOp:
                        item.setChecked(!item.isChecked());
                        options.notification = item.isChecked();
                        popup.show();
                        return true;
                    case R.id.listOp:
                        listDialog(MainActivity.this);
                        return true;

                    default:
                        return false;
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu(relLay);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        save();
        if(options.notification && tasks.size()!=0)
            NotificationReciever.showNotification(tasks.getFirst().getName(), tasks.getFirst().getDescription(), tasks.getFirst().getColorId(), this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
        NotificationReciever.cancelNotification(this);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
