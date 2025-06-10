package com.example.to_do.ui.home.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.to_do.MainActivity;
import com.example.to_do.R;
import com.example.to_do.data.model.Priority;
import com.example.to_do.data.model.Task;
import com.example.to_do.databinding.FragmentHomeBinding;
import com.example.to_do.ui.home.adapter.TaskAdapter;
import com.example.to_do.ui.newtask.model.TaskRepository;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private TaskRepository taskRepository;
    private TaskAdapter taskAdapter;

    private String selectedPriority = "Todas";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        taskRepository = TaskRepository.getInstance(requireContext());

        setupRecyclerView();
        setupSpinner();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(this);
        binding.recyclerViewTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewTasks.setAdapter(taskAdapter);

        showFilteredTasks();
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.priority_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPriority.setAdapter(adapter);

        binding.spinnerPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPriority = parent.getItemAtPosition(position).toString();
                showFilteredTasks();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showFilteredTasks() {
        List<Task> allTasks = taskRepository.getTasks();
        List<Task> filteredTasks = new ArrayList<>();

        switch (selectedPriority) {
            case "Alta":
                for (Task t : allTasks)
                    if (t.getPriority() == Priority.HIGH) filteredTasks.add(t);
                break;
            case "Media":
                for (Task t : allTasks)
                    if (t.getPriority() == Priority.MEDIUM) filteredTasks.add(t);
                break;
            case "Baja":
                for (Task t : allTasks)
                    if (t.getPriority() == Priority.LOW) filteredTasks.add(t);
                break;
            case "Todas":
            default:
                filteredTasks = allTasks;
                break;
        }

        taskAdapter.submitList(filteredTasks);

        if (filteredTasks.isEmpty()) {
            binding.emptyTasks.setVisibility(View.VISIBLE);
        } else {
            binding.emptyTasks.setVisibility(View.GONE);
        }
    }




}
