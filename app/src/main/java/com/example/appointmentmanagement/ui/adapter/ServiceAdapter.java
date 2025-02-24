package com.example.appointmentmanagement.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appointmentmanagement.R;
import com.example.appointmentmanagement.model.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for displaying a list of services in a RecyclerView.
 * This adapter handles displaying service names and provides options for editing and deleting services.
 */
public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {

    private final ArrayList<Service> serviceList; // Use ArrayList instead of List
    private final OnItemClickListener listener; // Listener interface for handling click events

    /**
     * Interface for handling edit and delete actions on a service item.
     */
    public interface OnItemClickListener {
        void onEdit(Service service); // Called when the edit button is clicked
        void onDelete(Service service); // Called when the delete button is clicked
    }

    /**
     * Constructor to initialize the adapter with a list of services and a listener.
     *
     * @param serviceList List of Service objects
     * @param listener    Listener for handling edit and delete actions
     */
    public ServiceAdapter(List<Service> serviceList, OnItemClickListener listener) {
        this.serviceList = new ArrayList<>(serviceList); // Convert List to ArrayList
        this.listener = listener;
    }

    /**
     * ViewHolder class that holds references to the UI elements for each item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            btnEdit = itemView.findViewById(R.id.btnEditService);
            btnDelete = itemView.findViewById(R.id.btnDeleteService);
        }
    }

    /**
     * Inflates the item layout and creates the ViewHolder.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds data to the ViewHolder by setting service details and click listeners.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Service service = serviceList.get(position);
        holder.tvServiceName.setText(service.getName() != null ? service.getName() : "Unnamed Service"); // Avoid null crash

        // Set click listeners for edit and delete buttons
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(service));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(service));
    }

    /**
     * Returns the total number of items in the list.
     */
    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    /**
     * Updates the adapter's data and refreshes the list.
     * @param newList The new list of services to display.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<Service> newList) {
        serviceList.clear();
        serviceList.addAll(newList);
        notifyDataSetChanged();
    }
}
