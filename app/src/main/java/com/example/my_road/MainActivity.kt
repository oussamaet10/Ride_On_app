package com.example.my_road

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize BottomNavigationView
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Set default fragment when the activity starts (HomeFragment)
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment()) // HomeFragment will be shown by default
        }

        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, mapFragment)
            .commit()

        // Set up BottomNavigationView to handle fragment selection
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    replaceFragment(HomeFragment()) // Navigate to HomeFragment
                    true
                }
                R.id.profile -> {
                    replaceFragment(ProfileFragment()) // Navigate to ProfileFragment
                    true
                }
                R.id.settings -> {
                    replaceFragment(SettingsFragment()) // Navigate to SettingsFragment
                    true
                }
                else -> false
            }
        }
    }

    // Helper function to replace fragments
    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null) // Add the transaction to back stack (optional)
        transaction.commit()
    }
}
