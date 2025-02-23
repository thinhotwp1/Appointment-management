package com.example.appointmentmanagement.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appointmentmanagement.R
import com.example.appointmentmanagement.model.Service

class ServiceAdapter(
    private val serviceList: List<Service>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ServiceAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onEdit(service: Service)
        fun onDelete(service: Service)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvServiceName: TextView = view.findViewById(R.id.tvServiceName)
        val btnEdit: Button = view.findViewById(R.id.btnEditService)
        val btnDelete: Button = view.findViewById(R.id.btnDeleteService)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val service = serviceList[position]
        holder.tvServiceName.text = service.name

        holder.btnEdit.setOnClickListener { listener.onEdit(service) }
        holder.btnDelete.setOnClickListener { listener.onDelete(service) }
    }

    override fun getItemCount() = serviceList.size
}
