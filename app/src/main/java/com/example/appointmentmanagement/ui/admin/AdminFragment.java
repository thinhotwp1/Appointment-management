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
    private RecyclerView rvAllAppointments; // RecyclerView to display all appointments
    private AdminAppointmentAdapter adapter; // Adapter for managing appointment items
    private SharedViewModel sharedViewModel; // Shared ViewModel for data sharing
    private List<Appointment> appointmentList; // List of appointments
    private FirebaseFirestore db; // Firestore database instance

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        // Initialize RecyclerView and its properties
        rvAllAppointments = view.findViewById(R.id.rvAllAppointments);
        rvAllAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        appointmentList = new ArrayList<>();
        adapter = new AdminAppointmentAdapter(appointmentList, getContext());
        rvAllAppointments.setAdapter(adapter);

        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();
        loadAllAppointments(); // Fetch all appointments from Firestore

        // Initialize shared ViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        return view;
    }

    /**
     * Fetches all appointments from Firestore and updates the RecyclerView.
     */
    private void loadAllAppointments() {
        db.collection("appointments").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                appointmentList.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Appointment appointment = doc.toObject(Appointment.class);
                    appointment.setId(doc.getId()); // Set appointment ID

                    // Retrieve userId of the person who booked the appointment
                    String userId = appointment.getUserId();
                    db.collection("users").document(userId).get()
                            .addOnSuccessListener(userDoc -> {
                                if (userDoc.exists()) {
                                    String userName = userDoc.getString("email"); // Get user's email
                                    appointment.setUserEmail(userName);
                                } else {
                                    appointment.setUserEmail("unknown@user.com"); // Default email if user not found
                                }
                                adapter.notifyDataSetChanged(); // Refresh RecyclerView
                            });

                    appointmentList.add(appointment);
                }
            } else {
                Toast.makeText(getContext(), "Failed to load appointments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates the status of an appointment in Firestore.
     * @param appointmentId The ID of the appointment to update.
     * @param newStatus The new status to be set.
     */
    private void updateBookingStatus(String appointmentId, String newStatus) {
        db.collection("appointments").document(appointmentId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Updated status to " + newStatus, Toast.LENGTH_SHORT).show();
                    loadAllAppointments(); // Refresh the list after update
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to update", Toast.LENGTH_SHORT).show());
    }
}
