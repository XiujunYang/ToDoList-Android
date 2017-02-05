package com.example.jean.todolist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * This Activity could be used as a selector of editing/deleting/marking task
 */
public class ModifyTaskActivity extends AppCompatActivity {
    private final String LOG_TAG = "ModifyTaskActivity";

    private Intent intent;
    private Resources rcs;
    private List<ToDoTask> task_list = new ArrayList<ToDoTask>();
    private MyBaseAdapter adapter;
    private AdapterView.OnItemClickListener listClickListener;
    private String action;
    private EditText searchFor;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rcs = this.getResources();
        if(MainActivity.searchFunctionOn) {
            setContentView(R.layout.modify_task_activity_with_search);
            searchFor = (EditText) findViewById(R.id.inputSearch);
        }
        else setContentView(R.layout.modify_task_activity);
        intent = getIntent();
        action = intent.getAction();
        task_list = intent.getParcelableArrayListExtra(AppContent.displayed_task_list);
        sharedPreferences = getSharedPreferences(AppContent.SharedPreferences_Name , MODE_PRIVATE);
    }

    @Override
    protected void onResume(){
        super.onResume();
        initView();
    }

    /*@Override
    protected void onPause(){
        super.onPause();
        intent = null;
        task_list.clear();
        action = null;
        searchFor = null;
        adapter = null;
    }*/

    private void initView(){
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        ImageView logo = (ImageView)findViewById(R.id.app_logo);
        logo.setImageResource(0);//don't show icon.
        setSupportActionBar(toolbar);
        // Enable back button on toolbar.
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if(null == action || null ==task_list) return;
        ListView listView = (ListView) findViewById(R.id.task_listview);
        listView.setHeaderDividersEnabled(false);
        listView.setFooterDividersEnabled(false);
        adapter = new MyBaseAdapter(ModifyTaskActivity.this, task_list);
        adapter.setHideCompletedTask(sharedPreferences.getBoolean(AppContent.sp_hidetask_flag , false));
        adapter.updateList(task_list);
        listView.setAdapter(adapter);
        setSearchEditTextListener();

        if(action.equals(AppContent.action_function_prioritize)) {
            toolbarTitle.setText(rcs.getString(R.string.prioritize_task_label));
            toolbar.setBackgroundColor(rcs.getColor(R.color.Prioritize_toolbar_color));
            listClickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ToDoTask item = (ToDoTask) adapter.getItem(position);
                    if(item.getPriority()>0) {
                        task_list.get(task_list.indexOf(item)).setPriority(0);
                        view.setBackgroundColor(rcs.getColor(R.color.default_background));
                    }else {
                        task_list.get(task_list.indexOf(item)).setPriority(1);
                        view.setBackgroundColor(rcs.getColor(R.color.importantTaskBgdColor));
                    }
                }
            };
        } else if(action.equals(AppContent.action_function_edit)) {
            toolbarTitle.setText(rcs.getString(R.string.selected_edittask_label));
            toolbar.setBackgroundColor(rcs.getColor(R.color.Edit_toolbar_color));
            listClickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ToDoTask item = (ToDoTask) adapter.getItem(position);
                    Intent intent = new Intent(ModifyTaskActivity.this, TaskEditorActivity.class);
                    intent.setAction(AppContent.action_function_edit);
                    intent.putExtra(AppContent.edit_task_index, task_list.indexOf(item));
                    intent.putExtra(AppContent.edit_task, item);
                    startActivityForResult(intent,AppContent.RequestCode_Edit_Task);
                }
            };
        } else if(action.equals(AppContent.action_function_delete)){
            toolbarTitle.setText(rcs.getString(R.string.deletetask_label));
            toolbar.setBackgroundColor(rcs.getColor(R.color.DEL_toolbar_color));
            listClickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final ToDoTask targetedTask = (ToDoTask) adapter.getItem(position);
                    // position is not including hided task' s count, but task_list of here and MainActivity is whole tasks.
                    final int pos = task_list.indexOf(targetedTask);
                    if(targetedTask !=null && pos!=-1) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ModifyTaskActivity.this);
                        alertDialog.setMessage("Are you sure to delete Task:\n\"" + targetedTask.getTask() + "\" ?");
                        alertDialog.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if(MainActivity.getDatabaseHander() != null) {
                                    MainActivity.getDatabaseHander().updateTaskToList(null, pos);
                                }
                                task_list.remove(pos);
                                adapter.updateList(task_list);
                            }
                        });
                        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {}});
                        alertDialog.show();
            }}};
        }
        listView.setOnItemClickListener(listClickListener);
        // init searchFor as same as MainActivity.
        if(searchFor!=null && intent.getStringExtra(AppContent.current_search_string)!=null)
            searchFor.setText(intent.getStringExtra(AppContent.current_search_string));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK) return;
        switch (requestCode) {
            case AppContent.RequestCode_Edit_Task:
                Bundle b=data.getExtras();
                int pos= b.getInt(AppContent.edit_task_index, -1);
                ToDoTask task = b.getParcelable(AppContent.edit_task);
                if(pos== -1 || task ==null) break;
                task_list.set(pos, task);
                adapter.updateList(task_list);
                break;
        }
    }

    @Override
    public void onBackPressed(){
        operationBeforeDead();
        super.onBackPressed();
        finish();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_selectedactivity, menu);
        menu.findItem(R.id.hide_completed_task).setChecked(
                sharedPreferences.getBoolean(AppContent.sp_hidetask_flag , false));
        return true;
    }

        @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.search_task:
                if(!MainActivity.searchFunctionOn){
                    setContentView(R.layout.modify_task_activity_with_search);
                    initView();
                    MainActivity.searchFunctionOn = true;
                    searchFor = (EditText) findViewById(R.id.inputSearch);
                    setSearchEditTextListener();
                }else{
                    searchFor = null;
                    setContentView(R.layout.modify_task_activity);
                    initView();
                    MainActivity.searchFunctionOn = false;
                }
                adapter.updateList(task_list);
                return true;
            case R.id.hide_completed_task:
                if(item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                adapter.setHideCompletedTask(item.isChecked());
                adapter.updateList(task_list);
                if(sharedPreferences!=null)
                    sharedPreferences.edit().putBoolean(AppContent.sp_hidetask_flag ,item.isChecked()).commit();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void operationBeforeDead(){
        if(action.equals(AppContent.action_function_prioritize)){
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(AppContent.displayed_task_list, (ArrayList<ToDoTask>) task_list);
            Intent intent = new Intent();
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
        }
    }

    private void setSearchEditTextListener(){
        if(searchFor !=null) {
            searchFor.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void afterTextChanged(Editable arg0) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.getFilter().filter(s);
                    adapter.updateList(task_list);
                }
    });}}
}
