package com.example.appointmentmanagement.ui.admin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appointmentmanagement.R
import com.example.appointmentmanagement.model.Service
import com.example.appointmentmanagement.repository.ServiceRepository
import com.example.appointmentmanagement.ui.adapter.ServiceAdapter
import com.google.android.material.button.MaterialButton

class AdminBookingFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ServiceAdapter
    private lateinit var serviceList: MutableList<Service>
    private lateinit var repository: ServiceRepository
    private lateinit var btnAddService: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_booking, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewServices)
        recyclerView.layoutManager = LinearLayoutManager(context)
        btnAddService = view.findViewById<MaterialButton>(R.id.btnAddService)

        repository = ServiceRepository()
        serviceList = mutableListOf()
        adapter = ServiceAdapter(serviceList, object : ServiceAdapter.OnItemClickListener {
            override fun onEdit(service: Service) {
                showEditDialog(service)
            }

            override fun onDelete(service: Service) {
                deleteService(service)
            }
        })

        recyclerView.adapter = adapter
        loadServices()

        btnAddService.setOnClickListener {
            showAddServiceDialog()
        }

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadServices() {
        repository.getAllServices { services ->
            serviceList.clear()
            serviceList.addAll(services)
            adapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showEditDialog(service: Service) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_service, null)
        val etServiceName = dialogView.findViewById<EditText>(R.id.etServiceName)

        etServiceName.setText(service.name)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Service")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = etServiceName.text.toString()
                if (newName.isNotEmpty()) {
                    updateService(service, newName)
                } else {
                    Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateService(service: Service, newName: String) {
        repository.updateService(service.id, newName) {
            Toast.makeText(requireContext(), "Service updated", Toast.LENGTH_SHORT).show()
            loadServices()
        }
    }

    private fun deleteService(service: Service) {
        repository.deleteService(service.id) {
            Toast.makeText(requireContext(), "Service deleted", Toast.LENGTH_SHORT).show()
            loadServices()
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showAddServiceDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_service, null)
        val etServiceName = dialogView.findViewById<EditText>(R.id.etServiceName)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Service")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etServiceName.text.toString()
                if (name.isNotEmpty()) {
                    repository.addService(name) {
                        Toast.makeText(requireContext(), "Service added", Toast.LENGTH_SHORT).show()
                        loadServices()
                    }
                } else {
                    Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
