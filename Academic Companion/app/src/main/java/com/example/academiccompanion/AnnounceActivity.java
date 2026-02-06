package com.example.academiccompanion;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class AnnounceActivity extends AppCompatActivity {

    Spinner spnNoticeType, spnAnnounceType, spnDepartment, spnYear;
    EditText etDeadline, etNoticeText;
    Button btnAnnounce;
    TextView goToViewNotice;

    DatabaseReference noticeRef;

    String selectedDepartment = "CSE"; // default
    String selectedYear = "1st";       // default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_announce);

        spnNoticeType = findViewById(R.id.spnnoticetype);
        spnAnnounceType = findViewById(R.id.spinner);
        spnDepartment = findViewById(R.id.spnDepartment);
        spnYear = findViewById(R.id.spnYear);
        etDeadline = findViewById(R.id.deadline);
        etNoticeText = findViewById(R.id.noticetext);
        btnAnnounce = findViewById(R.id.btnAnnounce);
        goToViewNotice = findViewById(R.id.gotoviewnotice);

        noticeRef = FirebaseDatabase.getInstance().getReference("NoticeTable");

        // Setup department spinner
        ArrayAdapter<CharSequence> depAdapter = ArrayAdapter.createFromResource(
                this, R.array.departments, android.R.layout.simple_spinner_item);
        depAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnDepartment.setAdapter(depAdapter);
        spnDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDepartment = parent.getItemAtPosition(position).toString();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Setup year spinner
        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(
                this, R.array.years, android.R.layout.simple_spinner_item);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnYear.setAdapter(yearAdapter);
        spnYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = parent.getItemAtPosition(position).toString();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnAnnounce.setOnClickListener(v -> {
            String subject = spnNoticeType.getSelectedItem().toString();
            String noticeType = spnAnnounceType.getSelectedItem().toString();
            String deadline = etDeadline.getText().toString().trim();
            String noticeText = etNoticeText.getText().toString().trim();

            if (deadline.isEmpty() || noticeText.isEmpty()) {
                Toast.makeText(AnnounceActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
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
                noticeData.put("department", selectedDepartment); // save department
                noticeData.put("year", selectedYear);             // save year

                noticeRef.child(noticeId).setValue(noticeData).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AnnounceActivity.this, "Notice announced successfully!", Toast.LENGTH_SHORT).show();
                        etDeadline.setText("");
                        etNoticeText.setText("");
                    } else {
                        Toast.makeText(AnnounceActivity.this, "Failed to announce notice: " + task.getException(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        goToViewNotice.setOnClickListener(view -> {
            finish();
        });
    }
}
