package com.example.jean.todolist;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by Jean on 2017/3/26.
 */
public class ToDoTaskTest {
    @Test
    public void equals() throws Exception {
        ToDoTask oldTask = new ToDoTask("2017-03-01","Test1",false,-1);
        ToDoTask newTask = new ToDoTask("2017-03-01","Tes",false,-1);
        assertEquals(false, oldTask.equals(newTask));
    }

    @Test
    public void list_contain_check() throws Exception {
        ArrayList<ToDoTask> taskList = new ArrayList<ToDoTask>();
        ToDoTask task1 = new ToDoTask("2017-03-01", "Test1", false, -1);
        ToDoTask task2 = new ToDoTask("2017-03-01", "Test2", false, -1);
        ToDoTask task3 = new ToDoTask("2017-03-01", "Test1", false, -1);
        taskList.add(task1);
        assertEquals(false, taskList.contains(task2));
        assertEquals(true, taskList.contains(task3));
    }

}