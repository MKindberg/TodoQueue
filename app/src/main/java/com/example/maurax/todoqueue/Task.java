package com.example.maurax.todoqueue;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class Task implements Parcelable, Comparable<Task> {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };
    private String name;
    private String description;
    private int priority;

    public Task(String name) {
        this.name = name;
    }

    /**
     * Create new task with name and description
     *
     * @param name
     * @param description
     */
    public Task(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Create new task with name, description and priority
     * @param name
     * @param description
     * @param priority
     */
    public Task(String name, String description, int priority) {
        this.name = name;
        this.description = description;
        this.priority = priority;
    }

    private Task(Parcel in) {
        String[] data = new String[3];
        in.readStringArray(data);
        this.name = data[0];
        this.description = data[1];
        this.priority = Integer.parseInt(data[2]);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
    public static String prioText(int prio){
        switch (prio){
            case 4:
                return "Urgent";
            case 3:
                return "High";
            case 2:
                return "Medium";
            case 1:
                return "Low";
            default:
                return "None";
        }
    }

    /**
     *
     * @return The color of the tasks priority
     */
    public int getColorId() {
        return prioColor(priority);
    }

    public static int prioColor(int prio) {
        int colId=0;
        switch(prio){
            case 4:
                colId = R.color.prioUrgent;
                break;
            case 3:
                colId = R.color.prioHigh;
                break;
            case 2:
                colId = R.color.prioMedium;
                break;
            case 1:
                colId = R.color.prioLow;
                break;
            case 0:
                colId = R.color.prioNone;
                break;
            default:
                colId = R.color.noPrio;
        }
        return colId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        if (description.length() == 0)
            return name;
        String res = description.replace("\n", "  //  ");
        return name + " - " + res;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{name, description, Integer.toString(priority)});
    }

    @Override
    public int compareTo(@NonNull Task another) {
        return -Integer.compare(priority, another.getPriority());
    }
}
