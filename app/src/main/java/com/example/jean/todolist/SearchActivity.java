package com.example.jean.todolist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private final String LOG_TAG = "SearchActivity";

    List<ToDoTask> list = new ArrayList<ToDoTask>();
    private MyBaseAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Intent intent = getIntent();
        listView = (ListView) findViewById(R.id.task_listview);
        list = intent.getParcelableArrayListExtra(AppContent.displayed_task_list);
        adapter = new MyBaseAdapter(SearchActivity.this,list);
        listView.setAdapter(adapter);
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ToDoTask item = (ToDoTask) adapter.getItem(position);
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppContent.search_task, item);
                Intent intent = new Intent();
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }
        };
        listView.setOnItemClickListener(listener);

        EditText searchFor = (EditText) findViewById(R.id.inputSearch);
        searchFor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable arg0) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.updateList(list);
                adapter.getFilter().filter(s);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
