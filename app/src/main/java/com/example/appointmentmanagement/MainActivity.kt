package com.example.appointmentmanagement

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Gán layout chính

        // Khởi tạo Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Lấy NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Lấy BottomNavigationView
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (auth.currentUser == null) {
            // Chưa đăng nhập → Chuyển đến LoginFragment
            navController.navigate(R.id.loginFragment)
            bottomNav.visibility = View.GONE // Ẩn thanh bottom navigation
        } else {
            // Đã đăng nhập → Hiển thị bottom navigation
            bottomNav.visibility = View.VISIBLE
            NavigationUI.setupWithNavController(bottomNav, navController)
        }

        // Lắng nghe sự thay đổi của Fragment để ẩn/hiện bottom navigation
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.loginFragment) {
                bottomNav.visibility = View.GONE
            } else {
                bottomNav.visibility = View.VISIBLE
            }
        }
    }
}
