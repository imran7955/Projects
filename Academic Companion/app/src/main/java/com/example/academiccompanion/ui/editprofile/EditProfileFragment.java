package com.example.academiccompanion.ui.editprofile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.academiccompanion.R;
import com.google.firebase.database.*;

import java.util.HashMap;

public class EditProfileFragment extends Fragment {

    private EditText etName, etEmail, etPhone, etStudentId, etRegistrationNumber;
    private Spinner spinnerUserType, spinnerDepartment, spinnerYear, spinnerSemester;
    private Button btnDone;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private String selectedUserType = "Student";
    private String selectedDepartment = "CSE";
    private String selectedYear = "1st";
    private String selectedSemester = "1";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            // You may want to redirect to login screen here
            return view;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        // Bind views (with renamed IDs)
        spinnerUserType = view.findViewById(R.id.edit_spinnerUserType);
        spinnerDepartment = view.findViewById(R.id.edit_spinnerDepartment);
        spinnerYear = view.findViewById(R.id.edit_spinnerYear);
        spinnerSemester = view.findViewById(R.id.edit_spinnerSemester);

        etName = view.findViewById(R.id.edit_etName);
        etEmail = view.findViewById(R.id.edit_etEmail);
        etPhone = view.findViewById(R.id.edit_etPhone);
        etStudentId = view.findViewById(R.id.edit_etStudentId);
        etRegistrationNumber = view.findViewById(R.id.edit_etRegistrationNumber);

        btnDone = view.findViewById(R.id.edit_btnDone);

        // Setup spinners with arrays and listeners
        setupSpinner(spinnerUserType, R.array.user_types, selected -> selectedUserType = selected);
        setupSpinner(spinnerDepartment, R.array.departments, selected -> selectedDepartment = selected);
        setupSpinner(spinnerYear, R.array.years, selected -> selectedYear = selected);
        setupSpinner(spinnerSemester, R.array.semesters, selected -> selectedSemester = selected);

        // Load user data from Firebase
        loadUserData();

        btnDone.setOnClickListener(v -> updateUserData());

        return view;
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    etName.setText(snapshot.child("name").getValue(String.class));
                    etEmail.setText(snapshot.child("email").getValue(String.class));
                    etPhone.setText(snapshot.child("phone").getValue(String.class));
                    etStudentId.setText(snapshot.child("studentId").getValue(String.class));
                    etRegistrationNumber.setText(snapshot.child("registrationNumber").getValue(String.class));

                    selectedUserType = snapshot.child("userType").getValue(String.class);
                    selectedDepartment = snapshot.child("department").getValue(String.class);
                    selectedYear = snapshot.child("year").getValue(String.class);
                    selectedSemester = snapshot.child("semester").getValue(String.class);

                    setSpinnerSelection(spinnerUserType, selectedUserType);
                    setSpinnerSelection(spinnerDepartment, selectedDepartment);
                    setSpinnerSelection(spinnerYear, selectedYear);
                    setSpinnerSelection(spinnerSemester, selectedSemester);
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserData() {
        // Validate input if you want (optional)
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(getContext(), "Name, Email and Phone cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("email", email);
        updates.put("phone", phone);
        updates.put("studentId", etStudentId.getText().toString().trim());
        updates.put("registrationNumber", etRegistrationNumber.getText().toString().trim());
        updates.put("userType", selectedUserType);
        updates.put("department", selectedDepartment);
        updates.put("year", selectedYear);
        updates.put("semester", selectedSemester);

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSpinner(Spinner spinner, int arrayResId, OnItemSelected callback) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(), arrayResId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                callback.onItemSelected(parent.getItemAtPosition(position).toString());
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    @SuppressWarnings("unchecked")
    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        int position = adapter.getPosition(value);
        if (position >= 0) {
            spinner.setSelection(position);
        }
    }


    private interface OnItemSelected {
        void onItemSelected(String selected);
    }
}
