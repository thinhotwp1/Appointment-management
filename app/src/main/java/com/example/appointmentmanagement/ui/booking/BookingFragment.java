package com.example.appointmentmanagement.ui.booking;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        spinnerServices = view.findViewById(R.id.spinnerServices);
        btnConfirmBooking = view.findViewById(R.id.btnConfirmBooking);
        btnPickDateTime = view.findViewById(R.id.btnPickDateTime);
        tvSelectedDateTime = view.findViewById(R.id.tvSelectedDateTime);
        repository = new AppointmentRepository();

        // Tạo danh sách dịch vụ
        List<String> services = Arrays.asList("Nail Design", "Manicure", "Pedicure", "Acrylic Nails", "Gel Polish");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, services);
        spinnerServices.setAdapter(adapter);

        btnPickDateTime.setOnClickListener(v -> showDatePicker());

        btnConfirmBooking.setOnClickListener(v -> saveAppointment());

        return view;
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
                    showTimePicker(); // Sau khi chọn ngày xong, mở chọn giờ
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, selectedHour, selectedMinute) -> {
                    selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    tvSelectedDateTime.setText(selectedDate + " at " + selectedTime);
                },
                hour, minute, true
        );
        timePickerDialog.show();
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

        // Tạo object Appointment
        Appointment appointment = new Appointment(
                appointmentId, userId, selectedService, selectedDate, selectedTime, "waiting"
        );

        // Lưu vào Firestore
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
