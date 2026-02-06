package com.example.academiccompanion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    EditText etName, etEmail, etPhone, etStudentId, etRegistrationNumber, etPassword;
    Spinner spinnerUserType, spinnerDepartment, spinnerYear, spinnerSemester;
    Button btnRegister;
    TextView tvAlreadyHaveAccount;

    FirebaseAuth mAuth;
    DatabaseReference databaseRef;

    String selectedUserType = "Student";
    String selectedDepartment = "CSE";
    String selectedYear = "1st";
    String selectedSemester = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        // Bind UI components
        spinnerUserType = findViewById(R.id.spinnerUserType);
        spinnerDepartment = findViewById(R.id.spinnerDepartment);
        spinnerYear = findViewById(R.id.spinnerYear);
        spinnerSemester = findViewById(R.id.spinnerSemester);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etStudentId = findViewById(R.id.etStudentId);
        etRegistrationNumber = findViewById(R.id.etRegistrationNumber);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvAlreadyHaveAccount = findViewById(R.id.tvAlreadyHaveAccount);

        // Set up all spinners
        setupSpinner(spinnerUserType, R.array.user_types, selected -> selectedUserType = selected);
        setupSpinner(spinnerDepartment, R.array.departments, selected -> selectedDepartment = selected);
        setupSpinner(spinnerYear, R.array.years, selected -> selectedYear = selected);
        setupSpinner(spinnerSemester, R.array.semesters, selected -> selectedSemester = selected);

        // Handle register button click
        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String studentId = etStudentId.getText().toString().trim();
            String regNo = etRegistrationNumber.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(name, email, phone, studentId, regNo, selectedDepartment, selectedYear, selectedSemester, password);
        });

        // Navigate to login
        tvAlreadyHaveAccount.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
    }

    // Utility method to setup spinner with listener
    private void setupSpinner(Spinner spinner, int arrayResId, OnItemSelectedListener listener) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, arrayResId, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                listener.onItemSelected(parent.getItemAtPosition(position).toString());
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // Interface for spinner selection
    interface OnItemSelectedListener {
        void onItemSelected(String selected);
    }

    // Register user with Firebase Authentication and Realtime Database
    private void registerUser(String name, String email, String phone, String studentId,
                              String regNo, String department, String year,
                              String semester, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();

                            HashMap<String, String> userData = new HashMap<>();
                            userData.put("userType", selectedUserType);
                            userData.put("name", name);
                            userData.put("email", email);
                            userData.put("phone", phone);
                            userData.put("studentId", studentId);
                            userData.put("registrationNumber", regNo);
                            userData.put("department", department);
                            userData.put("year", year);
                            userData.put("semester", semester);

                            databaseRef.child(userId).setValue(userData)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(SignUpActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(SignUpActivity.this, "Failed to save user data: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}