package com.example.jean.todolist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class SelectedTaskActivity extends AppCompatActivity {
    private final String LOG_TAG = "SelectedTaskActivity";

    private Resources rcs;
    private ListView listView;
    List<ToDoTask> task_list = new ArrayList<ToDoTask>();
    private MyBaseAdapter adapter;
    AdapterView.OnItemClickListener listClickListener;
    boolean isDoingPriority = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rcs = this.getResources();
        setContentView(R.layout.content_main);
        init();
    }

    protected void onResume(){
        super.onResume();
        init();
    }

    private void init(){
        Intent intent = getIntent();
        task_list = intent.getParcelableArrayListExtra(AppContent.displayed_task_list);
        String called_source = intent.getAction();
        if(null == called_source || null ==task_list) return;

        listView = (ListView) findViewById(R.id.task_listview);
        adapter = new MyBaseAdapter(SelectedTaskActivity.this, task_list);
        listView.setAdapter(adapter);
        if(called_source.equals(AppContent.action_function_prioritize)) {
            this.setTitle(rcs.getString(R.string.prioritize_task_label));
            isDoingPriority = true;
            listClickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(adapter.taskList.get(position).getPriority()>0) {
                        task_list.get(position).setPriority(0);
                        listView.getChildAt(position).setBackgroundColor(rcs.getColor(R.color.default_background));
                    }else {
                        task_list.get(position).setPriority(1);
                        listView.getChildAt(position).setBackgroundColor(rcs.getColor(R.color.importantTaskBgdColor));
                    }
                }
            };
        } else if(called_source.equals(AppContent.action_function_edit)) {
            this.setTitle(rcs.getString(R.string.selected_edittask_label));
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
        } else if(called_source.equals(AppContent.action_function_delete)){
            this.setTitle(rcs.getString(R.string.selected_deletetask_label));
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
                                //while(task_list.contains(targetedTask)) task_list.remove(targetedTask);
                                task_list.remove(pos);
                                adapter.notifyDataSetChanged();
                                //startActivity(new Intent(SelectedTaskActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP ));
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
        if(isDoingPriority){
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(AppContent.displayed_task_list, (ArrayList<ToDoTask>) task_list);
            Intent intent = new Intent();
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
            isDoingPriority = false;
            super.onBackPressed();
            finish();
        }
        super.onBackPressed();
    }
}
