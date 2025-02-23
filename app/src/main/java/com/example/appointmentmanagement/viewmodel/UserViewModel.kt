package com.example.appointmentmanagement.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _userRole = MutableLiveData<String>()
    val userRole: LiveData<String> get() = _userRole

    init {
        fetchUserRole()
    }

    private fun fetchUserRole() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _userRole.value = "user"
            return
        }

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                _userRole.value = document.getString("role") ?: "user"
            }
            .addOnFailureListener {
                _userRole.value = "user"
            }
    }

    fun refreshUserRole(userId: String?) {
        if (userId.isNullOrEmpty()) {
            _userRole.value = "user"
            return
        }

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                _userRole.value = document.getString("role") ?: "user"
            }
            .addOnFailureListener {
                _userRole.value = "user"
            }
    }
}
