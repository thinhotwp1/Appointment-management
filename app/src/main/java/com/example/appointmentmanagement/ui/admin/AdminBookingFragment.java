package com.example.appointmentmanagement.ui.admin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appointmentmanagement.R;
import com.example.appointmentmanagement.model.Service;
import com.example.appointmentmanagement.repository.ServiceRepository;
import com.example.appointmentmanagement.ui.adapter.ServiceAdapter;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for admin to manage booking services.
 */
public class AdminBookingFragment extends Fragment {

    private RecyclerView recyclerView;
    private ServiceAdapter adapter;
    private List<Service> serviceList;
    private ServiceRepository repository;
    private MaterialButton btnAddService;

    @SuppressLint("WrongViewCast")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_booking, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewServices);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        btnAddService = view.findViewById(R.id.btnAddService);

        repository = new ServiceRepository();
        serviceList = new ArrayList<>();
        
        adapter = new ServiceAdapter(serviceList, new ServiceAdapter.OnItemClickListener() {
            @Override
            public void onEdit(Service service) {
                showEditDialog(service);
            }

            @Override
            public void onDelete(Service service) {
                deleteService(service);
            }
        });

        recyclerView.setAdapter(adapter);
        loadServices();

        btnAddService.setOnClickListener(v -> showAddServiceDialog());

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadServices() {
        repository.getAllServices(new ServiceRepository.ServiceCallback() {
            @Override
            public void onSuccess(List<Service> services) { // Đảm bảo phương thức đúng tên
                adapter.updateList(services); // Cập nhật danh sách dịch vụ
            }

            @Override
            public void onFailure(Exception e) { // Nếu ServiceCallback có phương thức này
                Toast.makeText(requireContext(), "Failed to load services: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Displays a dialog to edit an existing service.
     *
     * @param service The service to be edited.
     */
    private void showEditDialog(Service service) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_service, null);
        EditText etServiceName = dialogView.findViewById(R.id.etServiceName);
        etServiceName.setText(service.getName());

        new AlertDialog.Builder(requireContext())
            .setTitle("Edit Service")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                String newName = etServiceName.getText().toString();
                if (!newName.isEmpty()) {
                    updateService(service, newName);
                } else {
                    Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Updates a service name in Firestore.
     *
     * @param service The service to be updated.
     * @param newName The new name for the service.
     */
    private void updateService(Service service, String newName) {
        repository.updateService(service.getId(), newName, () -> {
            Toast.makeText(requireContext(), "Service updated", Toast.LENGTH_SHORT).show();
            loadServices();
        });
    }

    /**
     * Deletes a service from Firestore.
     *
     * @param service The service to be deleted.
     */
    private void deleteService(Service service) {
        repository.deleteService(service.getId(), () -> {
            Toast.makeText(requireContext(), "Service deleted", Toast.LENGTH_SHORT).show();
            loadServices();
        });
    }

    /**
     * Displays a dialog to add a new service.
     */
    private void showAddServiceDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_service, null);
        EditText etServiceName = dialogView.findViewById(R.id.etServiceName);

        new AlertDialog.Builder(requireContext())
            .setTitle("Add Service")
            .setView(dialogView)
            .setPositiveButton("Add", (dialog, which) -> {
                String name = etServiceName.getText().toString();
                if (!name.isEmpty()) {
                    repository.addService(name, () -> {
                        Toast.makeText(requireContext(), "Service added", Toast.LENGTH_SHORT).show();
                        loadServices();
                    });
                } else {
                    Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}