package com.example.appointmentmanagement

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.example.appointmentmanagement.viewmodel.UserViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNav: BottomNavigationView
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        bottomNav = findViewById(R.id.bottomNavigationView)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNav.visibility = View.GONE // Ẩn tạm thời khi chưa có role

        if (auth.currentUser == null) {
            navController.navigate(R.id.loginFragment)
        } else {
            userViewModel.userRole.observe(this, Observer { role ->
                if (role != null) {
                    val startFragment = if (role == "admin") R.id.adminFragment else R.id.homeFragment
                    if (navController.currentDestination?.id != startFragment) {
                        navController.navigate(startFragment)
                    }
                    bottomNav.visibility = View.VISIBLE
                }
            })
        }

        // Ẩn/hiện BottomNavigationView dựa trên Fragment hiện tại
        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNav.visibility = if (destination.id == R.id.loginFragment) View.GONE else View.VISIBLE
        }

        bottomNav.setOnItemSelectedListener { item ->
            val role = userViewModel.userRole.value ?: return@setOnItemSelectedListener false

            val destination = when (item.itemId) {
                R.id.homeFragment -> if (role == "admin") R.id.adminFragment else R.id.homeFragment
                R.id.bookingFragment -> if (role == "admin") R.id.adminBookingFragment else R.id.bookingFragment
                R.id.profileFragment -> R.id.profileFragment
                else -> return@setOnItemSelectedListener false
            }

            if (navController.currentDestination?.id != destination) {
                navController.navigate(destination)
            }
            true
        }
    }
}
