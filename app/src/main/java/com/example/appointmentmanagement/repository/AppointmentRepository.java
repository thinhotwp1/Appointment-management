package com.example.appointmentmanagement.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.appointmentmanagement.model.Appointment;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for managing appointment data in Firestore.
 */
public class AppointmentRepository {
    private static final String TAG = "AppointmentRepository";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Callback interface for handling asynchronous Firestore operations.
     */
    public interface AppointmentCallback {
        void onSuccess(List<Appointment> appointments); // Called when data retrieval is successful
        void onFailure(Exception e); // Called when an error occurs
    }

    /**
     * Retrieves appointments for a specific user from Firestore.
     *
     * @param userId   The ID of the user whose appointments are to be fetched.
     * @param callback The callback to handle success or failure.
     */
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
                    callback.onSuccess(appointmentList); // Return the list of appointments
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching appointments", e);
                    callback.onFailure(e); // Handle the error
                });
    }

    /**
     * Adds a new appointment to Firestore.
     *
     * @param appointment The appointment object to be added.
     * @param callback    The callback to handle success or failure.
     */
    public void addAppointment(Appointment appointment, AppointmentCallback callback) {
        db.collection("appointments")
                .document(appointment.getId()) // Use the appointment ID as the document key
                .set(appointment)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Appointment added successfully!");
                    callback.onSuccess(null); // Indicate success
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add appointment", e);
                    callback.onFailure(e); // Handle the error
                });
    }
}
