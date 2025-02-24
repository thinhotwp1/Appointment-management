package com.example.appointmentmanagement.ui.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appointmentmanagement.R;
import com.example.appointmentmanagement.model.Appointment;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

/**
 * RecyclerView Adapter for displaying and managing appointments in the admin panel.
 */
public class AdminAppointmentAdapter extends RecyclerView.Adapter<AdminAppointmentAdapter.ViewHolder> {
    private final List<Appointment> appointmentList;
    private final Context context;

    /**
     * Constructor for initializing the adapter with appointment data.
     *
     * @param appointmentList List of appointments to display.
     * @param context         The application context.
     */
    public AdminAppointmentAdapter(List<Appointment> appointmentList, Context context) {
        this.appointmentList = appointmentList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_appointment, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);

        // Set user email if available; otherwise, fetch from Firestore
        if (appointment.getUserEmail() != null) {
            holder.tvUserName.setText("User: " + appointment.getUserEmail());
        } else {
            FirebaseFirestore.getInstance().collection("users")
                    .document(appointment.getUserId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String userName = doc.getString("email"); // Fetch user email
                            appointment.setUserEmail(userName);
                            holder.tvUserName.setText("User: " + userName);
                        } else {
                            holder.tvUserName.setText("User: Unknown");
                        }
                    })
                    .addOnFailureListener(e -> holder.tvUserName.setText("User: Error"));
        }

        // Display appointment details: service, date, and time
        holder.tvBookingInfo.setText("Service: " + appointment.getService() +
                " - Date: " + appointment.getDate() + " - Time: " + appointment.getTime());

        // Set status icon and text based on appointment status
        setStatusIcon(holder, appointment.getStatus());

        // Handle status change button click
        holder.btnChangeStatus.setOnClickListener(v -> changeStatus(appointment, holder));
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    /**
     * ViewHolder class to hold UI components for each appointment item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvBookingInfo, tvStatus;
        ImageView imgStatus;
        Button btnChangeStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvBookingInfo = itemView.findViewById(R.id.tvBookingInfo);
            imgStatus = itemView.findViewById(R.id.imgStatus);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnChangeStatus = itemView.findViewById(R.id.btnChangeStatus);
        }
    }

    /**
     * Displays a dialog allowing the admin to change the appointment status.
     *
     * @param appointment The appointment whose status needs to be updated.
     * @param holder      The ViewHolder of the selected item.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void changeStatus(Appointment appointment, ViewHolder holder) {
        String[] statuses = {"waiting", "accept", "success", "cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
        builder.setTitle("Choose status")
                .setItems(statuses, (dialog, which) -> {
                    String newStatus = statuses[which];

                    // Update status in Firestore
                    FirebaseFirestore.getInstance().collection("appointments")
                            .document(appointment.getId())
                            .update("status", newStatus)
                            .addOnSuccessListener(aVoid -> {
                                appointment.setStatus(newStatus);
                                setStatusIcon(holder, newStatus);
                                notifyDataSetChanged(); // Refresh RecyclerView
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(holder.itemView.getContext(), "Error!", Toast.LENGTH_SHORT).show()
                            );
                });

        builder.create().show();
    }

    /**
     * Sets the status icon and text based on the appointment status.
     *
     * @param holder The ViewHolder for the appointment item.
     * @param status The status of the appointment.
     */
    private void setStatusIcon(ViewHolder holder, String status) {
        switch (status) {
            case "waiting":
            default:
                holder.imgStatus.setImageResource(R.drawable.ic_waiting);
                holder.tvStatus.setText("Waiting");
                break;
            case "accept":
                holder.imgStatus.setImageResource(R.drawable.ic_accept);
                holder.tvStatus.setText("Accepted");
                break;
            case "success":
                holder.imgStatus.setImageResource(R.drawable.ic_success);
                holder.tvStatus.setText("Met");
                break;
            case "cancel":
                holder.imgStatus.setImageResource(R.drawable.ic_cancel);
                holder.tvStatus.setText("Canceled");
                break;
        }
    }
}
