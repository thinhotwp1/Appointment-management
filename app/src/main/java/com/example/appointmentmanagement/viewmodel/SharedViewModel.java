package com.example.appointmentmanagement.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<String> userRole = new MutableLiveData<>();

    public void setUserRole(String role) {
        userRole.setValue(role);
    }

    public LiveData<String> getUserRole() {
        return userRole;
    }
}
