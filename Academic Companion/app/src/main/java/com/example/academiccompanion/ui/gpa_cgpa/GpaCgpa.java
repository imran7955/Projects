package com.example.academiccompanion.ui.gpa_cgpa;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.academiccompanion.R;

public class GpaCgpa extends Fragment {

    Spinner spinnerSemesterCount;
    LinearLayout inputContainer;
    TextView tvCgpaResult;

    private static final int MAX_SEMESTERS = 8;
    private EditText[] gpaInputs = new EditText[MAX_SEMESTERS];
    private EditText[] creditInputs = new EditText[MAX_SEMESTERS];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gpa_cgpa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerSemesterCount = view.findViewById(R.id.spinnerSemesterCount);
        inputContainer = view.findViewById(R.id.inputContainer);
        tvCgpaResult = view.findViewById(R.id.tvCgpaResult);

        setupSemesterSpinner();
    }

    private void setupSemesterSpinner() {
        Integer[] semesters = new Integer[MAX_SEMESTERS];
        for (int i = 0; i < MAX_SEMESTERS; i++) semesters[i] = i + 1;

        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                semesters
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemesterCount.setAdapter(adapter);

        spinnerSemesterCount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                int semesterCount = position + 1;
                generateInputFields(semesterCount);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void generateInputFields(int count) {
        inputContainer.removeAllViews();

        for (int i = 0; i < count; i++) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 8, 0, 8);

            TextView tvSem = new TextView(requireContext());
            tvSem.setText("Sem " + (i + 1) + ": ");
            tvSem.setTextSize(16f);
            tvSem.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            EditText gpaInput = new EditText(requireContext());
            gpaInput.setHint("GPA");
            gpaInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            gpaInput.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 2));

            EditText creditInput = new EditText(requireContext());
            creditInput.setHint("Credit");
            creditInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            creditInput.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 2));

            gpaInputs[i] = gpaInput;
            creditInputs[i] = creditInput;

            gpaInput.addTextChangedListener(textWatcher);
            creditInput.addTextChangedListener(textWatcher);

            row.addView(tvSem);
            row.addView(gpaInput);
            row.addView(creditInput);

            inputContainer.addView(row);
        }

        calculateCgpa(); // recalculate on generation
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(Editable s) {
            calculateCgpa();
        }
    };

    private void calculateCgpa() {
        double totalGradePoints = 0.0;
        double totalCredits = 0.0;

        for (int i = 0; i < MAX_SEMESTERS; i++) {
            EditText gpaField = gpaInputs[i];
            EditText creditField = creditInputs[i];

            if (gpaField != null && creditField != null) {
                String gpaStr = gpaField.getText().toString().trim();
                String creditStr = creditField.getText().toString().trim();

                if (!gpaStr.isEmpty() && !creditStr.isEmpty()) {
                    try {
                        double gpa = Double.parseDouble(gpaStr);
                        double credit = Double.parseDouble(creditStr);

                        if (gpa >= 0 && gpa <= 10 && credit > 0) {
                            totalGradePoints += gpa * credit;
                            totalCredits += credit;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        double cgpa = totalCredits > 0 ? totalGradePoints / totalCredits : 0;
        tvCgpaResult.setText(String.format("%.2f", cgpa));
    }
}
