package com.mkindberg.todoqueue;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by marcus on 2016-07-02.
 */
public class Tasks implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Tasks createFromParcel(Parcel in) {
            return new Tasks(in);
        }

        @Override
        public Tasks[] newArray(int size) {
            return new Tasks[size];
        }
    };
    private final LinkedList<Task> tasks;

    private Task delTask = null;
    private int delIndex = -1;

    public Tasks() {
        tasks = new LinkedList<Task>();
    }

    public Tasks(LinkedList<Task> t) {
        this.tasks = t;
    }

    private Tasks(Parcel in) {
        tasks = new LinkedList<>();
        in.readList(tasks, getClass().getClassLoader());
    }

    public void add(String name, String desc) {
        tasks.add(new Task(name, desc));
    }

    public void add(String name, String desc, int prio) {
        tasks.add(new Task(name, desc, prio));
    }

    public void edit(Task t, int pos){
        tasks.remove(pos);
        tasks.add(pos, t);
    }
    public void sort() {
        Collections.sort(tasks);
    }

    /**
     * Removes first task
     *
     * @return true if list not empty
     */
    public boolean complete() {
        if (tasks.isEmpty())
            return false;
        delTask = tasks.getFirst();
        delIndex = 0;
        tasks.remove();
        return true;
    }

    public boolean complete(int index) {
        if (tasks.isEmpty())
            return false;
        delIndex = index;
        delTask = tasks.remove(index);
        return true;
    }

    public void undo(){
        if(delIndex!=-1){
            tasks.add(delIndex, delTask);
        }
    }

    /**
     * Swaps the first and second task
     * @return true if list not empty
     */
    public boolean postpone() {
        if (tasks.isEmpty())
            return false;
        if (tasks.size() == 1)
            return true;
        tasks.add(1, tasks.remove());
        return true;
    }

    /**
     * Places first task last
     * @return true if list not empty
     */
    public boolean toLast() {
        if (tasks.isEmpty())
            return false;
        tasks.addLast(tasks.remove());
        return true;
    }

    /**
     *
     * @return First task or null if empty
     */
    public Task getFirst() {
        if (tasks.isEmpty())
            return null;
        return tasks.getFirst();
    }

    public void clear(){
        tasks.clear();
    }

    public void moveUp(int pos){
        tasks.add(pos - 1, tasks.remove(pos));
    }

    public void moveDown(int pos){
        tasks.add(pos + 1, tasks.remove(pos));
    }

    public int size() {
        return tasks.size();
    }

    public LinkedList<Task> getAll() {
        return tasks;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(tasks);
    }

    public Task get(int index) {
        return tasks.get(index);
    }
}