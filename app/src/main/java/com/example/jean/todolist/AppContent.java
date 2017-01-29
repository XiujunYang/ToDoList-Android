package com.example.jean.todolist;

/**
 * Created by Jean on 2017/1/21.
 */

public class AppContent {
    //Activity Request_Code from MainActivity
    public static final int RequestCode_Search_Task = 1000;
    public static final int RequestCode_Prioritize_Task = 1001;

    //Intent
    //Database is loaded done.
    public static final String QUERY_NOTIFICATION_INTENT = "com.example.todolist.db.query.done";
    public static final String action_function_create = "action_create_task";
    public static final String action_function_edit = "action_edit_task";
    public static final String action_function_delete = "action_delete_task";
    public static final String action_function_prioritize = "action_prioritize_task";
    public static final String edit_task = "edit_task";
    public static final String edit_task_index = "edit_task_index";
    public static final String displayed_task_list = "displayed_task_list";
    public static final String search_task = "search_task";

    //SharedPreferences
    public static final String SharedPreferences_Name = "TodoListConfig";
    public static final String sp_hidetask_flag = "TodoListConfig";
}
