package com.example.jean.todolist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;


public class EditTaskActivity extends AppCompatActivity {
    private final String LOG_TAG = "EditTaskActivity";

    private DatePicker datePicker;
    private EditText taskDesc;
    private Spinner spinner;
    private ToDoTask newTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        ImageView logo = (ImageView)findViewById(R.id.app_logo);
        logo.setImageResource(0);
        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText(getApplicationContext().getString(R.string.edit_task_label));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        datePicker = (DatePicker) findViewById(R.id.date_picke_edit);
        taskDesc = (EditText) findViewById(R.id.task_edit);
        spinner = (Spinner) findViewById(R.id.status_edit);
        ArrayAdapter typeAdapter1 = ArrayAdapter.createFromResource(this,
                R.array.progressList, R.layout.spinner_status_list);
        spinner.setAdapter(typeAdapter1);
    }

    @Override
    protected  void onResume(){
        super.onResume();
        initData();
    }

    private void initData(){
        final Intent intent = getIntent();
        final String action = intent.getAction();
        final int priority;
        if(null!= action && action.equals(AppContent.action_function_edit)){
            ToDoTask task = intent.getParcelableExtra(AppContent.edit_task);
            if(task != null){
                String[] value = task.getDate().split("-");
                datePicker.init(Integer.parseInt(value[0]),Integer.parseInt(value[1])-1,Integer.parseInt(value[2]),null);
                taskDesc.setText(task.getTask());
                taskDesc.setSelection(task.getTask().length());//Let cursor put the end.
                spinner.setSelection(((ArrayAdapter)spinner.getAdapter()).getPosition(
                        String.valueOf(task.isCompleted())));
                priority = task.getPriority();
            } else priority=-1;
        } else{
            // AppContent.action_function_create
            priority = -1;
        }

        // if only create, show up following is enough.
        Button confirmBtn = (Button) findViewById(R.id.confirmBtn_edit);
        confirmBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                newTask = new ToDoTask(datePicker.getYear()+"-"+(datePicker.getMonth()+1)+"-"+datePicker.getDayOfMonth(),
                        taskDesc.getText().toString(),
                        spinner.getSelectedItem().toString().equals("true")? true:false, priority);
                if(MainActivity.getDatabaseHander() != null) {
                    MainActivity.getDatabaseHander().updateTaskToList(newTask, intent.getIntExtra(AppContent.edit_task_index,-1));
                }
                // Create's intent didn't exist AppContent.edit_task_index.
                Bundle bundle = new Bundle();
                bundle.putInt(AppContent.edit_task_index, intent.getIntExtra(AppContent.edit_task_index,-1));
                bundle.putParcelable(AppContent.edit_task, newTask);
                Intent intent = new Intent();
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }});
        Button cancelBtn = (Button) findViewById(R.id.cancelBtn_edit);
        cancelBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
            }});
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
