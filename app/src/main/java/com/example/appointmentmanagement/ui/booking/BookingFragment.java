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
    private Spinner spinnerServices;
    private Button btnConfirmBooking, btnPickDateTime;
    private TextView tvSelectedDateTime;
    private AppointmentRepository repository;
    private String selectedDate = "", selectedTime = "";
    private List<String> services;
    private final List<String> allTimeSlots = Arrays.asList(
            "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
            "14:00", "14:30", "15:00", "15:30", "16:00", "16:30"
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        spinnerServices = view.findViewById(R.id.spinnerServices);
        btnConfirmBooking = view.findViewById(R.id.btnConfirmBooking);
        btnPickDateTime = view.findViewById(R.id.btnPickDateTime);
        tvSelectedDateTime = view.findViewById(R.id.tvSelectedDateTime);
        repository = new AppointmentRepository();

        services = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, services);
        spinnerServices.setAdapter(adapter);
        loadServicesFromFirestore(adapter);

        btnPickDateTime.setOnClickListener(v -> showDatePicker());
        btnConfirmBooking.setOnClickListener(v -> saveAppointment());

        return view;
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
            Log.e("BookingFragment", "Error loading working hours", e);
            return "09:00 - 18:00"; // Giá trị mặc định nếu không có file
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    loadAvailableTimeSlots();
                },
                year, month, day
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

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
                    generateTimeSlots(bookedTimes); // Tạo danh sách giờ làm việc thực tế
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error checking booked times!", Toast.LENGTH_SHORT).show());
    }

    private void generateTimeSlots(List<String> bookedTimes) {
        String workingHours = loadWorkingHoursFromFile(); // Đọc từ file hoặc Firestore
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
                calendar.add(Calendar.MINUTE, 30); // Tạo slot mỗi 30 phút
            }
        } catch (Exception e) {
            Log.e("BookingFragment", "Error parsing time", e);
            return;
        }

        showTimeSlotPicker(availableSlots);
    }


    @SuppressLint("SetTextI18n")
    private void showTimeSlotPicker(List<String> bookedTimes) {
        List<String> availableTimes = new ArrayList<>();
        for (String time : allTimeSlots) {
            if (!bookedTimes.contains(time)) {
                availableTimes.add(time);
            }
        }

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
        if (selectedService.isEmpty()) {
            Toast.makeText(getContext(), "Invalid service!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String appointmentId = UUID.randomUUID().toString();

        Appointment appointment = new Appointment(
                appointmentId, userId, selectedService, selectedDate, selectedTime, "waiting"
        );

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
