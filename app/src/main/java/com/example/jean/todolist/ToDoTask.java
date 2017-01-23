package com.example.jean.todolist;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by Jean on 2017/1/17.
 */

public class ToDoTask implements Parcelable {

    private String mDate;
    private String mTaskContent;
    private boolean mCompleted;

    public ToDoTask(String date, String task, boolean completed){
        this.mDate = date;
        this.mTaskContent = task;
        this.mCompleted = completed;
    }

    public String getDate(){
        return mDate;
    }
    public void setmDate(String date){
        this.mDate = date;
    }
    public String getTask(){
        return mTaskContent;
    }
    public void setTask(String task){ this.mTaskContent = task;}
    public boolean isCompleted(){
        return mCompleted;
    }
    public void setCompleted(boolean value){
        this.mCompleted = value;
    }

    @Override
    public boolean equals(Object obj){
        if (obj instanceof ToDoTask){
            if(((ToDoTask) obj).getDate().equals(this.mDate) &&
                    ((ToDoTask) obj).getTask().equals(this.mTaskContent) &&
                    ((ToDoTask) obj).isCompleted() == this.mCompleted) return true;
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mDate);
        dest.writeString(mTaskContent);
        dest.writeInt(mCompleted?1:0);
    }

    public static final Parcelable.Creator<ToDoTask> CREATOR = new Parcelable.Creator<ToDoTask>() {
        @Override
        public ToDoTask createFromParcel(Parcel source) {
            return new ToDoTask(source.readString(), source.readString(), source.readInt()==1?true:false);
        }

        @Override
        public ToDoTask[] newArray(int size) {
            return new ToDoTask[size];
        }
    };
}
