package com.example.to_do.ui.home.adapter;


import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.to_do.R;
import com.example.to_do.data.model.Task;
import com.example.to_do.data.model.Category;
import com.example.to_do.ui.home.view.HomeFragment;
import com.example.to_do.ui.newtask.model.TaskRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final HomeFragment fragment;
    private final List<Task> taskListFiltered = new ArrayList<>();
    private final TaskRepository taskRepository;

    public TaskAdapter(HomeFragment fragment) {
        this.fragment = fragment;
        this.taskRepository = TaskRepository.getInstance(fragment.requireContext());
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryIcon;
        TextView taskTitle;
        TextView taskDescription;
        TextView taskDueDate;
        TextView taskPriority;
        CheckBox taskCompleted;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            taskDueDate = itemView.findViewById(R.id.taskDueDate);
            taskPriority = itemView.findViewById(R.id.taskPriority);
            taskCompleted = itemView.findViewById(R.id.taskCompleted);
        }
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskListFiltered.get(position);

        holder.taskTitle.setText(task.getTitle());

        if (task.getDescription().isEmpty()) {
            holder.taskDescription.setVisibility(View.GONE);
            holder.taskPriority.setVisibility(View.GONE);
            holder.taskDueDate.setVisibility(View.GONE);
        } else {
            holder.taskDescription.setText(task.getDescription());
            holder.taskPriority.setText("Prioridad: " + task.getPriority());
            holder.taskDueDate.setText("Fecha de vencimiento: " + formatDate(task.getDueDate()));
        }

        holder.taskCompleted.setChecked(task.isCompleted());
        holder.categoryIcon.setImageResource(getCategoryIcon(task.getCategory()));

        holder.taskCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            if (isChecked) {
                showMessageDialog(position);
            }
        });
    }

    private void showMessageDialog(int position) {
        LayoutInflater inflater = fragment.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_delete_task, null);

        AlertDialog dialog = new AlertDialog.Builder(fragment.requireContext())
                .setView(dialogView)
                .create();

        dialog.show();

        Button btnDelete = dialogView.findViewById(R.id.delete_button);
        Button btnCancel = dialogView.findViewById(R.id.cancel_button);

        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();

            Task taskToDelete = taskListFiltered.get(position);

            taskListFiltered.remove(position);
            notifyItemRemoved(position);

            List<Task> allTasks = taskRepository.getTasks();
            allTasks.removeIf(task -> task.getId() == taskToDelete.getId());

            taskRepository.saveTasks(allTasks);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }
    public void submitList(List<Task> filteredTasks) {
        taskListFiltered.clear();
        taskListFiltered.addAll(filteredTasks);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return taskListFiltered.size();
    }

    private String formatDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return format.format(date);
    }

    private int getCategoryIcon(Category category) {
        switch (category) {
            case WORK:
                return R.drawable.ic_category_work;
            case PERSONAL:
                return R.drawable.ic_category_personal;
            case SHOPPING:
                return R.drawable.ic_category_shopping;
            case HEALTH:
                return R.drawable.ic_category_health;
            case OTHER:
            default:
                return R.drawable.ic_category_default;
        }
    }
}