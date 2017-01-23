package com.example.jean.todolist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Created by Jean on 2017/1/18.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "Todolist.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = "INTEGER";
    private static final String COMMA_SEP = ",";
    public static final String TABLE_NAME = "taskList";
    public static final String COLUMN_DATE = "datetime";
    public static final String COLUMN_TASK = "task";
    public static final String COLUMN_STATUS = "isFinish";

    private static final String SQL_TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_DATE +" "+ TEXT_TYPE + " NOT NULL"+ COMMA_SEP +
                    COLUMN_TASK +" "+ TEXT_TYPE + " NOT NULL"+ COMMA_SEP +
                    COLUMN_STATUS + " " + INTEGER_TYPE +" NOT NULL"+" )";

    private static final String SQL_TABLE_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static final String SQL_TABLE_IF_EXISTED=
            "SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = '"+TABLE_NAME+"'";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_TABLE_CREATE);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_TABLE_DELETE);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
