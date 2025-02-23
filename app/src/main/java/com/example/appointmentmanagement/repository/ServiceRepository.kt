package com.example.appointmentmanagement.repository

import com.example.appointmentmanagement.model.Service
import com.google.firebase.firestore.FirebaseFirestore

class ServiceRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getAllServices(callback: (List<Service>) -> Unit) {
        db.collection("services").get().addOnSuccessListener { result ->
            val services = result.map { doc -> Service(doc.id, doc.getString("name") ?: "") }
            callback(services)
        }
    }

    fun addService(name: String, callback: () -> Unit) {
        val service = hashMapOf("name" to name)
        db.collection("services").add(service).addOnSuccessListener { callback() }
    }

    fun updateService(id: String, newName: String, callback: () -> Unit) {
        db.collection("services").document(id).update("name", newName).addOnSuccessListener { callback() }
    }

    fun deleteService(id: String, callback: () -> Unit) {
        db.collection("services").document(id).delete().addOnSuccessListener { callback() }
    }
}
