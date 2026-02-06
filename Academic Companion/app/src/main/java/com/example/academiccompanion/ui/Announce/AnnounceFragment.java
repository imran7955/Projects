package com.example.academiccompanion.ui.Announce;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.academiccompanion.R;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class AnnounceFragment extends Fragment {

    Spinner spnNoticeType, spnAnnounceType, spnDepartment, spnYear, spnSemester;
    EditText etDeadline, etNoticeText;
    Button btnAnnounce;
    TextView goToViewNotice;

    DatabaseReference noticeRef, subjectListRef;

    String selectedDepartment = "CSE";
    String selectedYear = "1st";
    String selectedSemester = "1";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_announce, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spnNoticeType = view.findViewById(R.id.spnnoticetype);
        spnAnnounceType = view.findViewById(R.id.spinner);
        spnDepartment = view.findViewById(R.id.spnDepartment);
        spnYear = view.findViewById(R.id.spnYear);
        spnSemester = view.findViewById(R.id.spnSemester);
        etDeadline = view.findViewById(R.id.deadline);
        etNoticeText = view.findViewById(R.id.noticetext);
        btnAnnounce = view.findViewById(R.id.btnAnnounce);
        goToViewNotice = view.findViewById(R.id.gotoviewnotice);

        noticeRef = FirebaseDatabase.getInstance().getReference("NoticeTable");
        subjectListRef = FirebaseDatabase.getInstance().getReference("SubjectList");

        setupSpinners();

        btnAnnounce.setOnClickListener(v -> {
            String subject = spnNoticeType.getSelectedItem() != null ?
                    spnNoticeType.getSelectedItem().toString() : "";
            String noticeType = spnAnnounceType.getSelectedItem().toString();
            String deadline = etDeadline.getText().toString().trim();
            String noticeText = etNoticeText.getText().toString().trim();

            if (subject.isEmpty() || deadline.isEmpty() || noticeText.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String pattern = "^\\d{2}-\\d{2}-\\d{4} (0[1-9]|1[0-2]):[0-5][0-9] (AM|PM)$";
            if (!deadline.matches(pattern)) {
                Toast.makeText(getContext(),
                        "Invalid format. Use dd-MM-yyyy hh:mm AM/PM (e.g., 27-01-2001 07:00 AM)",
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
                        "Invalid date/time. Please check the values.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            String noticeId = noticeRef.push().getKey();
            String issueDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            if (noticeId != null) {
                HashMap<String, String> noticeData = new HashMap<>();
                noticeData.put("noticeId", noticeId);
                noticeData.put("subject", subject);
                noticeData.put("noticeType", noticeType);
                noticeData.put("deadline", deadline);
                noticeData.put("text", noticeText);
                noticeData.put("issueDateTime", issueDateTime);
                noticeData.put("department", selectedDepartment);
                noticeData.put("year", selectedYear);
                noticeData.put("semester", selectedSemester);
                noticeData.put("deadlineTimestamp", String.valueOf(parsedDeadlineDate.getTime()));

                noticeRef.child(noticeId).setValue(noticeData).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Notice announced successfully!", Toast.LENGTH_SHORT).show();
                        etDeadline.setText("");
                        etNoticeText.setText("");
                    } else {
                        Toast.makeText(getContext(), "Failed: " + task.getException(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> depAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.departments, android.R.layout.simple_spinner_item);
        depAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnDepartment.setAdapter(depAdapter);
        spnDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDepartment = parent.getItemAtPosition(position).toString();
                loadSubjectsForSelection();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.years, android.R.layout.simple_spinner_item);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnYear.setAdapter(yearAdapter);
        spnYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = parent.getItemAtPosition(position).toString();
                loadSubjectsForSelection();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new String[]{"1", "2"});
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSemester.setAdapter(semesterAdapter);
        spnSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSemester = parent.getItemAtPosition(position).toString();
                loadSubjectsForSelection();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        ArrayAdapter<CharSequence> announceTypeAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.announcenoticetype, android.R.layout.simple_spinner_item);
        announceTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnAnnounceType.setAdapter(announceTypeAdapter);
    }

    private void loadSubjectsForSelection() {
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
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load subjects", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
