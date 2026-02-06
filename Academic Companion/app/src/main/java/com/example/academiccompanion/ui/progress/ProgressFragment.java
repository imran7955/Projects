package com.example.academiccompanion.ui.progress;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.academiccompanion.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class ProgressFragment extends Fragment {

    private EditText ct1, ct2, ct3, ct4, totalClass, presentClass, obtainedMark;
    private TextView sumBest3, percentage;
    private Button btnDoneEdit;
    private Spinner subjectSpinner;
    private TextView[] gradeMarks;

    private DatabaseReference usersRef, progressRef;
    private String studentId, department, year, semester, selectedSubject;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        // Initialize Views
        ct1 = view.findViewById(R.id.ct1);
        ct2 = view.findViewById(R.id.ct2);
        ct3 = view.findViewById(R.id.ct3);
        ct4 = view.findViewById(R.id.ct4);
        sumBest3 = view.findViewById(R.id.sumBest3);

        totalClass = view.findViewById(R.id.totalClass);
        presentClass = view.findViewById(R.id.presentClass);
        obtainedMark = view.findViewById(R.id.obtainedMark);
        percentage = view.findViewById(R.id.percentage);

        btnDoneEdit = view.findViewById(R.id.btnDoneEdit);
        subjectSpinner = view.findViewById(R.id.subjectSpinner);

        gradeMarks = new TextView[]{
                view.findViewById(R.id.markAPlus), view.findViewById(R.id.markA), view.findViewById(R.id.markAMinus),
                view.findViewById(R.id.markBPlus), view.findViewById(R.id.markB), view.findViewById(R.id.markBMinus),
                view.findViewById(R.id.markCPlus), view.findViewById(R.id.markC), view.findViewById(R.id.markD),
                view.findViewById(R.id.markF)
        };

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        progressRef = FirebaseDatabase.getInstance().getReference("progress");

        getUserInfo(); // Load user data → then load subject list

        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSubject = parent.getItemAtPosition(position).toString();
                if (department != null && year != null && studentId != null) {
                    loadDataFromFirebase();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnDoneEdit.setOnClickListener(v -> {
            calculateBest3Sum();
            calculateAttendancePercentage();
            calculateObtainedMark();
            updateRequiredMarksTable(); // This is where the formatting will happen
            saveDataToFirebase();
        });

        return view;
    }

    private void getUserInfo() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentId = snapshot.child("studentId").getValue(String.class);
                department = snapshot.child("department").getValue(String.class);
                year = snapshot.child("year").getValue(String.class);
                semester = snapshot.child("semester").getValue(String.class);

                if (department != null && year != null && semester != null) {
                    loadSubjectsFromFirebase();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadSubjectsFromFirebase() {
        DatabaseReference subjectRef = FirebaseDatabase.getInstance()
                .getReference("SubjectList")
                .child(department)
                .child(year)
                .child(semester);

        subjectRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> subjectList = new ArrayList<>();
                for (DataSnapshot subjectSnap : snapshot.getChildren()) {
                    String subject = subjectSnap.getValue(String.class);
                    if (subject != null) {
                        subjectList.add(subject);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        subjectList
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                subjectSpinner.setAdapter(adapter);

                if (!subjectList.isEmpty()) {
                    selectedSubject = subjectList.get(0);
                    loadDataFromFirebase();
                } else {
                    Toast.makeText(getContext(), "No subjects found for this semester.", Toast.LENGTH_SHORT).show();
                    // Clear fields if no subjects
                    clearAllFields();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load subjects: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDataFromFirebase() {
        DatabaseReference ref = progressRef.child(department).child(year).child(studentId).child(selectedSubject);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Safely retrieve data, providing empty string if not found
                ct1.setText(snapshot.child("ct1").getValue(String.class));
                ct2.setText(snapshot.child("ct2").getValue(String.class));
                ct3.setText(snapshot.child("ct3").getValue(String.class));
                ct4.setText(snapshot.child("ct4").getValue(String.class));
                totalClass.setText(snapshot.child("totalClass").getValue(String.class));
                presentClass.setText(snapshot.child("presentClass").getValue(String.class));
                // obtainedMark should be calculated, not loaded directly
                // obtainedMark.setText(snapshot.child("obtainedMark").getValue(String.class)); // Removed this line

                // Re-calculate all derived values after loading base data
                calculateBest3Sum();
                calculateAttendancePercentage();
                calculateObtainedMark(); // This will set the obtainedMark EditText
                updateRequiredMarksTable();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load progress data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                clearAllFields(); // Clear fields if data loading fails
            }
        });
    }

    private void saveDataToFirebase() {
        if (department == null || year == null || studentId == null || selectedSubject == null) {
            Toast.makeText(getContext(), "User or subject information missing. Cannot save.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ref = progressRef.child(department).child(year).child(studentId).child(selectedSubject);

        // Ensure default values if EditTexts are empty
        String ct1Val = ct1.getText().toString().trim().isEmpty() ? "0" : ct1.getText().toString().trim();
        String ct2Val = ct2.getText().toString().trim().isEmpty() ? "0" : ct2.getText().toString().trim();
        String ct3Val = ct3.getText().toString().trim().isEmpty() ? "0" : ct3.getText().toString().trim();
        String ct4Val = ct4.getText().toString().trim().isEmpty() ? "0" : ct4.getText().toString().trim();
        String totalClassVal = totalClass.getText().toString().trim().isEmpty() ? "0" : totalClass.getText().toString().trim();
        String presentClassVal = presentClass.getText().toString().trim().isEmpty() ? "0" : presentClass.getText().toString().trim();
        // obtainedMark is calculated, so it should always have a value after calculateObtainedMark()
        String obtainedMarkVal = obtainedMark.getText().toString().trim().isEmpty() ? "0" : obtainedMark.getText().toString().trim();


        ref.child("ct1").setValue(ct1Val);
        ref.child("ct2").setValue(ct2Val);
        ref.child("ct3").setValue(ct3Val);
        ref.child("ct4").setValue(ct4Val);
        ref.child("totalClass").setValue(totalClassVal);
        ref.child("presentClass").setValue(presentClassVal);
        ref.child("obtainedMark").setValue(obtainedMarkVal); // Save the calculated obtained mark

        Toast.makeText(getContext(), "Progress updated", Toast.LENGTH_SHORT).show();
    }

    private void calculateBest3Sum() {
        List<Integer> marks = new ArrayList<>();
        // Using a loop to add marks safely
        for (EditText et : new EditText[]{ct1, ct2, ct3, ct4}) {
            try {
                String val = et.getText().toString().trim();
                if (!val.isEmpty()) {
                    marks.add(Integer.parseInt(val));
                }
            } catch (NumberFormatException e) {
                // Ignore non-numeric inputs for calculation
            }
        }
        Collections.sort(marks, Collections.reverseOrder());
        int sum = 0;
        for (int i = 0; i < Math.min(3, marks.size()); i++) {
            sum += marks.get(i);
        }
        sumBest3.setText(String.valueOf(sum));
    }

    private void calculateAttendancePercentage() {
        try {
            int total = Integer.parseInt(totalClass.getText().toString().trim());
            int present = Integer.parseInt(presentClass.getText().toString().trim());
            if (total > 0) {
                float percent = (present * 100.0f) / total;
                percentage.setText(String.format(Locale.getDefault(), "%d%%", (int) percent)); // Format as integer percentage
            } else {
                percentage.setText("0%");
            }
        } catch (NumberFormatException e) {
            percentage.setText("0%");
        }
    }

    private void calculateObtainedMark() {
        String percentStr = percentage.getText().toString().replace("%", "").trim();
        if (!percentStr.isEmpty()) {
            try {
                int percent = Integer.parseInt(percentStr);
                int mark = getObtainedMarkFromPercentage(percent);
                obtainedMark.setText(String.valueOf(mark));
            } catch (NumberFormatException e) {
                obtainedMark.setText("0"); // Set to 0 if parsing fails
            }
        } else {
            obtainedMark.setText("0"); // Set to 0 if percentage string is empty
        }
    }

    private int getObtainedMarkFromPercentage(int percent) {
        if (percent >= 90) return 30;
        else if (percent >= 85) return 28;
        else if (percent >= 80) return 25;
        else if (percent >= 75) return 22;
        else if (percent >= 70) return 20;
        else return 0;
    }

    private void updateRequiredMarksTable() {
        int sum3 = 0, attendanceMark = 0;
        try {
            sum3 = Integer.parseInt(sumBest3.getText().toString().trim());
            attendanceMark = Integer.parseInt(obtainedMark.getText().toString().trim());
        } catch (NumberFormatException ignored) {
            // If parsing fails, sum3 or attendanceMark remains 0
        }

        double[] expectedPercentages = {0.8, 0.75, 0.7, 0.65, 0.6, 0.55, 0.5, 0.45, 0.4, 0.0};
        final int TOTAL_MARK_FOR_REQUIRED = 210; // Assuming the remaining mark is out of 210

        for (int i = 0; i < expectedPercentages.length; i++) {
            // Calculate total marks needed for target percentage out of 300
            int totalTargetMark = (int) (300 * expectedPercentages[i]);

            // Calculate remaining required marks from the 210 component
            int required = totalTargetMark - (sum3 + attendanceMark);
            required = Math.max(required, 0); // Ensure required mark is not negative

            // Calculate percentage out of TOTAL_MARK_FOR_REQUIRED (210)
            double percentageOfRequired = 0.0;
            if (TOTAL_MARK_FOR_REQUIRED > 0) {
                percentageOfRequired = (double) required * 100.0 / TOTAL_MARK_FOR_REQUIRED;
            }

            // Format the string: "RequiredMark (Percentage%)"
            String formattedText = String.format(Locale.getDefault(), "%d (%.2f%%)", required, percentageOfRequired);
            gradeMarks[i].setText(formattedText);
        }
    }

    // Helper method to clear all input fields
    private void clearAllFields() {
        ct1.setText("");
        ct2.setText("");
        ct3.setText("");
        ct4.setText("");
        totalClass.setText("");
        presentClass.setText("");
        obtainedMark.setText("0"); // Reset obtained mark
        sumBest3.setText("0"); // Reset best 3 sum
        percentage.setText("0%"); // Reset attendance percentage

        // Clear grade marks table
        for (TextView tv : gradeMarks) {
            tv.setText("0 (0.00%)"); // Reset to default format
        }
    }
}