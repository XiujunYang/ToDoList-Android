package com.example.jean.todolist;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
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

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Jean on 2017/1/21.
 */

public class MyBaseAdapter extends BaseAdapter implements Filterable {

    private final String LOG_TAG = "MyBaseAdapter";
    private LayoutInflater myInflater;
    List<ToDoTask> taskList;
    private Context mContext;
    private ViewHolder holder = null;
    private Boolean hideCompletedTask = false;
    private TaskFilter filter;

    public MyBaseAdapter(Context context, List<ToDoTask> tasklist){
        this.mContext = context;
        this.myInflater = LayoutInflater.from(mContext);
        this.taskList = tasklist;
        this.hideCompletedTask =mContext.getSharedPreferences(AppContent.SharedPreferences_Name,MODE_PRIVATE)
                .getBoolean(AppContent.sp_hidetask_flag , false);;
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public Object getItem(int position) {
        synchronized (this.taskList) {
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
            holder.mDate.setClickable(false);
            holder.mTask.setClickable(false);
            if(task.isCompleted()) {
                holder.mDate.setPaintFlags(holder.mDate.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.mTask.setPaintFlags(holder.mDate.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.mDate.setTextColor(Color.GRAY);
                holder.mTask.setTextColor(Color.GRAY);
            } else{
                // Reset to default style for holder, because holder will be re-use in the other row.
                holder.mDate.setPaintFlags(holder.mDate.getPaintFlags() & 0xFFFFFFEF);
                holder.mTask.setPaintFlags(holder.mDate.getPaintFlags() & 0xFFFFFFEF);
                holder.mDate.setTextColor(Color.BLACK);
                holder.mTask.setTextColor(Color.BLACK);
            }
            if (task.getPriority() > 0) convertView.setBackgroundColor(
                    mContext.getResources().getColor(R.color.importantTaskBgdColor));
            else convertView.setBackgroundColor(
                    mContext.getResources().getColor(R.color.default_background));
            return convertView;
        }
    }
    public void setHideCompletedTask(boolean value){this.hideCompletedTask = value;}
    public boolean isHideCompletedTask(){return hideCompletedTask;}

    /**
     *  Used to filter completed task while user turn on function hiding finished task.
     * @param taskList
     */
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
            } else this.taskList = taskList;
            Log.i(LOG_TAG, "["+Thread.currentThread()+"] updateList, count:" + this.taskList.size());
        this.notifyDataSetChanged();
        }
    }

    @Override
    public Filter getFilter() {
        if(filter == null) filter = new TaskFilter();
        return filter;
    }

    public class ViewHolder{
        TextView mDate;
        TextView mTask;

        public ViewHolder(View view) {
            mDate = (TextView) view.findViewById(R.id.Date);
            mTask = (TextView) view.findViewById(R.id.Task);
        }
    }

    private class TaskFilter extends Filter{
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
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
                        ToDoTask item = new ToDoTask(taskList.get(i).getDate(),
                                taskList.get(i).getTask(),taskList.get(i).isCompleted(),
                                taskList.get(i).getPriority());
                        filterList.add(item);
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
