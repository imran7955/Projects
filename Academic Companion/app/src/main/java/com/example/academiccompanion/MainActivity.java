package com.example.academiccompanion;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem; // Import MenuItem
import android.widget.Toast; // Import Toast for potential error messages

import androidx.annotation.NonNull; // For @NonNull annotation
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.academiccompanion.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser; // Import FirebaseUser
import com.google.firebase.database.DataSnapshot; // Import DataSnapshot
import com.google.firebase.database.DatabaseError; // Import DatabaseError
import com.google.firebase.database.DatabaseReference; // Import DatabaseReference
import com.google.firebase.database.FirebaseDatabase; // Import FirebaseDatabase
import com.google.firebase.database.ValueEventListener; // Import ValueEventListener

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Floating Action Button action (can be modified as needed)
        binding.appBarMain.fab.setOnClickListener(view -> {
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null) // Removed "Go to Login" as it's typically not needed here
                    .setAnchorView(R.id.fab)
                    .show();
        });

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Define top-level destinations
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_view_notice,
                R.id.nav_announce, // Keep this here initially; we'll hide it later
                R.id.nav_edit_profile,
                R.id.nav_progress,
                R.id.nav_logout,
                R.id.nav_gpa_cgpa
        ).setOpenableLayout(drawer).build();

        // Set up NavController
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // --- Start of new logic to hide "Announce" for students ---
        checkUserTypeAndHideMenuItem(navigationView.getMenu());
        // --- End of new logic ---

        // Handle custom menu actions (like logout)
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_logout) {
                // Sign out from Firebase
                mAuth.signOut();

                // Navigate to LoginActivity and clear back stack
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }

            // Handle other navigation via NavigationUI
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            if (handled) {
                drawer.closeDrawer(GravityCompat.START);
            }
            return handled;
        });
    }

    private void checkUserTypeAndHideMenuItem(Menu menu) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String userType = snapshot.child("userType").getValue(String.class);
                        MenuItem announceMenuItem = menu.findItem(R.id.nav_announce);

                        if (announceMenuItem != null) {
                            if ("Student".equals(userType)) {
                                announceMenuItem.setVisible(false);
                            } else {
                                // If the user is not a Student (e.g., CR/Teacher), ensure it's visible
                                announceMenuItem.setVisible(true);
                            }
                        }
                    } else {
                        // User data not found in database, handle as needed (e.g., default to Student or log out)
                        Toast.makeText(MainActivity.this, "User data not found!", Toast.LENGTH_SHORT).show();
                        // Consider signing out if user data is missing for security/consistency
                        // mAuth.signOut();
                        // startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        // finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Failed to load user data: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    // In case of error, you might want to hide it by default or show an error
                    MenuItem announceMenuItem = menu.findItem(R.id.nav_announce);
                    if (announceMenuItem != null) {
                        announceMenuItem.setVisible(false); // Hide by default on error for safety
                    }
                }
            });
        } else {
            // No user logged in, hide "Announce" by default
            MenuItem announceMenuItem = menu.findItem(R.id.nav_announce);
            if (announceMenuItem != null) {
                announceMenuItem.setVisible(false);
            }
            // Optionally, redirect to login if no user is authenticated
            // startActivity(new Intent(MainActivity.this, LoginActivity.class));
            // finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the toolbar menu if needed
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle navigation "Up" actions
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }
}