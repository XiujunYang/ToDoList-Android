package com.example.jean.todolist;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Jean on 2017/1/21.
 */

public class MyBaseAdapter extends BaseAdapter {

    final String LOG_TAG = "MyBaseAdapter";
    LayoutInflater myInflater;
    List<ToDoTask> taskList;
    Context mContext;
    ViewHolder holder = null;

    public MyBaseAdapter(Context context, List<ToDoTask> tasklist){
        this.mContext = context;
        this.myInflater = LayoutInflater.from(mContext);
        this.taskList = tasklist;
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public Object getItem(int position) {
        return taskList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return taskList.indexOf(getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            convertView = myInflater.inflate(R.layout.task_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else{
            holder = (MyBaseAdapter.ViewHolder) convertView.getTag();
        }

        ToDoTask task = (ToDoTask) getItem(position);
        holder.mDate.setText(task.getDate());
        holder.mTask.setText(task.getTask());
        holder.mCompleted.setText(String.valueOf(task.isCompleted()));
        holder.mDate.setClickable(false);
        holder.mTask.setClickable(false);
        holder.mCompleted.setClickable(false);

        return convertView;
    }


    public class ViewHolder{
        TextView mDate;
        TextView mTask;
        TextView mCompleted;

        public ViewHolder(View view) {
            mDate = (TextView) view.findViewById(R.id.Date);
            mTask = (TextView) view.findViewById(R.id.Task);
            mCompleted = (TextView) view.findViewById(R.id.Completed);
        }
    }
}
