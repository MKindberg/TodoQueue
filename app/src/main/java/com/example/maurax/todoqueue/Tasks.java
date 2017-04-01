package com.example.maurax.todoqueue;

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
    private LinkedList<Task> tasks;

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

    public void sort() {
        Collections.sort(tasks);

    }

    public boolean complete() {
        if (tasks.isEmpty())
            return false;
        tasks.remove();
        return true;
    }

    public boolean postpone() {
        if (tasks.isEmpty())
            return false;
        if (tasks.size() == 1)
            return true;
        tasks.add(1, tasks.remove());
        return true;
    }

    public boolean toLast() {
        if (tasks.isEmpty())
            return false;
        tasks.addLast(tasks.remove());
        return true;
    }

    public Task getFirst() {
        if (tasks.isEmpty())
            return null;
        return tasks.getFirst();
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
}