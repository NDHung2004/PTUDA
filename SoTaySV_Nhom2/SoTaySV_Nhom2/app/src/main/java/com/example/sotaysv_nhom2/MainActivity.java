package com.example.sotaysv_nhom2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.sotaysv_nhom2.Fragment.HomeFragment;
import com.example.sotaysv_nhom2.Fragment.NotesFragment;
import com.example.sotaysv_nhom2.Fragment.SubjectListFragment; // Import Mới
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private int currentTabPosition = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        askNotificationPermission();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) { loadFragment(new HomeFragment(), 1); }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_grades) {
                loadFragment(new HomeFragment(), 1);
                return true;
            }
            else if (id == R.id.nav_notes) {
                loadFragment(new NotesFragment(), 3);
                return true;
            }
            else if (id == R.id.nav_subjects) {
                loadFragment(new SubjectListFragment(), 2);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment, int newTabPosition) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (newTabPosition > currentTabPosition) { transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left); }
        else if (newTabPosition < currentTabPosition) { transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right); }
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
        currentTabPosition = newTabPosition;
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }
}