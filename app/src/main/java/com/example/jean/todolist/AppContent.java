package com.example.jean.todolist;

/**
 * Created by Jean on 2017/1/21.
 */

public class AppContent {
    //Activity
    public static final int Request_Code_MainActivity = 1000;

    //Intent
    //Database is loaded done.
    public static final String QUERY_NOTIFICATION_INTENT = "com.example.todolist.db.query.done";
    public static final String action_function_create = "create_task";
    public static final String action_function_edit = "edit_task";
    public static final String action_function_delete = "delete_task";
    public static final String edit_task = "edit_task";
    public static final String edit_task_index = "edit_task_index";
    public static final String displayed_task_list = "displayed_task_list";
    public static final String search_task = "search_task";

    //SharedPreferences
    public static final String SharedPreferences_Name = "TodoListConfig";
    public static final String sp_hidetask_flag = "TodoListConfig";
}
