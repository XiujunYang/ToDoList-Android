package com.example.jean.todolist;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


/**
 * Created by Jean on 2017/1/17.
 */

public class ToDoTask implements Parcelable {
    private final String LOG_TAG = "ToDoTask";

    private String mDate;
    private String mTaskContent;
    private boolean mCompleted;
    private int mPriority = -1;//0 is normal, 1 is important. -1 is unassigned, could be considered as normal one.

    public ToDoTask(String date, String task, boolean completed, int priority){
        this.mDate = date;
        this.mTaskContent = task;
        this.mCompleted = completed;
        if (priority!=-1) this.mPriority=priority;
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
    public int getPriority(){
        return mPriority;
    }
    public void setPriority(int value){
        this.mPriority = value;
    }

    @Override
    public boolean equals(Object obj){
        // Ignore mPriority
        if (obj instanceof ToDoTask){
            if(((ToDoTask) obj).getDate().equals(this.mDate) &&
                    ((ToDoTask) obj).getTask().equals(this.mTaskContent) &&
                    ((ToDoTask) obj).isCompleted() == this.mCompleted) return true;
        }
        return false;
    }

    @Override
    public ToDoTask clone(){
        ToDoTask task = new ToDoTask(getDate(), getTask(),isCompleted(),getPriority());
        return task;
    }

    @Override
    public String toString(){
        return new String("[Date: "+mDate+"; Task: "+mTaskContent+"; isFinish: "+mCompleted+"; Priority:"+mPriority+"]");
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
        dest.writeInt(mPriority);
    }

    public static final Parcelable.Creator<ToDoTask> CREATOR = new Parcelable.Creator<ToDoTask>() {
        @Override
        public ToDoTask createFromParcel(Parcel source) {
            return new ToDoTask(source.readString(), source.readString(), source.readInt()==1?true:false,
                    source.readInt());
        }

        @Override
        public ToDoTask[] newArray(int size) {
            return new ToDoTask[size];
        }
    };
}
