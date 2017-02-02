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
import android.util.Log;
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

public class SelectedTaskActivity extends AppCompatActivity {
    private final String LOG_TAG = "SelectedTaskActivity";

    Intent intent;
    private Resources rcs;
    private ListView listView;
    List<ToDoTask> task_list = new ArrayList<ToDoTask>();
    private MyBaseAdapter adapter;
    AdapterView.OnItemClickListener listClickListener;
    Toolbar toolbar;
    TextView toolbarTitle;
    String action;
    EditText searchFor;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rcs = this.getResources();
        if(MainActivity.searchFunctionOn) {
            setContentView(R.layout.selected_task_activity_with_search);
            searchFor = (EditText) findViewById(R.id.inputSearch);
        }
        else setContentView(R.layout.selected_task_activity);
        intent = getIntent();
        task_list = intent.getParcelableArrayListExtra(AppContent.displayed_task_list);
        action = intent.getAction();
        sharedPreferences = getSharedPreferences(AppContent.SharedPreferences_Name , MODE_PRIVATE);
    }

    protected void onResume(){
        super.onResume();
        initView();
    }

    private void initView(){
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        ImageView logo = (ImageView)findViewById(R.id.app_logo);
        logo.setImageResource(0);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if(null == action || null ==task_list) return;
        listView = (ListView) findViewById(R.id.task_listview);
        listView.setHeaderDividersEnabled(false);
        listView.setFooterDividersEnabled(false);
        adapter = new MyBaseAdapter(SelectedTaskActivity.this, task_list);
        adapter.setHideCompletedTask(sharedPreferences.getBoolean(AppContent.sp_hidetask_flag , false));
        adapter.updateList(task_list);
        listView.setAdapter(adapter);
        updateSearchEditTextListener();

        if(action.equals(AppContent.action_function_prioritize)) {
            toolbarTitle.setText(rcs.getString(R.string.prioritize_task_label));
            toolbar.setBackgroundColor(rcs.getColor(R.color.Prioritize_toolbar_color));
            listClickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(adapter.taskList.get(position).getPriority()>0) {
                        task_list.get(position).setPriority(0);
                        view.setBackgroundColor(rcs.getColor(R.color.default_background));
                    }else {
                        task_list.get(position).setPriority(1);
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
                    Intent intent = new Intent(SelectedTaskActivity.this, EditTaskActivity.class);
                    intent.setAction(AppContent.action_function_edit);
                    intent.putExtra(AppContent.edit_task_index, position);
                    intent.putExtra(AppContent.edit_task, (ToDoTask) adapter.getItem(position));
                    startActivityForResult(intent,0);
                }
            };
        } else if(action.equals(AppContent.action_function_delete)){
            toolbarTitle.setText(rcs.getString(R.string.deletetask_label));
            toolbar.setBackgroundColor(rcs.getColor(R.color.DEL_toolbar_color));
            listClickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final ToDoTask targetedTask = (ToDoTask) adapter.getItem(position);
                    if(targetedTask !=null) {
                        final int pos = position;
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SelectedTaskActivity.this);
                        alertDialog.setMessage("Are you sure to delete Task:\n\"" + targetedTask.getTask() + "\" ?");
                        alertDialog.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if(MainActivity.getDatabaseHander() != null) {
                                    MainActivity.getDatabaseHander().updateTaskToList(null, pos);
                                }
                                task_list.remove(pos);
                                adapter.notifyDataSetChanged();
                            }
                        });
                        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        alertDialog.show();
                    }
                }
            };
        }
        listView.setOnItemClickListener(listClickListener);
        if(searchFor!=null && intent.getStringExtra(AppContent.current_search_string)!=null)
            searchFor.setText(intent.getStringExtra(AppContent.current_search_string));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                Bundle b=data.getExtras();
               int pos= b.getInt(AppContent.edit_task_index, -1);
                ToDoTask task = b.getParcelable(AppContent.edit_task);
                if(pos!= -1 && task !=null){
                    task_list.set(pos, task);
                    adapter.notifyDataSetChanged();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed(){
        operationBeforeDead();
        this.finish();
        super.onBackPressed();
    }

    @Override
    public void finish(){
        action = null;
        super.finish();
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
                    setContentView(R.layout.selected_task_activity_with_search);
                    initView();
                    MainActivity.searchFunctionOn = true;
                    searchFor = (EditText) findViewById(R.id.inputSearch);
                    updateSearchEditTextListener();
                }else{
                    searchFor = null;
                    setContentView(R.layout.selected_task_activity);
                    initView();
                    MainActivity.searchFunctionOn = false;
                }
                adapter.notifyDataSetChanged();
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
            case android.R.id.home:
                operationBeforeDead();
                this.finish();
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

    private void updateSearchEditTextListener(){
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
    }
}
