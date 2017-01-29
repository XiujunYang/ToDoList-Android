package com.example.jean.todolist;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by Jean on 2017/1/18.
 */

public class MyDatabase  {
    private final String LOGTAG = "MyDatabase";
    private final String selection = DatabaseHelper.COLUMN_DATE + " = ? AND " +
            DatabaseHelper.COLUMN_TASK +" = ? AND " +
            DatabaseHelper.COLUMN_STATUS+" = ?";

    private static MyDatabase instance;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private List<ToDoTask> mTaksList = new ArrayList<ToDoTask>();
    private Context mContext;

    static public MyDatabase getInstance(Context context){
        if(instance == null) instance = new MyDatabase(context);
        return instance;
    }

    private MyDatabase(Context context){
        mContext = context;
        dbHelper = new DatabaseHelper(mContext);
        db = dbHelper.getWritableDatabase();
        if(!isTableExisted()) {
            Log.i(LOGTAG,"["+Thread.currentThread()+"]go to create Table.");
            dbHelper.onCreate(db);
        } else
            queryDataFromDB();
    }

    private boolean isTableExisted(){
        if(db == null || !db.isOpen()){
            db = dbHelper.getWritableDatabase();
        } else if(db.isReadOnly()){
            db.close();
            db = dbHelper.getWritableDatabase();
        }

        Cursor cursor = db.rawQuery(DatabaseHelper.SQL_TABLE_IF_EXISTED, null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    synchronized public void queryDataFromDB(){
        List<ToDoTask> tasklist = new ArrayList<ToDoTask>();
        String[] projection = {
                DatabaseHelper.COLUMN_DATE,
                DatabaseHelper.COLUMN_TASK,
                DatabaseHelper.COLUMN_STATUS,
                DatabaseHelper.COLUMN_PRIORITY
        };
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_NAME,  // The table to query
                projection,                    // The columns to return
                null,                          // The columns for the WHERE clause
                null,                // The values for the WHERE clause
                null,                          // don't group the rows
                null,                          // don't filter by row groups
                null                           // no sort order
        );
        if(cursor != null && cursor.getCount()>0){
            cursor.moveToFirst();
            do{
                String date = cursor.getString(cursor.getColumnIndexOrThrow(projection[0]));
                String task = cursor.getString(cursor.getColumnIndexOrThrow(projection[1]));
                boolean isFinish = cursor.getInt(cursor.getColumnIndexOrThrow(projection[2]))==1?true:false;
                int priority = cursor.getInt(cursor.getColumnIndexOrThrow(projection[3]));
                ToDoTask taskRow = new ToDoTask(date,task,isFinish,priority);
                tasklist.add(taskRow);
            }while(cursor.moveToNext());
            mTaksList = tasklist;
            Log.i(LOGTAG, "["+Thread.currentThread()+"]queryDataFromDB:"+mTaksList.size());
        }

        mContext.sendBroadcast(new Intent(AppContent.QUERY_NOTIFICATION_INTENT));
    }

    synchronized public List<ToDoTask> getDataFromDB(){
        return mTaksList;
    }

    synchronized public boolean insertDataToDB(ToDoTask newTask){
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_DATE, newTask.getDate());
        values.put(DatabaseHelper.COLUMN_TASK, newTask.getTask());
        values.put(DatabaseHelper.COLUMN_STATUS, newTask.isCompleted());
        values.put(DatabaseHelper.COLUMN_PRIORITY, newTask.getPriority());
        long count = db.insert(
                DatabaseHelper.TABLE_NAME,
                null,
                values);
        Log.i(LOGTAG,"["+Thread.currentThread()+"] insertDataToDB for "+newTask.toString()+" , result:"+count);
        if(count == -1) return false;
        mTaksList.add(newTask);
        return true;
    }

    synchronized public boolean deleteDataFromDB(int index){
        ToDoTask delTask = mTaksList.get(index);
        Log.i(LOGTAG, "delete:"+delTask.toString());
        String[] selectionArgs = {delTask.getDate(),delTask.getTask(),
                delTask.isCompleted()==true?"1":"0"};
        int result = db.delete(DatabaseHelper.TABLE_NAME, selection, selectionArgs);
        Log.i(LOGTAG,"["+Thread.currentThread()+"] deleteDataFromDB for index("+index+"), result:"+result);
        if(result == 0) return false;
        mTaksList.remove(delTask);
        return true;
    }

    synchronized public boolean updateDataToDB(ToDoTask modifiedTask, int index){
        if(index >= mTaksList.size()) return false;
        ToDoTask targetTask = mTaksList.get(index);
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_DATE, modifiedTask.getDate());
        values.put(DatabaseHelper.COLUMN_TASK, modifiedTask.getTask());
        values.put(DatabaseHelper.COLUMN_STATUS, modifiedTask.isCompleted()==true? 1:0);
        values.put(DatabaseHelper.COLUMN_PRIORITY, modifiedTask.getPriority());

        // Which row to update, based on the following data.
        String[] selectionArgs = {targetTask.getDate(),targetTask.getTask(),
                targetTask.isCompleted()==true?"1":"0"};
        int count = db.update(
                DatabaseHelper.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        Log.i(LOGTAG,"["+Thread.currentThread()+"] updateDataToDB "+targetTask.toString()+" to "+
                modifiedTask.toString()+" in index("+index+"), result:"+count);
        if(count <= 0) return false;
        mTaksList.set(index, modifiedTask);
        return true;
    }

    synchronized boolean clearAllPriority(){
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PRIORITY, 0);

        int count = db.update(
                DatabaseHelper.TABLE_NAME,
                values,
                null,
                null);
        Log.i(LOGTAG,"["+Thread.currentThread()+"] clearAllPriority Total update count: "+count);
        Iterator it = mTaksList.iterator();
        if(count <= 0) return false;
        while(it.hasNext()) ((ToDoTask)it.next()).setPriority(0);
        return true;
    }

    synchronized void updateWholePriorityToDB(List<ToDoTask> taskList){
        Iterator it = taskList.iterator();
        ContentValues values;
        ToDoTask targetTask;
        int updatedCount = 0;
        while(it.hasNext()){
            targetTask = (ToDoTask) it.next();
            if(mTaksList.get(mTaksList.indexOf(targetTask)).getPriority() == targetTask.getPriority())
                continue;
            values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_PRIORITY, targetTask.getPriority());
            if(targetTask==null) continue;
            String[] selectionArgs = {targetTask.getDate(),targetTask.getTask(),
                    targetTask.isCompleted()==true?"1":"0"};
            int count = db.update(
                    DatabaseHelper.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);
            if(count!= 1) Log.d(LOGTAG,"["+Thread.currentThread()+"] updateWholePriorityToDB fail for: "+
                    targetTask.toString());
            else updatedCount+=count;
        }
        Log.i(LOGTAG, "["+Thread.currentThread()+"] updateWholePriorityToDB update "+updatedCount+" rows successfully.");
    }
}
