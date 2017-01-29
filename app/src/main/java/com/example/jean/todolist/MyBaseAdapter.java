package com.example.jean.todolist;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Jean on 2017/1/21.
 */

public class MyBaseAdapter extends BaseAdapter implements Filterable {

    final String LOG_TAG = "MyBaseAdapter";
    LayoutInflater myInflater;
    List<ToDoTask> taskList;
    Context mContext;
    ViewHolder holder = null;
    Boolean hideCompletedTask = false;
    TaskFilter filter;

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
        synchronized (this.taskList) {
            //Log.i(LOG_TAG, "position=" + position + "; size=" + taskList.size());
            if(position < taskList.size()) return taskList.get(position);
            return null;
        }
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
        synchronized(this.taskList) {
        ToDoTask task = (ToDoTask) getItem(position);
            if (task == null) return convertView;
            holder.mDate.setText(task.getDate());
            holder.mTask.setText(task.getTask());
            holder.mCompleted.setText(String.valueOf(task.isCompleted()));
            holder.mDate.setClickable(false);
            holder.mTask.setClickable(false);
            holder.mCompleted.setClickable(false);
            if (task.getPriority() > 0) convertView.setBackgroundColor(
                    mContext.getResources().getColor(R.color.importantTaskBgdColor));
            else convertView.setBackgroundColor(
                    mContext.getResources().getColor(R.color.default_background));
            if (MainActivity.searchedTaskPos == position)
                convertView.setBackgroundResource(R.drawable.highlight_searched_task);
            return convertView;
        }
    }

    public void updateList(List<ToDoTask> taskList){
        synchronized (this.taskList) {
        List<ToDoTask> newList = new ArrayList<ToDoTask>();
            if (hideCompletedTask) {
            Iterator it = taskList.iterator();
                while (it.hasNext()) {
                ToDoTask task = (ToDoTask) it.next();
                    if (!task.isCompleted()) newList.add(task);
            }
            this.taskList = newList;
            } else {
            this.taskList = taskList;
            }
            Log.i(LOG_TAG, "updateList, count:" + this.taskList.size());
        this.notifyDataSetChanged();
        }
    }

    public void setHideCompletedTask(boolean value){ this.hideCompletedTask = value;}
    public boolean isHideCompletedTask(){return hideCompletedTask;}

    @Override
    public Filter getFilter() {
        if(filter == null) filter = new TaskFilter();
        return filter;
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

    private class TaskFilter extends Filter{
        //Invoked in a worker thread to filter the data according to the constraint.

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            taskList=(ArrayList<ToDoTask>) results.values;
            notifyDataSetChanged();
        }

        @Override
        protected  Filter.FilterResults performFiltering(CharSequence constraint) {
            FilterResults  results=new FilterResults ();
            if(constraint!=null && constraint.length()>0){
                ArrayList<ToDoTask> filterList=new ArrayList<ToDoTask>();
                for(int i=0;i<taskList.size();i++){
                    if((taskList.get(i).getTask().toLowerCase())
                            .contains(constraint.toString().toLowerCase())) {
                        ToDoTask contacts = new ToDoTask(taskList.get(i).getDate(),
                                taskList.get(i).getTask(),taskList.get(i).isCompleted(),
                                taskList.get(i).getPriority());
                        filterList.add(contacts);
                    }
                }
                results.count=filterList.size();
                results.values=filterList;
                taskList = filterList;
            }else{
                results.count=taskList.size();
                results.values=taskList;
            }
            return results;
        }
    }
}
