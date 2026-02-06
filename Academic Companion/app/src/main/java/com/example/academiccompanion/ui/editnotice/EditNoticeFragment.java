package com.example.academiccompanion.ui.editnotice;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.academiccompanion.R;
import com.google.firebase.database.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class EditNoticeFragment extends Fragment {

    EditText etDeadline, etNoticeText;
    Spinner spnNoticeType, spnAnnounceType, spnDepartment, spnYear, spnSemester;
    Button btnEditDone;
    String noticeId;
    DatabaseReference noticeRef, subjectListRef;

    // Variables to hold the currently selected values for dynamic loading
    String selectedDepartment = "CSE"; // Default or initial value
    String selectedYear = "1st";       // Default or initial value
    String selectedSemester = "1";     // Default or initial value

    // Store the original notice data to pre-select spinners after dynamic loading
    private String originalSubject;
    private String originalNoticeType;
    private String originalDepartment;
    private String originalYear;
    private String originalSemester;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_notice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etDeadline = view.findViewById(R.id.deadline);
        etNoticeText = view.findViewById(R.id.noticetext);
        spnNoticeType = view.findViewById(R.id.spnnoticetype);
        spnAnnounceType = view.findViewById(R.id.spinner);
        spnDepartment = view.findViewById(R.id.spnDepartment);
        spnYear = view.findViewById(R.id.spnYear);
        spnSemester = view.findViewById(R.id.spnSemester);
        btnEditDone = view.findViewById(R.id.btnAnnounce);
        btnEditDone.setText("Done Edit");

        noticeRef = FirebaseDatabase.getInstance().getReference("NoticeTable");
        subjectListRef = FirebaseDatabase.getInstance().getReference("SubjectList");

        setupSpinners();

        if (getArguments() != null) {
            noticeId = getArguments().getString("noticeId");
            loadNoticeData(noticeId);
        }

        btnEditDone.setOnClickListener(v -> {
            try {
                if (noticeId == null || noticeId.isEmpty()) {
                    Toast.makeText(getContext(), "Invalid notice ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                String deadline = etDeadline.getText() != null ? etDeadline.getText().toString().trim() : "";
                String text = etNoticeText.getText() != null ? etNoticeText.getText().toString().trim() : "";

                if (TextUtils.isEmpty(deadline) || TextUtils.isEmpty(text)) {
                    Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                String pattern = "^\\d{2}-\\d{2}-\\d{4} (0[1-9]|1[0-2]):[0-5][0-9] (AM|PM)$";
                if (!deadline.matches(pattern)) {
                    Toast.makeText(getContext(),
                            "Invalid deadline format.\nUse dd-MM-yyyy hh:mm AM/PM\n(e.g., 27-01-2001 07:00 AM)",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault());
                sdf.setLenient(false);
                Date parsedDeadlineDate;
                try {
                    parsedDeadlineDate = sdf.parse(deadline);
                } catch (Exception e) {
                    Toast.makeText(getContext(),
                            "Invalid deadline date/time. Please check the values.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                String subject = spnNoticeType.getSelectedItem() != null ? spnNoticeType.getSelectedItem().toString() : "";
                String noticeType = spnAnnounceType.getSelectedItem() != null ? spnAnnounceType.getSelectedItem().toString() : "";
                // Use the selected values from the spinners directly
                String department = selectedDepartment;
                String year = selectedYear;
                String semester = selectedSemester;

                HashMap<String, Object> updates = new HashMap<>();
                updates.put("subject", subject);
                updates.put("noticeType", noticeType);
                updates.put("department", department);
                updates.put("year", year);
                updates.put("semester", semester);
                updates.put("deadline", deadline);
                updates.put("text", text);
                updates.put("deadlineTimestamp", String.valueOf(parsedDeadlineDate.getTime()));

                btnEditDone.setEnabled(false);

                noticeRef.child(noticeId).updateChildren(updates).addOnCompleteListener(task -> {
                    btnEditDone.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Notice updated", Toast.LENGTH_SHORT).show();
                        requireActivity().onBackPressed();
                    } else {
                        Toast.makeText(getContext(), "Update failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception ex) {
                Toast.makeText(getContext(), "Update error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadNoticeData(String noticeId) {
        noticeRef.child(noticeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    originalSubject = snapshot.child("subject").getValue(String.class);
                    originalNoticeType = snapshot.child("noticeType").getValue(String.class);
                    String deadline = snapshot.child("deadline").getValue(String.class);
                    String text = snapshot.child("text").getValue(String.class);
                    originalDepartment = snapshot.child("department").getValue(String.class);
                    originalYear = snapshot.child("year").getValue(String.class);
                    originalSemester = snapshot.child("semester").getValue(String.class);

                    // Set initial spinner values which will trigger loadSubjectsForSelection
                    setSpinnerValue(spnDepartment, originalDepartment);
                    setSpinnerValue(spnYear, originalYear);
                    setSpinnerValue(spnSemester, originalSemester);

                    // Load subjects will be called by the OnItemSelectedListeners.
                    // The subject spinner will be set in loadSubjectsForSelection's onDataChange.

                    etDeadline.setText(deadline);
                    etNoticeText.setText(text);

                    // Set Notice Type after all other dynamic data is set.
                    setSpinnerValue(spnAnnounceType, originalNoticeType);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter != null && value != null) {
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
    }

    private void setupSpinners() {
        // Department spinner setup
        ArrayAdapter<CharSequence> depAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.departments, android.R.layout.simple_spinner_item);
        depAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnDepartment.setAdapter(depAdapter);
        spnDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDepartment = parent.getItemAtPosition(position).toString();
                loadSubjectsForSelection();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Year spinner setup
        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.years, android.R.layout.simple_spinner_item);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnYear.setAdapter(yearAdapter);
        spnYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = parent.getItemAtPosition(position).toString();
                loadSubjectsForSelection();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Semester spinner setup
        ArrayAdapter<CharSequence> semesterAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.semesters, android.R.layout.simple_spinner_item);
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSemester.setAdapter(semesterAdapter);
        spnSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSemester = parent.getItemAtPosition(position).toString();
                loadSubjectsForSelection();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Notice Type spinner setup (this is static, so no need for dynamic loading here)
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.announcenoticetype, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnAnnounceType.setAdapter(typeAdapter);
    }

    private void loadSubjectsForSelection() {
        // Only proceed if all selection variables are initialized
        if (selectedDepartment == null || selectedYear == null || selectedSemester == null) {
            return;
        }

        subjectListRef.child(selectedDepartment)
                .child(selectedYear)
                .child(selectedSemester)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> subjectList = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String subject = child.getValue(String.class);
                            if (subject != null) {
                                subjectList.add(subject);
                            }
                        }

                        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(
                                requireContext(), android.R.layout.simple_spinner_item, subjectList);
                        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spnNoticeType.setAdapter(subjectAdapter);

                        // After subjects are loaded, attempt to set the original subject if it exists
                        if (originalSubject != null && selectedDepartment.equals(originalDepartment) &&
                                selectedYear.equals(originalYear) && selectedSemester.equals(originalSemester)) {
                            setSpinnerValue(spnNoticeType, originalSubject);
                            originalSubject = null; // Consume it so it's not set again unnecessarily
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load subjects: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}