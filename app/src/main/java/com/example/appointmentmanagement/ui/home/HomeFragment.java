package com.example.appointmentmanagement.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appointmentmanagement.R;
import com.example.appointmentmanagement.model.Appointment;
import com.example.appointmentmanagement.repository.AppointmentRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {
    private RecyclerView rvAppointments;
    private AppointmentAdapter adapter;
    private AppointmentRepository repository;
    private TextView tvWelcome;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvAppointments = view.findViewById(R.id.rvAppointments);
        tvWelcome = view.findViewById(R.id.tvWelcome); // Ánh xạ TextView
        rvAppointments.setLayoutManager(new LinearLayoutManager(getContext()));

        repository = new AppointmentRepository();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            fetchUserInfo(user.getUid());
        } else {
            Toast.makeText(getContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void fetchUserInfo(String userId) {
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userName = Objects.requireNonNull(documentSnapshot.getString("email")).split("@")[0];
                        String role = documentSnapshot.getString("role");

                        tvWelcome.setText("Welcome, " + (userName != null ? userName : "User") + " !");

                        loadAppointments(userId, role);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch user info", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadAppointments(String userId, String role) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference appointmentsRef = db.collection("appointments");

        Query query;
        if ("admin".equals(role)) {
            query = appointmentsRef; // Admin lấy tất cả lịch hẹn
        } else {
            query = appointmentsRef.whereEqualTo("userId", userId); // User chỉ lấy lịch hẹn của họ
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Appointment> appointments = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Appointment appointment = doc.toObject(Appointment.class);
                appointment.setId(doc.getId()); // Lưu ID của Firestore
                appointments.add(appointment);
            }
            adapter = new AppointmentAdapter(appointments, getContext(), new AppointmentAdapter.OnAppointmentActionListener() {
                @Override
                public void onEdit(Appointment appointment) {
                    editAppointment(appointment);
                }

                @Override
                public void onDelete(Appointment appointment) {
                    deleteAppointment(appointment);
                }
            });
            rvAppointments.setAdapter(adapter);
        });
    }

    private void deleteAppointment(Appointment appointment) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("appointments").document(appointment.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Appointment deleted", Toast.LENGTH_SHORT).show();
                    loadAppointments(FirebaseAuth.getInstance().getCurrentUser().getUid(), "user"); // Tải lại danh sách
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("MissingInflatedId")
    private void editAppointment(Appointment appointment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Appointment");

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_appointment, null);
        EditText etService = view.findViewById(R.id.etService);
        EditText etDate = view.findViewById(R.id.etDate);
        EditText etTime = view.findViewById(R.id.etTime);
        Button btnSave = view.findViewById(R.id.btnSave);

        etService.setText(appointment.getService());
        etDate.setText(appointment.getDate());
        etTime.setText(appointment.getTime());

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        btnSave.setOnClickListener(v -> {
            String newService = etService.getText().toString().trim();
            String newDate = etDate.getText().toString().trim();
            String newTime = etTime.getText().toString().trim();

            if (!newService.isEmpty() && !newTime.isEmpty()) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("appointments").document(appointment.getId())
                        .update("service", newService, "date", newDate, "time", newTime)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Appointment updated", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadAppointments(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), "user"); // Tải lại danh sách
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show());
            }
        });
    }

}
