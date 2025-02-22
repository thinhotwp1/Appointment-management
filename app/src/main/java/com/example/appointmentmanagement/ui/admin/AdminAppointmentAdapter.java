package com.example.appointmentmanagement.ui.admin;

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
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class AdminAppointmentAdapter extends RecyclerView.Adapter<AdminAppointmentAdapter.ViewHolder> {
    private final List<Appointment> appointmentList;
    private final Context context;

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

        // Nếu đã có userName thì hiển thị, nếu không thì tải từ Firestore
        if (appointment.getUserEmail() != null) {
            holder.tvUserName.setText("User: " + appointment.getUserEmail());
        } else {
            // Lấy thông tin user từ Firestore theo userId
            FirebaseFirestore.getInstance().collection("users")
                    .document(appointment.getUserId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String userName = doc.getString("email"); // Hoặc "name"
                            appointment.setUserEmail(userName);
                            holder.tvUserName.setText("User: " + userName);
                        } else {
                            holder.tvUserName.setText("User: Unknown");
                        }
                    })
                    .addOnFailureListener(e -> holder.tvUserName.setText("User: Error"));
        }

        holder.tvBookingInfo.setText("Service: " + appointment.getService() +
                " - Date: " + appointment.getDate() + " - Time: " + appointment.getTime());

        setStatusIcon(holder, appointment.getStatus());

        holder.btnChangeStatus.setOnClickListener(v -> changeStatus(appointment, holder));
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

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

    @SuppressLint("NotifyDataSetChanged")
    private void changeStatus(Appointment appointment, ViewHolder holder) {
        // Chuyển đổi trạng thái
        String newStatus;
        switch (appointment.getStatus()) {
            case "waiting":
                newStatus = "accept";
                break;
            case "accept":
                newStatus = "success";
                break;
            case "success":
                newStatus = "cancel";
                break;
            default:
                newStatus = "pending";
                break;
        }

        // Cập nhật trạng thái trong Firestore
        FirebaseFirestore.getInstance().collection("appointments")
                .document(appointment.getId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    appointment.setStatus(newStatus);
                    setStatusIcon(holder, newStatus);
                    notifyDataSetChanged();
                });
    }

    private void setStatusIcon(ViewHolder holder, String status) {
        switch (status) {
            case "waiting":
                holder.imgStatus.setImageResource(R.drawable.ic_waiting);
                holder.tvStatus.setText("Waiting");
                break;
            case "accept":
                holder.imgStatus.setImageResource(R.drawable.ic_accept);
                holder.tvStatus.setText("Accepted");
                break;
            case "success":
                holder.imgStatus.setImageResource(R.drawable.ic_success);
                holder.tvStatus.setText("Success");
                break;
            case "cancel":
                holder.imgStatus.setImageResource(R.drawable.ic_cancel);
                holder.tvStatus.setText("Canceled");
                break;
        }
    }
}
