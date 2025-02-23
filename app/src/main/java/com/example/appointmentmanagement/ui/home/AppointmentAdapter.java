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

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
    private List<Appointment> appointments;
    private Context context;
    private OnAppointmentActionListener listener;

    public interface OnAppointmentActionListener {
        void onEdit(Appointment appointment);

        void onDelete(Appointment appointment);
    }

    public AppointmentAdapter(List<Appointment> appointments, Context context, OnAppointmentActionListener listener) {
        this.appointments = appointments;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.tvAppointmentDetails.setText(appointment.getService() + " - " + appointment.getDate() + " " + appointment.getTime());
        holder.appointmentStatus.setText(appointment.getStatus());

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

        boolean isEditDisabled = appointment.getStatus().equals("accept") ||
                appointment.getStatus().equals("success") ||
                appointment.getStatus().equals("cancel");
        holder.btnEdit.setEnabled(!isEditDisabled);
        holder.btnEdit.setAlpha(isEditDisabled ? 0.5f : 1.0f);

        holder.btnEdit.setOnClickListener(v -> {
            if (!isEditDisabled) listener.onEdit(appointment);
        });

        holder.btnDelete.setOnClickListener(v -> {
            listener.onDelete(appointment);
        });
    }


    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAppointmentDetails, appointmentStatus;
        Button btnEdit, btnDelete;
        ImageView imgStatus;

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
