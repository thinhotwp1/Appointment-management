package com.example.appointmentmanagement.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appointmentmanagement.R;
import com.example.appointmentmanagement.model.Appointment;
import com.example.appointmentmanagement.ui.adapter.AdminAppointmentAdapter;
import com.example.appointmentmanagement.viewmodel.SharedViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminFragment extends Fragment {
    private RecyclerView rvAllAppointments;
    private AdminAppointmentAdapter adapter;
    private SharedViewModel sharedViewModel;
    private List<Appointment> appointmentList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        rvAllAppointments = view.findViewById(R.id.rvAllAppointments);
        rvAllAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        appointmentList = new ArrayList<>();
        adapter = new AdminAppointmentAdapter(appointmentList, getContext());
        rvAllAppointments.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadAllAppointments();
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);


        return view;
    }

    private void loadAllAppointments() {
        db.collection("appointments").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                appointmentList.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Appointment appointment = doc.toObject(Appointment.class);
                    appointment.setId(doc.getId());

                    // Lấy userId của người đặt lịch
                    String userId = appointment.getUserId();
                    db.collection("users").document(userId).get()
                            .addOnSuccessListener(userDoc -> {
                                if (userDoc.exists()) {
                                    String userName = userDoc.getString("email"); // Hoặc "name" nếu có
                                    appointment.setUserEmail(userName); // Cập nhật tên người dùng
                                } else {
                                    appointment.setUserEmail("UnknownUser@user.com");
                                }
                                adapter.notifyDataSetChanged();
                            });

                    appointmentList.add(appointment);
                }
            } else {
                Toast.makeText(getContext(), "Failed to load appointments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBookingStatus(String appointmentId, String newStatus) {
        db.collection("appointments").document(appointmentId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Updated status to " + newStatus, Toast.LENGTH_SHORT).show();
                    loadAllAppointments();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update", Toast.LENGTH_SHORT).show());
    }
}
