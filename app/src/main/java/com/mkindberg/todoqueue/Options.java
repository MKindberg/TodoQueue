package com.mkindberg.todoqueue;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Created by marcus on 27/04/2017.
 */

public class Options implements Parcelable {
    public boolean colors = false;
    public boolean notification = false;
    public String list = TaskDB.DEFALT_TABLE_NAME;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.colors ? (byte) 1 : (byte) 0);
        dest.writeByte(this.notification ? (byte) 1 : (byte) 0);
        dest.writeString(this.list);
    }

    public Options() {
    }

    protected Options(Parcel in) {
        this.colors = in.readByte() != 0;
        this.notification = in.readByte() != 0;
        this.list = in.readString();
    }

    public static final Parcelable.Creator<Options> CREATOR = new Parcelable.Creator<Options>() {
        @Override
        public Options createFromParcel(Parcel source) {
            return new Options(source);
        }

        @Override
        public Options[] newArray(int size) {
            return new Options[size];
        }
    };
}
