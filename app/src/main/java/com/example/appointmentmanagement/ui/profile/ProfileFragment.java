package com.example.appointmentmanagement.ui.profile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.appointmentmanagement.MainActivity;
import com.example.appointmentmanagement.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {
    private EditText etUserName, etEmail, etPhone, etWorkingHours;
    private ImageView ivProfileImage;
    private Button btnUpdate, btnChangeAvatar, btnLogout;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String userId;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private static final String WORKING_HOURS_FILE = "working_hours.txt";
    private String userRole;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);
        etUserName = view.findViewById(R.id.etUserName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etWorkingHours = view.findViewById(R.id.etWorkingHours);
        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnLogout = view.findViewById(R.id.btnLogout);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            userId = user.getUid();
            etEmail.setText(user.getEmail());
            loadUserProfile();
        }

        btnUpdate.setOnClickListener(v -> updateUserInfo());
        btnChangeAvatar.setOnClickListener(v -> selectImage());
        btnLogout.setOnClickListener(v -> logoutUser());

        return view;
    }

    private String loadWorkingHoursFromFile() {
        try {
            FileInputStream fis = getContext().openFileInput(WORKING_HOURS_FILE);
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            return new String(buffer);
        } catch (IOException e) {
            Log.e("ProfileFragment", "Error loading working hours", e);
            return "09:00 - 18:00";
        }
    }

    private void loadUserProfile() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etUserName.setText(documentSnapshot.getString("name"));
                        etPhone.setText(documentSnapshot.getString("phone"));
                        userRole = documentSnapshot.getString("role");

                        if ("admin".equals(userRole)) {
                            etWorkingHours.setVisibility(View.VISIBLE);
                            String workingHours = documentSnapshot.getString("workingHours");
                            etWorkingHours.setText(workingHours);
                            saveWorkingHoursToFile(workingHours);
                        } else {
                            etWorkingHours.setVisibility(View.GONE);
                        }

                        String imageUrl = documentSnapshot.getString("avatar");
                        if (imageUrl != null) {
                            Glide.with(this).load(imageUrl).into(ivProfileImage);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show());
    }

    private void updateUserInfo() {
        String name = etUserName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String workingHours = etWorkingHours.getText().toString().trim();

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", name);
        updateData.put("phone", phone);

        if ("admin".equals(userRole)) {
            updateData.put("workingHours", workingHours);
            saveWorkingHoursToFile(workingHours);
        }

        db.collection("users").document(userId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Update Failed", Toast.LENGTH_SHORT).show());
    }

    private void saveWorkingHoursToFile(String workingHours) {
        try {
            FileOutputStream fos = getContext().openFileOutput(WORKING_HOURS_FILE, Context.MODE_PRIVATE);
            fos.write(workingHours.getBytes());
            fos.close();
        } catch (IOException e) {
            Log.e("ProfileFragment", "Error saving working hours", e);
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            ivProfileImage.setImageURI(imageUri);
            uploadImage();
        }
    }

    private void uploadImage() {
        if (imageUri != null) {
            StorageReference fileRef = storage.getReference().child("avatars/" + userId + ".jpg");
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        db.collection("users").document(userId).update("avatar", uri.toString());
                        Glide.with(this).load(uri).into(ivProfileImage);
                        Toast.makeText(getContext(), "Avatar Updated", Toast.LENGTH_SHORT).show();
                    }));
        }
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        SharedPreferences preferences = getActivity().getSharedPreferences("your_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        clearCache(getContext());

        if (getActivity() != null) {
            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
            if (bottomNavigationView != null) {
                bottomNavigationView.setVisibility(View.GONE);
            }
        }

        NavController navController = NavHostFragment.findNavController(ProfileFragment.this);
        navController.navigate(R.id.loginFragment, null, new NavOptions.Builder()
                .setPopUpTo(R.id.bottomNavigationView, true)  // Xóa toàn bộ stack navigation
                .setLaunchSingleTop(true)
                .build());

        restartApp();
    }
    private void clearCache(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            deleteDir(cacheDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        }
        return false;
    }
    private void restartApp() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finishAffinity();
    }


}
