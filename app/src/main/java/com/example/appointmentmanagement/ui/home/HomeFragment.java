package com.example.appointmentmanagement.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * HomeFragment displays a list of appointments for the logged-in user.
 * Admins can see all appointments, while regular users only see their own.
 */
public class HomeFragment extends Fragment {
    private RecyclerView rvAppointments; // RecyclerView to display appointments
    private AppointmentAdapter adapter; // Adapter for managing the appointment list
    private AppointmentRepository repository; // Repository for appointment operations
    private TextView tvWelcome; // TextView to display a welcome message
    private String selectedDate = "", selectedTime = ""; // Stores the selected date and time=
    //    private TextView tvSelectedDateTime; // Displays the selected date and time
    private List<String> services = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the home fragment layout
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize UI elements
        rvAppointments = view.findViewById(R.id.rvAppointments);
        tvWelcome = view.findViewById(R.id.tvWelcome);
        rvAppointments.setLayoutManager(new LinearLayoutManager(getContext()));

        repository = new AppointmentRepository(); // Initialize repository

        // Get the currently logged-in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            fetchUserInfo(user.getUid()); // Load user info and fetch appointments
        } else {
            Toast.makeText(getContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    /**
     * Fetches user information from Firestore and updates UI accordingly.
     *
     * @param userId The ID of the logged-in user.
     */
    @SuppressLint("SetTextI18n")
    private void fetchUserInfo(String userId) {
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Extract user's email (username before '@') and role
                        String userName = Objects.requireNonNull(documentSnapshot.getString("email")).split("@")[0];
                        String role = documentSnapshot.getString("role");

                        // Update welcome message
                        tvWelcome.setText("Welcome, " + (userName != null ? userName : "User") + " !");

                        // Load appointments based on role
                        loadAppointments(userId, role);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch user info", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Loads appointments from Firestore based on user role.
     *
     * @param userId The ID of the logged-in user.
     * @param role   The role of the user (admin or regular user).
     */
    private void loadAppointments(String userId, String role) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference appointmentsRef = db.collection("appointments");

        Query query;
        if ("admin".equals(role)) {
            query = appointmentsRef; // Admins get all appointments
        } else {
            query = appointmentsRef.whereEqualTo("userId", userId); // Regular users get only their own
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Appointment> appointments = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Appointment appointment = doc.toObject(Appointment.class);
                appointment.setId(doc.getId()); // Store Firestore document ID
                appointments.add(appointment);
            }
            // Set up the adapter with appointments and actions
            adapter = new AppointmentAdapter(appointments, getContext(), new AppointmentAdapter.OnAppointmentActionListener() {
                @Override
                public void onEdit(Appointment appointment) {
                    editAppointment(appointment); // Open edit dialog
                }

                @Override
                public void onDelete(Appointment appointment) {
                    deleteAppointment(appointment); // Delete appointment
                }
            });
            rvAppointments.setAdapter(adapter);
        });
    }

    /**
     * Deletes an appointment from Firestore.
     *
     * @param appointment The appointment to be deleted.
     */
    private void deleteAppointment(Appointment appointment) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("appointments").document(appointment.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Appointment deleted", Toast.LENGTH_SHORT).show();
                    // Reload appointments after deletion
                    loadAppointments(FirebaseAuth.getInstance().getCurrentUser().getUid(), "user");
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show());
    }

    /**
     * Opens an edit dialog for an appointment, allowing modifications.
     *
     * @param appointment The appointment to be edited.
     */
    private void editAppointment(Appointment appointment) {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_edit_appointment);

        TextView nameInput = dialog.findViewById(R.id.edit_name);
        Spinner serviceSpinner = dialog.findViewById(R.id.spinner_service);
        Button btnSelectDate = dialog.findViewById(R.id.btn_select_date);
        Button btnSelectTime = dialog.findViewById(R.id.btn_select_time);
        Button btnSave = dialog.findViewById(R.id.btn_save);
        TextView tvSelectedDateTime = dialog.findViewById(R.id.tv_selected_date_time);

        nameInput.setText(appointment.getService());
        tvSelectedDateTime.setText(appointment.getDate() + " at " + appointment.getTime());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, services);
        loadServicesFromFirestore(adapter);
        serviceSpinner.setAdapter(adapter);

        btnSelectDate.setOnClickListener(v -> showDatePicker(tvSelectedDateTime));
        btnSelectTime.setOnClickListener(v -> showTimeSlotPicker(tvSelectedDateTime));

        btnSave.setOnClickListener(v -> {
            appointment.setService(serviceSpinner.getSelectedItem().toString());
            appointment.setDate(selectedDate);
            appointment.setTime(selectedTime);

            // Cập nhật lên Firestore
            FirebaseFirestore.getInstance().collection("appointments").document(appointment.getId())
                    .update("service", appointment.getService(), "date", !Objects.equals(selectedDate, "") ? selectedDate : appointment.getDate(),
                            "time", !Objects.equals(selectedTime, "") ? selectedTime : appointment.getTime())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Updated successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        // Reload appointments after deletion
                        loadAppointments(FirebaseAuth.getInstance().getCurrentUser().getUid(), "user");
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }


    private void loadServicesFromFirestore(ArrayAdapter<String> adapter) {
        FirebaseFirestore.getInstance().collection("services").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    services.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String serviceName = doc.getString("name");
                        if (serviceName != null) {
                            services.add(serviceName);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading services!", Toast.LENGTH_SHORT).show());
    }

    private String loadWorkingHoursFromFile() {
        try {
            FileInputStream fis = getContext().openFileInput("working_hours.txt");
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            return new String(buffer);
        } catch (IOException e) {
            Log.e("HomeFragment", "Error loading working hours", e);
            return "09:00 - 18:00";
        }
    }

    private void showDatePicker(TextView tvSelectedDateTime) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    tvSelectedDateTime.setText(selectedDate + " at " + selectedTime);
                },
                year, month, day
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void generateTimeSlots(List<String> bookedTimes, TextView tvSelectedDateTime) {
        String workingHours = loadWorkingHoursFromFile();
        String[] parts = workingHours.split(" - ");

        if (parts.length < 2) {
            Toast.makeText(getContext(), "Invalid working hours!", Toast.LENGTH_SHORT).show();
            return;
        }

        String startTime = parts[0].trim();
        String endTime = parts[1].trim();

        List<String> availableSlots = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            calendar.setTime(sdf.parse(startTime));

            while (calendar.getTime().before(sdf.parse(endTime))) {
                String slot = sdf.format(calendar.getTime());
                if (!bookedTimes.contains(slot)) {
                    availableSlots.add(slot);
                }
                calendar.add(Calendar.MINUTE, 30);
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "Error parsing time", e);
            return;
        }

        showTimePickerDialog(availableSlots, tvSelectedDateTime);
    }

    private void showTimePickerDialog(List<String> availableTimes, TextView tvSelectedDateTime) {
        if (availableTimes.isEmpty()) {
            Toast.makeText(getContext(), "No available slots on this day!", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select a time slot");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, availableTimes);
        builder.setAdapter(adapter, (dialog, which) -> {
            selectedTime = availableTimes.get(which);
            tvSelectedDateTime.setText(selectedDate + " at " + selectedTime);
        });

        builder.show();
    }

    /**
     * Displays a dialog for the user to select an available time slot.
     */
    @SuppressLint("SetTextI18n")
    private void showTimeSlotPicker(TextView tvSelectedDateTime) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("appointments")
                .whereEqualTo("date", selectedDate)
                .get()
                .addOnSuccessListener(documents -> {
                    List<String> bookedTimes = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : documents) {
                        String time = doc.getString("time");
                        if (time != null) {
                            bookedTimes.add(time);
                        }
                    }
                    generateTimeSlots(bookedTimes, tvSelectedDateTime);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error checking booked times!", Toast.LENGTH_SHORT).show());
    }
}
