package com.example.to_do.ui.home.adapter;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import android.text.format.DateUtils;
import java.text.SimpleDateFormat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.to_do.R;
import com.example.to_do.data.model.Task;
import com.example.to_do.data.model.Category;
import com.example.to_do.ui.home.view.HomeFragment;
import com.example.to_do.ui.newtask.model.TaskRepository;

import java.io.Serializable;
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

        holder.itemView.setOnClickListener(v -> editTask(task));

        holder.taskTitle.setText(task.getTitle());

        if (task.getDescription().isEmpty()) {
            holder.taskDescription.setVisibility(View.GONE);
            holder.taskPriority.setVisibility(View.GONE);
            holder.taskDueDate.setVisibility(View.GONE);
        } else {
            holder.taskDescription.setText(task.getDescription());
            holder.taskPriority.setText("Prioridad: " + task.getPriorityText());

            CharSequence relative = DateUtils.getRelativeTimeSpanString(
                    task.getDueDate().getTime(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            );
            holder.taskDueDate.setText(relative);

            if (task.getDueDate().before(new Date()) && !task.isCompleted()) {
                holder.taskDueDate.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.red));
            } else {
                holder.taskDueDate.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
            }

            holder.taskDueDate.setVisibility(View.VISIBLE);
        }

        holder.taskCompleted.setChecked(task.isCompleted());

        holder.categoryIcon.setImageResource(getCategoryIcon(task.getCategory()));

        holder.taskCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);

            taskRepository.updateTask(task);

            if (isChecked) {
                sendCompletedNotification(holder.itemView.getContext(), task.getTitle());
                showMessageDialog(position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(fragment.requireContext())
                    .setTitle("Eliminar tarea")
                    .setMessage("¿Seguro que deseas eliminar esta tarea?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        Task taskToDelete = taskListFiltered.get(holder.getAdapterPosition());

                        taskListFiltered.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());

                        List<Task> allTasks = taskRepository.getTasks();

                        allTasks.removeIf(t -> t.getId() == taskToDelete.getId());

                        taskRepository.saveTasks(allTasks);
                    })
                    .setNegativeButton("No", null)
                    .show();

            return true; // Muy importante, indica que el evento fue manejado
        });
    }
    private void sendCompletedNotification(Context context, String taskTitle) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "task_channel")
                .setSmallIcon(R.drawable.ic_category_default) // Usa un ícono válido
                .setContentTitle("Tarea completada")
                .setContentText("Has completado: " + taskTitle)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
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
    public void editTask(Task task) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("task_to_edit", task);
        NavController navController = NavHostFragment.findNavController(fragment);
        navController.navigate(R.id.action_navigation_home_to_navigation_dashboard, bundle);
    }
}