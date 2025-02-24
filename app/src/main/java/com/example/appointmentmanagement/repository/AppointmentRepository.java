package com.example.appointmentmanagement.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.appointmentmanagement.model.Appointment;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentRepository {
    private static final String TAG = "AppointmentRepository";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface AppointmentCallback {
        void onSuccess(List<Appointment> appointments);
        void onFailure(Exception e);
    }

    public void getAppointments(String userId, AppointmentCallback callback) {
        db.collection("appointments")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Appointment> appointmentList = new ArrayList<>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Appointment appointment = doc.toObject(Appointment.class);
                    appointmentList.add(appointment);
                }
                callback.onSuccess(appointmentList);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error fetching appointments", e);
                callback.onFailure(e);
            });
    }

    public void addAppointment(Appointment appointment, AppointmentCallback callback) {
        db.collection("appointments")
            .document(appointment.getId())
            .set(appointment)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Appointment added successfully!");
                callback.onSuccess(null);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to add appointment", e);
                callback.onFailure(e);
            });
    }
}
