package com.example.appointmentmanagement;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.example.appointmentmanagement.viewmodel.UserViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private BottomNavigationView bottomNav;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase authentication
        auth = FirebaseAuth.getInstance();
        bottomNav = findViewById(R.id.bottomNavigationView);
        userViewModel = new UserViewModel();

        // Set up navigation host fragment and controller
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        // Initially hide bottom navigation
        bottomNav.setVisibility(View.GONE);

        // Check if user is logged in
        if (auth.getCurrentUser() == null) {
            navController.navigate(R.id.loginFragment);
        } else {
            // Observe user role from ViewModel and navigate accordingly
            userViewModel.userRole.observe(this, new Observer<String>() {
                @Override
                public void onChanged(String role) {
                    if (role != null) {
                        int startFragment = role.equals("admin") ? R.id.adminFragment : R.id.homeFragment;
                        if (navController.getCurrentDestination() == null || navController.getCurrentDestination().getId() != startFragment) {
                            navController.navigate(startFragment);
                        }
                        bottomNav.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        // Hide bottom navigation when on login screen
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            bottomNav.setVisibility(destination.getId() == R.id.loginFragment ? View.GONE : View.VISIBLE);
        });

        // Handle bottom navigation item selection
        bottomNav.setOnItemSelectedListener(item -> {
            String role = userViewModel.userRole.getValue();
            if (role == null) return false;

            int destination;
            if (item.getItemId() == R.id.homeFragment) {
                destination = role.equals("admin") ? R.id.adminFragment : R.id.homeFragment;
            } else if (item.getItemId() == R.id.bookingFragment) {
                destination = role.equals("admin") ? R.id.adminBookingFragment : R.id.bookingFragment;
            } else if (item.getItemId() == R.id.profileFragment) {
                destination = R.id.profileFragment;
            } else {
                return false;
            }

            if (navController.getCurrentDestination() == null || navController.getCurrentDestination().getId() != destination) {
                navController.navigate(destination);
            }
            return true;
        });
    }
}
