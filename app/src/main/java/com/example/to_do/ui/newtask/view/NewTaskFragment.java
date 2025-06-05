package com.example.to_do.ui.newtask.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.to_do.MainActivity;
import com.example.to_do.R;
import com.example.to_do.data.model.Category;
import com.example.to_do.data.model.Priority;
import com.example.to_do.data.model.Task;
import com.example.to_do.databinding.FragmentNewtaskBinding;
import com.example.to_do.ui.newtask.model.TaskRepository;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewTaskFragment extends Fragment {

    private FragmentNewtaskBinding binding;
    private String selectedDate = "";
    private String selectedTime = "";
    private int taskIdCounter = 1;
    private TaskRepository taskRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentNewtaskBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        taskRepository = TaskRepository.getInstance(requireContext());


        setupSpinner();
        setupListeners();
        return root;
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
        // Crear selector de fecha (MaterialDatePicker)
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona una fecha")
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);

            selectedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime());

            // Luego mostramos el TimePicker
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTitleText("Selecciona una hora")
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(calendar.get(Calendar.HOUR_OF_DAY))
                    .setMinute(calendar.get(Calendar.MINUTE))
                    .build();

            timePicker.addOnPositiveButtonClickListener(v -> {
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendar.set(Calendar.MINUTE, timePicker.getMinute());

                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", timePicker.getHour(), timePicker.getMinute());

                // Mostrar en el botón combinado
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

        Date dueDate = parseDateTime(selectedDate, selectedTime);

        // Carga la lista actual desde repositorio
        List<Task> currentTasks = taskRepository.getTasks();

        // Opcional: actualizar el contador de id con base en la lista (para evitar ids repetidos)
        if (!currentTasks.isEmpty()) {
            int maxId = 0;
            for (Task t : currentTasks) {
                if (t.getId() > maxId) maxId = t.getId();
            }
            taskIdCounter = maxId + 1;
        }

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

        // Guardar lista actualizada en repositorio
        taskRepository.saveTasks(currentTasks);

        Toast.makeText(requireContext(), "Tarea guardada con éxito", Toast.LENGTH_SHORT).show();

        clearFields();
    }

    private Date parseDateTime(String date, String time) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            return format.parse(date + " " + time);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
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
}
