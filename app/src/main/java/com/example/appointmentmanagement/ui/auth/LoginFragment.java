package com.example.appointmentmanagement.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.appointmentmanagement.R;
import com.example.appointmentmanagement.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.lifecycle.ViewModelProvider;
import com.example.appointmentmanagement.viewmodel.UserViewModel;

/**
 * LoginFragment handles user authentication (login & registration) using Firebase.
 * It allows users to log in with their credentials and navigate to different sections
 * based on their roles (Admin/User).
 */
public class LoginFragment extends Fragment {
    private FirebaseAuth mAuth; // Firebase Authentication instance
    private EditText etEmail, etPassword; // Input fields for email and password
    private Button btnRegister; // Register button
    private RadioGroup radioGroupRole; // Radio group for selecting role
    private RadioButton radioUser, radioAdmin; // Radio buttons for roles
    private UserViewModel userViewModel; // ViewModel to handle user data

    /**
     * Called when the fragment's view is created.
     * Initializes UI elements and sets up button click listeners.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        // Initialize ViewModel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Find UI elements
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        Button btnLogin = view.findViewById(R.id.btnLogin);
        btnRegister = view.findViewById(R.id.btnRegister);

        radioGroupRole = view.findViewById(R.id.radioGroupRole);
        radioUser = view.findViewById(R.id.radioUser);
        radioAdmin = view.findViewById(R.id.radioAdmin);

        // Set button click listeners
        btnLogin.setOnClickListener(v -> loginUser());
        btnRegister.setOnClickListener(v -> registerUser());

        return view;
    }

    /**
     * Handles user login by authenticating with Firebase.
     * Fetches the user role after successful login and navigates accordingly.
     */
    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            fetchUserRoleAndNavigate(user.getUid());
                        }
                    } else {
                        Toast.makeText(getActivity(), "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Retrieves the user's role from Firestore and navigates to the appropriate screen.
     * @param userId The ID of the logged-in user.
     */
    private void fetchUserRoleAndNavigate(String userId) {
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");

                        // Refresh user role in ViewModel
                        userViewModel.refreshUserRole(userId);

                        // Navigate based on user role
                        if ("admin".equals(role)) {
                            Toast.makeText(getActivity(), "Welcome Admin!", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_adminFragment);
                        } else {
                            Toast.makeText(getActivity(), "Welcome User!", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_homeFragment);
                        }
                    } else {
                        Toast.makeText(getActivity(), "Role not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to fetch user role", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Handles new user registration by creating an account in Firebase Authentication.
     * Stores user details (email, role) in Firestore after successful registration.
     */
    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String role = radioUser.isChecked() ? "user" : "admin";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        if (user != null) {
                            saveUserToFirestore(user.getUid(), email, role);
                        }
                    } else {
                        Toast.makeText(getActivity(), "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Saves user data to Firestore, including role and working hours (for admins).
     * @param userId The unique ID of the registered user.
     * @param email The email of the registered user.
     * @param role The role of the user (admin or user).
     */
    private void saveUserToFirestore(String userId, String email, String role) {
        String workingHours = role.equals("admin") ? "08:00 - 18:00" : "";
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .set(new User(userId, email, role, workingHours, ""))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "User registered successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to save user data", Toast.LENGTH_SHORT).show();
                    System.out.printf("Failed to save user data: %s%n", e.getMessage());
                });
    }
}
