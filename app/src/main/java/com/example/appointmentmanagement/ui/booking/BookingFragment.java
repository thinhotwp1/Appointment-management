package com.example.appointmentmanagement.ui.booking;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.appointmentmanagement.R;
import com.example.appointmentmanagement.model.Appointment;
import com.example.appointmentmanagement.repository.AppointmentRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class BookingFragment extends Fragment {
    private Spinner spinnerServices; // Dropdown list for selecting services
    private Button btnConfirmBooking, btnPickDateTime; // Buttons for booking confirmation and date selection
    private TextView tvSelectedDateTime; // Displays the selected date and time
    private AppointmentRepository repository; // Repository for managing appointment data
    private String selectedDate = "", selectedTime = ""; // Stores the selected date and time
    private List<String> services; // List of available services

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        // Initialize UI components
        spinnerServices = view.findViewById(R.id.spinnerServices);
        btnConfirmBooking = view.findViewById(R.id.btnConfirmBooking);
        btnPickDateTime = view.findViewById(R.id.btnPickDateTime);
        tvSelectedDateTime = view.findViewById(R.id.tvSelectedDateTime);
        repository = new AppointmentRepository();

        // Initialize service list and set up adapter for the spinner
        services = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, services);
        spinnerServices.setAdapter(adapter);

        // Load available services from Firestore
        loadServicesFromFirestore(adapter);

        // Set up event listeners
        btnPickDateTime.setOnClickListener(v -> showDatePicker());
        btnConfirmBooking.setOnClickListener(v -> saveAppointment());

        return view;
    }

    /**
     * Fetches the list of available services from Firestore and updates the dropdown.
     */
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
                    adapter.notifyDataSetChanged(); // Notify adapter of data change
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading services!", Toast.LENGTH_SHORT).show());
    }

    /**
     * Loads working hours from a local file.
     * If an error occurs, returns default hours (09:00 - 18:00).
     */
    private String loadWorkingHoursFromFile() {
        try {
            FileInputStream fis = getContext().openFileInput("working_hours.txt");
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            return new String(buffer);
        } catch (IOException e) {
            Log.e("BookingFragment", "Error loading working hours", e);
            return "09:00 - 18:00"; // Default working hours
        }
    }

    /**
     * Opens a date picker dialog for the user to select an appointment date.
     */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    loadAvailableTimeSlots(); // Load available time slots for the selected date
                },
                year, month, day
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    /**
     * Fetches already booked time slots from Firestore and updates available slots.
     */
    private void loadAvailableTimeSlots() {
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
                    generateTimeSlots(bookedTimes);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error checking booked times!", Toast.LENGTH_SHORT).show());
    }

    /**
     * Generates available time slots based on working hours and booked times.
     */
    private void generateTimeSlots(List<String> bookedTimes) {
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
            Log.e("BookingFragment", "Error parsing time", e);
            return;
        }

        showTimeSlotPicker(availableSlots);
    }

    /**
     * Displays a dialog for the user to select an available time slot.
     */
    @SuppressLint("SetTextI18n")
    private void showTimeSlotPicker(List<String> availableTimes) {
        if (availableTimes.isEmpty()) {
            Toast.makeText(getContext(), "No available slots on this day!", Toast.LENGTH_SHORT).show();
            return;
        }

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Select a time slot");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, availableTimes);
        builder.setAdapter(adapter, (dialog, which) -> {
            selectedTime = availableTimes.get(which);
            tvSelectedDateTime.setText(selectedDate + " at " + selectedTime);
        });

        builder.show();
    }

    /**
     * Saves the appointment to Firestore after validating input fields.
     */
    private void saveAppointment() {
        if (spinnerServices.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Please select a service!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(getContext(), "Please select a date and time!", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedService = spinnerServices.getSelectedItem().toString().trim();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String appointmentId = UUID.randomUUID().toString();

        Appointment appointment = new Appointment(appointmentId, userId, selectedService, selectedDate, selectedTime, "waiting");

        repository.addAppointment(appointment, new AppointmentRepository.AppointmentCallback() {
            @Override
            public void onSuccess(List<Appointment> appointments) {
                Toast.makeText(getContext(), "Appointment booked successfully!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Failed to book appointment", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
