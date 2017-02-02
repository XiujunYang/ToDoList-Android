package com.example.jean.todolist;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Iterator;
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
    private static DatabaseHandler dbHandler;
    LooperThread myLooper = new LooperThread();
    private DBNotifiedReceiver dbNotifiedReceiver = new DBNotifiedReceiver();
    private IntentFilter filter = new IntentFilter(AppContent.QUERY_NOTIFICATION_INTENT);
    SharedPreferences sharedPreferences;
    EditText searchFor;
    static boolean searchFunctionOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG,"onCreate:"+Thread.currentThread());
        mContext = this.getApplicationContext();
        myLooper.start();
        rcs = mContext.getResources();
        sharedPreferences = getSharedPreferences(AppContent.SharedPreferences_Name , MODE_PRIVATE);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void initialView(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        toolbarTitle.setTypeface(toolbarTitle.getTypeface(), Typeface.BOLD);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.task_listview);
        adapter = new MyBaseAdapter(mContext, task_list);
        if(task_list.size()==0){
            Message msg = Message.obtain(dbHandler, RequestCode.GET_DATA);
            msg.sendToTarget();
        }
        listView.setAdapter(adapter);
        registerForContextMenu(listView);

        FloatingActionButton addTaskBtn = (FloatingActionButton) findViewById(R.id.action_addTask);
        addTaskBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(mContext, EditTaskActivity.class);
                intent.setAction(AppContent.action_function_create);
                startActivity(intent);
            }});
    }

    @Override
    public void onStart() {
        super.onStart();
        if(searchFunctionOn) setContentView(R.layout.activity_main_with_search);
        else setContentView(R.layout.activity_main);
        initialView();
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
        Log.i(LOG_TAG, "requestCode:"+requestCode+", resultCode:"+resultCode);
        if(resultCode != RESULT_OK) return;
        int pos = -1;
        Bundle b;
        switch (requestCode) {
            case AppContent.RequestCode_Prioritize_Task:
                if(data==null || data.getExtras() ==null) break;
                List<ToDoTask> list = data.getParcelableArrayListExtra(AppContent.displayed_task_list);
                if(list != null) {
                    Iterator it = list.iterator();
                    while(it.hasNext()){
                        ToDoTask changedTask = (ToDoTask) it.next();
                        task_list.get(task_list.indexOf(changedTask)).setPriority(changedTask.getPriority());
                    }
                    Message msg = Message.obtain(dbHandler,RequestCode.UPDATE_PRIORITY,task_list);
                    msg.sendToTarget();
                    adapter.notifyDataSetChanged();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.task_listview) {
            menu.add(Menu.NONE, AppContent.Context_MenuItem_EDIT, Menu.NONE, rcs.getString(R.string.edittask_label));
            menu.add(Menu.NONE, AppContent.Context_MenuItem_DEL, Menu.NONE, rcs.getString(R.string.deletetask_label));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case AppContent.Context_MenuItem_EDIT:
                Intent intent = new Intent(this, EditTaskActivity.class);
                intent.setAction(AppContent.action_function_edit);
                intent.putExtra(AppContent.edit_task_index, info.position);
                intent.putExtra(AppContent.edit_task, (ToDoTask) adapter.getItem(info.position));
                startActivity(intent);
                return true;
            case AppContent.Context_MenuItem_DEL:
                ToDoTask targetedTask = (ToDoTask) adapter.getItem(info.position);
                if(targetedTask !=null) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    alertDialog.setMessage("Are you sure to delete Task:\n\"" + targetedTask.getTask() + "\" ?");
                    alertDialog.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(dbHandler != null) {
                                dbHandler.updateTaskToList(null, info.position);
                            }
                        }
                    });
                    alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alertDialog.show();
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
                item.getSubMenu().findItem(R.id.hide_completed_task).setChecked(
                        sharedPreferences.getBoolean(AppContent.sp_hidetask_flag , false));
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
                if(!searchFunctionOn) {
                    setContentView(R.layout.activity_main_with_search);
                    initialView();
                    searchFunctionOn = true;
                    searchFor = (EditText) findViewById(R.id.inputSearch);
                    if(searchFor !=null) {
                        searchFor.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                            @Override
                            public void afterTextChanged(Editable arg0) {}
                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                adapter.updateList(task_list);
                                adapter.getFilter().filter(s);
                            }
                        });
                    }
                }else{
                    setContentView(R.layout.activity_main);
                    initialView();
                    searchFunctionOn = false;
                }
                adapter.updateList(task_list);
                return true;
            case R.id.action_edit:
            case R.id.action_delete:
                intent = new Intent(this, SelectedTaskActivity.class);
                if(id == R.id.action_edit) intent.setAction(AppContent.action_function_edit);
                else if(id ==R.id.action_delete) intent.setAction(AppContent.action_function_delete);
                if(searchFor!=null){
                    intent.putExtra(AppContent.current_search_string,searchFor.getText().toString());
                    searchFor.setText("");
                }
                intent.putParcelableArrayListExtra(AppContent.displayed_task_list, (ArrayList<ToDoTask>) task_list);
                startActivity(intent);
                return true;
            case R.id.unmark_all_task:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Clear all task mark?");
                alertDialog.setMessage("This operation will not be retrieved, are you sure?");
                alertDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Iterator it = task_list.iterator();
                        while(it.hasNext()) {
                            ((ToDoTask) it.next()).setPriority(0);
                        }
                        Message msg = Message.obtain(dbHandler, RequestCode.CLEAR_ALL_PRIORITY);
                        msg.sendToTarget();
                        adapter.notifyDataSetChanged();
                    }
                });
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {}});
                alertDialog.show();
                return true;
            case R.id.mark_important_task:
                intent = new Intent(mContext, SelectedTaskActivity.class);
                intent.setAction(AppContent.action_function_prioritize);
                intent.putParcelableArrayListExtra(AppContent.displayed_task_list, (ArrayList<ToDoTask>) task_list);
                startActivityForResult(intent,AppContent.RequestCode_Prioritize_Task);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    public static DatabaseHandler getDatabaseHander(){
        if(dbHandler != null) return dbHandler;
        return null;
    }

    private void updateTaskList(ToDoTask needUpdatedTask, int index){
        Message msg;
        if(needUpdatedTask == null){
            task_list.remove(index);
            msg = Message.obtain(dbHandler, RequestCode.DELETE_DATA, index,-1);
            msg.sendToTarget();
        }else if(index != -1 && index < task_list.size()){
            task_list.set(index, needUpdatedTask);
            msg = Message.obtain(dbHandler, RequestCode.UPDATE_DATA,index,-1,needUpdatedTask);
            msg.sendToTarget();
        }else {
            // create a new task
            if(task_list.contains(needUpdatedTask)){
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setIcon(R.drawable.warning_icon);
                alertDialog.setTitle("Task Repeated");
                alertDialog.setMessage("There's same task content, it is not allow input the same.");
                alertDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {}
                });
                alertDialog.show();
                return;
            }
            task_list.add(needUpdatedTask);
            msg = Message.obtain(dbHandler,RequestCode.INSERT_DATA, needUpdatedTask);
            msg.sendToTarget();
        }
        adapter.updateList(task_list);
    }

    // Deep copy each element of arrayLIst
    private List<ToDoTask> cloneList(List<ToDoTask> list) {
        List<ToDoTask> newlist = new ArrayList<ToDoTask>(list.size());
        for (ToDoTask item : list) newlist.add(item.clone());
        return newlist;
    }


    public static class DBNotifiedReceiver extends BroadcastReceiver {
        public DBNotifiedReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == AppContent.QUERY_NOTIFICATION_INTENT) {
                Message msg = Message.obtain(dbHandler,RequestCode.GET_DATA);
                msg.sendToTarget();
            }
        }
            }

    // Make Handler and looper run in the other thread to do database operation..
    class LooperThread extends Thread {
        public void run() {
            Looper.prepare();
            dbHandler = new DatabaseHandler();
            Looper.loop();
        }
    }

    public class DatabaseHandler extends Handler {
        private final String LOG_TAG = "DatabaseHandler";
        private MyDatabase dbConnection = MyDatabase.getInstance(mContext);


        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RequestCode.GET_DATA:
                    if(task_list.size() != 0) break;
                    List<ToDoTask> list = dbConnection.getDataFromDB();
                    if (list.size() != 0) {
                        task_list.addAll(cloneList(list));
                        adapter.updateList(task_list);
                    }
                    break;
                case RequestCode.INSERT_DATA:
                    dbConnection.insertDataToDB((ToDoTask) msg.obj);
                    break;
                case RequestCode.DELETE_DATA:
                    dbConnection.deleteDataFromDB(msg.arg1);
                    break;
                case RequestCode.UPDATE_DATA:
                    dbConnection.updateDataToDB((ToDoTask) msg.obj, msg.arg1);
                    break;
                case RequestCode.UPDATE_PRIORITY:
                    dbConnection.updateWholePriorityToDB((List<ToDoTask>) msg.obj);
                    break;
                case RequestCode.CLEAR_ALL_PRIORITY:
                    dbConnection.clearAllPriority();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }

        // Can not use Message to handleMessage from the other activity, because BaseAdapter.notifyDataSetChanged must call by same UI thread.
        public void updateTaskToList(ToDoTask needUpdatedTask, int index) {
            updateTaskList(needUpdatedTask, index);
            }
        }

}
