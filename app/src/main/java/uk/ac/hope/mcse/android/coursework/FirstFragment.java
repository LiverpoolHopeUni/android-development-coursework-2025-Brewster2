package uk.ac.hope.mcse.android.coursework;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {
    private FragmentFirstBinding binding;
    private TaskAdapter adapter;
    private final List<String> tasks = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        loadTasks();
        setupRecyclerView();
        checkEmptyState();
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener("taskRequest", this, (requestKey, bundle) -> {
            String newTask = bundle.getString("newTask");
            if (newTask != null) {
                tasks.add(newTask);
                adapter.notifyItemInserted(tasks.size() - 1);
                saveTasks();
                checkEmptyState();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new TaskAdapter(tasks);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                tasks.remove(position);
                adapter.notifyItemRemoved(position);
                saveTasks();
                checkEmptyState();
            }
        }).attachToRecyclerView(binding.recyclerView);
    }

    private void saveTasks() {
        SharedPreferences prefs = requireContext().getSharedPreferences("tasks", Context.MODE_PRIVATE);
        Set<String> taskSet = new HashSet<>(tasks);
        prefs.edit().putStringSet("taskList", taskSet).apply();
    }

    private void loadTasks() {
        SharedPreferences prefs = requireContext().getSharedPreferences("tasks", Context.MODE_PRIVATE);
        tasks.clear();
        Set<String> savedTasks = prefs.getStringSet("taskList", new HashSet<>());
        if (savedTasks != null) {
            tasks.addAll(savedTasks);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void checkEmptyState() {
        boolean isEmpty = adapter == null || adapter.getItemCount() == 0;
        binding.recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}