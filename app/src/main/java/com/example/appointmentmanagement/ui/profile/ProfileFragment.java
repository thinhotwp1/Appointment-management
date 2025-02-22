package com.example.appointmentmanagement.ui.profile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.appointmentmanagement.R;
import com.example.appointmentmanagement.model.Appointment;
import com.example.appointmentmanagement.ui.home.AppointmentAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {
    private EditText etUserName, etEmail, etPhone;
    private ImageView ivProfileImage;
    private Button btnUpdate, btnChangeAvatar, btnLogout;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String userId;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

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
        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnLogout = view.findViewById(R.id.btnLogout); // Thêm nút logout

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
        btnLogout.setOnClickListener(v -> logoutUser()); // Xử lý khi nhấn nút đăng xuất

        return view;
    }

    private void loadUserProfile() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etUserName.setText(documentSnapshot.getString("name"));
                        etPhone.setText(documentSnapshot.getString("phone"));
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

        db.collection("users").document(userId)
                .update("name", name, "phone", phone)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Update Failed", Toast.LENGTH_SHORT).show());
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
        Toast.makeText(getContext(), "Here we go again", Toast.LENGTH_SHORT).show();

        // Ẩn BottomNavigationView để tránh lỗi UI
        if (getActivity() != null) {
            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottomNavigationView);
            if (bottomNavigationView != null) {
                bottomNavigationView.setVisibility(View.GONE);
            }
        }

        // Go to login screen
        NavHostFragment.findNavController(ProfileFragment.this)
                .navigate(R.id.action_profileFragment_to_loginFragment);
    }
}
