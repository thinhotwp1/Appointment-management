package com.example.appointmentmanagement.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appointmentmanagement.R;
import com.example.appointmentmanagement.model.Appointment;

import java.util.List;

/**
 * Adapter class for displaying a list of appointments in a RecyclerView.
 * It handles the UI binding and user interactions for each appointment item.
 */
public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
    private List<Appointment> appointments; // List of appointments to be displayed
    private Context context; // Context reference
    private OnAppointmentActionListener listener; // Listener for handling edit and delete actions

    /**
     * Interface for handling edit and delete actions on an appointment item.
     */
    public interface OnAppointmentActionListener {
        void onEdit(Appointment appointment); // Called when the edit button is clicked

        void onDelete(Appointment appointment); // Called when the delete button is clicked
    }

    /**
     * Constructor for the adapter.
     *
     * @param appointments List of appointments
     * @param context      Context reference
     * @param listener     Listener for appointment actions
     */
    public AppointmentAdapter(List<Appointment> appointments, Context context, OnAppointmentActionListener listener) {
        this.appointments = appointments;
        this.context = context;
        this.listener = listener;
    }

    /**
     * Creates and returns a new ViewHolder for an appointment item.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the appointment data to the ViewHolder.
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.tvAppointmentDetails.setText(appointment.getService() + " - " + appointment.getDate() + " " + appointment.getTime());
        holder.appointmentStatus.setText(appointment.getStatus());

        // Set status icon and text based on appointment status
        switch (appointment.getStatus()) {
            case "waiting":
            default:
                holder.imgStatus.setImageResource(R.drawable.ic_waiting);
                holder.appointmentStatus.setText("Waiting");
                break;
            case "accept":
                holder.imgStatus.setImageResource(R.drawable.ic_accept);
                holder.appointmentStatus.setText("Accepted");
                break;
            case "success":
                holder.imgStatus.setImageResource(R.drawable.ic_success);
                holder.appointmentStatus.setText("Met");
                break;
            case "cancel":
                holder.imgStatus.setImageResource(R.drawable.ic_cancel);
                holder.appointmentStatus.setText("Canceled");
                break;
        }

        // Disable edit button if the appointment is already accepted, met, or canceled
        boolean isEditDisabled = appointment.getStatus().equals("accept") ||
                appointment.getStatus().equals("success") ||
                appointment.getStatus().equals("cancel");
        holder.btnEdit.setEnabled(!isEditDisabled);
        holder.btnEdit.setAlpha(isEditDisabled ? 0.5f : 1.0f);

        // Handle edit button click
        holder.btnEdit.setOnClickListener(v -> {
            if (!isEditDisabled) listener.onEdit(appointment);
        });

        // Handle delete button click
        holder.btnDelete.setOnClickListener(v -> {
            listener.onDelete(appointment);
        });
    }

    /**
     * Returns the total number of appointment items.
     */
    @Override
    public int getItemCount() {
        return appointments.size();
    }

    /**
     * ViewHolder class that holds the UI components for an appointment item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAppointmentDetails, appointmentStatus;
        Button btnEdit, btnDelete;
        ImageView imgStatus;

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView The view representing an appointment item
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAppointmentDetails = itemView.findViewById(R.id.tvAppointmentDetails);
            appointmentStatus = itemView.findViewById(R.id.appointment_status);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            imgStatus = itemView.findViewById(R.id.imgStatus);
        }
    }
}
