package com.example.to_do.ui.newtask.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.to_do.data.model.Task;
import com.example.to_do.ui.newtask.model.TaskRepository;

import java.util.List;

public class NewTaskViewModel extends AndroidViewModel {

    private TaskRepository taskRepository;
    private MutableLiveData<List<Task>> _tasks = new MutableLiveData<>();
    public LiveData<List<Task>> tasks = _tasks;

    public NewTaskViewModel(@NonNull Application application) {
        super(application);
        taskRepository = TaskRepository.getInstance(application);

        loadTasks();
    }

    private void loadTasks() {
        _tasks.setValue(taskRepository.getTasks());
    }

    public void saveTasks(List<Task> tasks) {
        _tasks.setValue(tasks);
        taskRepository.saveTasks(tasks);
    }
}