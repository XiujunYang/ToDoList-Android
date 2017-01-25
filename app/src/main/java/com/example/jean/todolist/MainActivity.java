package com.example.jean.todolist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    final String LOG_TAG = "MainActivity";

    private GoogleApiClient client;
    private Context mContext;
    private Resources rcs;
    private ListView listView;
    List<ToDoTask> task_list = new ArrayList<ToDoTask>();
    private MyBaseAdapter adapter;
    public static DatabaseHandler dbHandler;
    private DBNotifiedReceiver dbNotifiedReceiver = new DBNotifiedReceiver();
    private IntentFilter filter = new IntentFilter(AppContent.QUERY_NOTIFICATION_INTENT);
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHandler = new DatabaseHandler();
        dbHandler.start();
        mContext = this.getApplicationContext();
        rcs = mContext.getResources();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.task_listview);
        adapter = new MyBaseAdapter(mContext, task_list);
        dbHandler.getTaskListFromDB();
        listView.setAdapter(adapter);
        sharedPreferences = getSharedPreferences(AppContent.SharedPreferences_Name , MODE_PRIVATE);

        FloatingActionButton addTaskBtn = (FloatingActionButton) findViewById(R.id.action_addTask);
        addTaskBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(mContext, EditTaskActivity.class);
                intent.setAction(AppContent.action_function_create);
                startActivity(intent);
            }});

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onResume(){
        super.onResume();
        adapter.setHideCompletedTask(sharedPreferences.getBoolean(AppContent.sp_hidetask_flag , false));
        adapter.updateList(task_list);
        registerReceiver(dbNotifiedReceiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(dbNotifiedReceiver);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(LOG_TAG, "requestCode:"+requestCode);
        int pos = -1;
        Bundle b;
        switch (requestCode) {
            case AppContent.Request_Code_MainActivity:
                if(resultCode != RESULT_OK) break;
                if(data==null || data.getExtras() ==null) break;
                ToDoTask searchTask= data.getExtras().getParcelable(AppContent.search_task);
                if(searchTask != null) pos = adapter.taskList.indexOf(searchTask);
                Log.i(LOG_TAG,"search position result: "+pos);
                if(pos==-1) break;
                listView.smoothScrollToPosition(pos);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.hide_completed_task).setChecked(
                sharedPreferences.getBoolean(AppContent.sp_hidetask_flag , false));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.action_settings:
            return true;
            case R.id.hide_completed_task:
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if(item.isChecked()) {
                    item.setChecked(false);
                    adapter.setHideCompletedTask(false);
                    adapter.updateList(task_list);
                }else{
                    item.setChecked(true);
                    adapter.setHideCompletedTask(true);
                    adapter.updateList(task_list);
                }
                editor.putBoolean(AppContent.sp_hidetask_flag ,item.isChecked());
                editor.commit();
                return true;
            case R.id.search_task:
                intent = new Intent(mContext, SearchActivity.class);
                // show up current displayed list in SearchActivity, and it's depended on R.id.hide_completed_task.
                intent.putParcelableArrayListExtra(AppContent.displayed_task_list, (ArrayList<ToDoTask>) adapter.taskList);
                startActivityForResult(intent,AppContent.Request_Code_MainActivity);
                return true;
            case R.id.action_edit:
            case R.id.action_delete:
                intent = new Intent(this, SelectedTaskActivity.class);
                if(id == R.id.action_edit) intent.setAction(AppContent.action_function_edit);
                else if(id ==R.id.action_delete) intent.setAction(AppContent.action_function_delete);
                intent.putParcelableArrayListExtra(AppContent.displayed_task_list, (ArrayList<ToDoTask>) task_list);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    private void updateTaskList(ToDoTask needUpdatedTask, int index){
        if(needUpdatedTask == null){
            task_list.remove(index);
            dbHandler.dbConnection.deleteDataFromDB(index);
        }else if(index != -1 && index < task_list.size()){
            task_list.set(index, needUpdatedTask);
            dbHandler.dbConnection.updateDataToDB(needUpdatedTask,index);
        }else {
            // create a new task
            task_list.add(needUpdatedTask);
            dbHandler.dbConnection.insertDataToDB(needUpdatedTask);
        }
        adapter.updateList(task_list);
    }


    public static class DBNotifiedReceiver extends BroadcastReceiver {
        public DBNotifiedReceiver(){}

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == AppContent.QUERY_NOTIFICATION_INTENT){
                dbHandler.getTaskListFromDB();
            }
        }
    }

    public class DatabaseHandler extends Thread {
        private final String LOG_TAG = "DatabaseHandler";
        private MyDatabase dbConnection;

        @Override
        public void run(){
            dbConnection = MyDatabase.getInstance(mContext);
        }

        public void getTaskListFromDB(){
            List<ToDoTask> list = dbConnection.getDataFromDB();
            if(task_list.size()== 0 && list.size() != 0){
                task_list.addAll(list);
                adapter.updateList(task_list);
            }
        }

        public void updateTaskToList(ToDoTask needUpdatedTask, int index){
           updateTaskList(needUpdatedTask,index);
        }
    }
}
