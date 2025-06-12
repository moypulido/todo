package com.example.to_do.ui.newtask.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.to_do.data.model.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TaskRepository {

    private static final String PREFS_NAME = "task_prefs";
    private static final String TASK_LIST_KEY = "task_list_key";

    private static TaskRepository instance;

    private final SharedPreferences sharedPreferences;
    private final Gson gson;
    private final Type taskListType;

    private List<Task> taskList;

    private TaskRepository(Context context) {
        this.sharedPreferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.taskListType = new TypeToken<List<Task>>() {}.getType();
        loadTasks();
    }

    public static synchronized TaskRepository getInstance(Context context) {
        if (instance == null) {
            instance = new TaskRepository(context);
        }
        return instance;
    }

    private void loadTasks() {
        String json = sharedPreferences.getString(TASK_LIST_KEY, null);
        if (json != null) {
            taskList = gson.fromJson(json, taskListType);
        } else {
            taskList = new ArrayList<>();
        }
    }

    public List<Task> getTasks() {
        return new ArrayList<>(taskList);
    }

    public void saveTasks(List<Task> taskList) {
        this.taskList = new ArrayList<>(taskList);  // Actualiza también la lista interna
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(this.taskList);
        editor.putString(TASK_LIST_KEY, json);
        editor.apply();
    }

    public void addTask(Task task) {
        taskList.add(task);
        saveTasks(taskList);
    }

    public void deleteTask(Task task) {
        Iterator<Task> iterator = taskList.iterator();
        while (iterator.hasNext()) {
            Task t = iterator.next();
            if (t.getId() == task.getId()) {
                iterator.remove();
                break;
            }
        }
        saveTasks(taskList);
    }

    public void updateTask(Task updatedTask) {
        for (int i = 0; i < taskList.size(); i++) {
            Task t = taskList.get(i);
            if (t.getId() == updatedTask.getId()) {
                taskList.set(i, updatedTask);
                break;
            }
        }
        saveTasks(taskList);
    }

    public void clearTasks() {
        taskList.clear();
        saveTasks(taskList);
    }
}
