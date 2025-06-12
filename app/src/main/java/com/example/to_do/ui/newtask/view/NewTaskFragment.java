package com.example.to_do.ui.newtask.view;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.to_do.R;
import com.example.to_do.broadcast.TaskAlarmReceiver;
import com.example.to_do.data.model.Category;
import com.example.to_do.data.model.Priority;
import com.example.to_do.data.model.Task;
import com.example.to_do.databinding.FragmentNewtaskBinding;
import com.example.to_do.ui.newtask.model.TaskRepository;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NewTaskFragment extends Fragment {

    private FragmentNewtaskBinding binding;
    private String selectedDate = "";
    private String selectedTime = "";
    private int taskIdCounter = 1;
    private TaskRepository taskRepository;
    private Task taskToEdit = null;
    private Calendar selectedCalendar = null;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentNewtaskBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        taskRepository = TaskRepository.getInstance(requireContext());

        checkTask();

        setupSpinner();
        setupListeners();
        return root;
    }

    private void checkTask() {
        if (getArguments() != null && getArguments().containsKey("task_to_edit")) {
            taskToEdit = (Task) getArguments().getSerializable("task_to_edit");
            populateFieldsForEdit();
            binding.titleMain.setText("Editar tarea"); // Cambia el título
            binding.saveTaskButton.setText("Guardar cambios");
        }
    }
    private void populateFieldsForEdit() {
        binding.titleInput.setText(taskToEdit.getTitle());
        binding.descriptionInput.setText(taskToEdit.getDescription());

        // Prioridad
        switch (taskToEdit.getPriority()) {
            case HIGH: binding.priorityHigh.setChecked(true); break;
            case MEDIUM: binding.priorityMedium.setChecked(true); break;
            case LOW: binding.priorityLow.setChecked(true); break;
            default: break;
        }

        // Categoría (si el orden del enum coincide con el spinner)
        binding.categorySpinner.setSelection(taskToEdit.getCategory().ordinal());

        // Fecha y hora
        selectedCalendar = Calendar.getInstance();
        selectedCalendar.setTime(taskToEdit.getDueDate());
        selectedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedCalendar.getTime());
        selectedTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedCalendar.getTime());
        binding.selectDateTimeButton.setText(selectedDate + " " + selectedTime);

    }
    private void setupSpinner() {
        Category[] categories = Category.values();
        String[] categoryNames = new String[categories.length];

        for (int i = 0; i < categories.length; i++) {
            categoryNames[i] = categories[i].getDisplayName(requireContext());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                categoryNames
        );
        binding.categorySpinner.setAdapter(adapter);
    }
    private void setupListeners() {
        binding.selectDateTimeButton.setOnClickListener(v -> showDateTimePicker());
        binding.saveTaskButton.setOnClickListener(v -> saveTask());
    }
    private void showDateTimePicker() {
        Calendar now = Calendar.getInstance();

        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        long todayStartMillis = now.getTimeInMillis();

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona una fecha")
                .setSelection(now.getTimeInMillis())
                .setCalendarConstraints(
                        new CalendarConstraints.Builder()
                                .setStart(todayStartMillis)
                                .build()
                )
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Crea un Calendar en UTC, pon la fecha seleccionada
            Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            utcCalendar.setTimeInMillis(selection);

            Calendar localCalendar = Calendar.getInstance();
            localCalendar.set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR));
            localCalendar.set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH));
            localCalendar.set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH));

            localCalendar.set(Calendar.HOUR_OF_DAY, 0);
            localCalendar.set(Calendar.MINUTE, 0);
            localCalendar.set(Calendar.SECOND, 0);
            localCalendar.set(Calendar.MILLISECOND, 0);

            selectedCalendar = localCalendar;

            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTitleText("Selecciona una hora")
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(localCalendar.get(Calendar.HOUR_OF_DAY))
                    .setMinute(localCalendar.get(Calendar.MINUTE))
                    .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
                    .build();

            timePicker.addOnPositiveButtonClickListener(v -> {
                selectedCalendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                selectedCalendar.set(Calendar.MINUTE, timePicker.getMinute());
                selectedCalendar.set(Calendar.SECOND, 0);

                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                SimpleDateFormat tf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                selectedDate = df.format(selectedCalendar.getTime());
                selectedTime = tf.format(selectedCalendar.getTime());

                binding.selectDateTimeButton.setText(selectedDate + " " + selectedTime);
            });

            timePicker.show(getParentFragmentManager(), "TIME_PICKER");
        });


        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }
    private void saveTask() {
        String title = binding.titleInput.getText().toString().trim();
        String description = binding.descriptionInput.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || selectedDate.isEmpty() || selectedTime.isEmpty()
                || binding.priorityGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(requireContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Date dueDate = getSelectedDueDate();
        if (dueDate == null) {
            Toast.makeText(requireContext(), "Selecciona fecha y hora", Toast.LENGTH_SHORT).show();
            return;
        }
        Date today = Calendar.getInstance().getTime();
        if (dueDate.before(today)) {
            Toast.makeText(requireContext(), "La fecha seleccionada no puede ser anterior a hoy", Toast.LENGTH_SHORT).show();
            return;
        }

        Priority priority;
        int checkedId = binding.priorityGroup.getCheckedRadioButtonId();
        if (checkedId == binding.priorityHigh.getId()) {
            priority = Priority.HIGH;
        } else if (checkedId == binding.priorityMedium.getId()) {
            priority = Priority.MEDIUM;
        } else if (checkedId == binding.priorityLow.getId()) {
            priority = Priority.LOW;
        } else {
            priority = Priority.OTHER;
        }

        Category category = Category.values()[binding.categorySpinner.getSelectedItemPosition()];

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Confirmar")
                .setMessage("¿Seguro de guardar tarea?")

                .setPositiveButton("Sí", (dialog, which) -> {
                    saveTaskToRepository(title, description, dueDate, priority, category);
                    scheduleTaskAlarm(requireContext(), title, dueDate.getTime());
                    requireActivity().onBackPressed();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void saveTaskToRepository(String title, String description, Date dueDate, Priority priority, Category category) {
        List<Task> currentTasks = taskRepository.getTasks();

        if (taskToEdit != null) {
            // Modo editar: actualiza la tarea existente
            Task updatedTask = new Task(
                    taskToEdit.getId(),  // MISMO id!
                    title,
                    description,
                    priority,
                    dueDate,
                    category,
                    taskToEdit.isCompleted() // Mantén el estado completado
            );

            // Actualiza la lista
            for (int i = 0; i < currentTasks.size(); i++) {
                if (currentTasks.get(i).getId() == taskToEdit.getId()) {
                    currentTasks.set(i, updatedTask);
                    break;
                }
            }
            taskRepository.saveTasks(currentTasks);

            Toast.makeText(requireContext(), "Tarea editada con éxito", Toast.LENGTH_SHORT).show();

        } else {
            // Modo crear: igual que antes
            int maxId = 0;
            for (Task t : currentTasks) {
                if (t.getId() > maxId) maxId = t.getId();
            }
            taskIdCounter = maxId + 1;

            Task task = new Task(
                    taskIdCounter++,
                    title,
                    description,
                    priority,
                    dueDate,
                    category,
                    false
            );

            currentTasks.add(task);
            taskRepository.saveTasks(currentTasks);

            Toast.makeText(requireContext(), "Tarea guardada con éxito", Toast.LENGTH_SHORT).show();
        }

        clearFields();
    }
    private void clearFields() {
        binding.titleInput.getText().clear();
        binding.descriptionInput.getText().clear();
        binding.priorityGroup.clearCheck();
        binding.categorySpinner.setSelection(0);
        binding.selectDateTimeButton.setText(R.string.seleccionar_fecha_y_hora);

        selectedDate = "";
        selectedTime = "";
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private Date getSelectedDueDate() {
        if (selectedCalendar != null) {
            return selectedCalendar.getTime();
        }
        return null;
    }

    public static void scheduleTaskAlarm(Context context, String taskTitle, long dueTimeMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Para Android 12+ verifica permiso antes de agendar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(context, "Activa el permiso de alarmas exactas en Configuración", Toast.LENGTH_LONG).show();
                Intent intentSettings = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                context.startActivity(intentSettings);
                return;
            }
        }

        Intent intent = new Intent(context, TaskAlarmReceiver.class);
        intent.putExtra("task_title", taskTitle);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, taskTitle.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    dueTimeMillis,
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    dueTimeMillis,
                    pendingIntent
            );
        }
    }


}
