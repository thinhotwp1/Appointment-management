package com.example.appointmentmanagement.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserViewModel extends ViewModel {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final MutableLiveData<String> _userRole = new MutableLiveData<>();
    public LiveData<String> userRole = _userRole;

    public UserViewModel() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fetchUserRole();
    }

    /**
     * Fetches the user role from Firestore based on the currently authenticated user.
     * If no user is logged in, defaults to "user".
     */
    private void fetchUserRole() {
        String userId = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            _userRole.setValue("user");
            return;
        }

        db.collection("users").document(userId).get()
                .addOnSuccessListener(document -> _userRole.setValue(document.getString("role") != null ? document.getString("role") : "user"))
                .addOnFailureListener(e -> _userRole.setValue("user"));
    }

    /**
     * Refreshes the user role based on the given user ID.
     * If the user ID is null or empty, defaults to "user".
     *
     * @param userId The ID of the user whose role needs to be refreshed.
     */
    public void refreshUserRole(String userId) {
        if (userId == null || userId.isEmpty()) {
            _userRole.setValue("user");
            return;
        }

        db.collection("users").document(userId).get()
                .addOnSuccessListener(document -> _userRole.setValue(document.getString("role") != null ? document.getString("role") : "user"))
                .addOnFailureListener(e -> _userRole.setValue("user"));
    }
}
