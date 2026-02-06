package com.example.academiccompanion.ui.view_notice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.academiccompanion.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ViewNoticeFragment extends Fragment {

    private RecyclerView recyclerView;
    private NoticeAdapter noticeAdapter;
    private final List<Notice> noticeList = new ArrayList<>();
    private DatabaseReference noticeRef, userRef;
    private String userDepartment = "";
    private String userYear = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_notice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.noticeRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        noticeAdapter = new NoticeAdapter(noticeList);
//        recyclerView.setAdapter(noticeAdapter);

        noticeAdapter = new NoticeAdapter(noticeList, () -> loadNoticesFromFirebase());
        recyclerView.setAdapter(noticeAdapter);

        noticeRef = FirebaseDatabase.getInstance().getReference("NoticeTable");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            loadUserDetailsAndNotices();
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserDetailsAndNotices() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userDepartment = snapshot.child("department").getValue(String.class);
                userYear = snapshot.child("year").getValue(String.class);

                if (userDepartment == null) userDepartment = "";
                if (userYear == null) userYear = "";

                loadNoticesFromFirebase();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to get user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadNoticesFromFirebase() {
        noticeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                noticeList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Notice notice = dataSnapshot.getValue(Notice.class);
                    if (notice == null) continue;

                    // ✅ Step 1: Filter based on user's department and year
                    if (!userDepartment.equals(notice.department) || !userYear.equals(notice.year)) {
                        continue;
                    }

                    // ✅ Step 2: Only include notices with valid timestamp
                    try {
                        if (notice.deadlineTimestamp != null && !notice.deadlineTimestamp.isEmpty()) {
                            Long.parseLong(notice.deadlineTimestamp); // validation only
                            noticeList.add(notice);
                        }
                    } catch (NumberFormatException ignored) {
                        // Skip invalid timestamps
                    }
                }

                // ✅ Step 3: Sort filtered notices by deadlineTimestamp (ascending)
                noticeList.sort((n1, n2) -> {
                    try {
                        long t1 = Long.parseLong(n1.deadlineTimestamp);
                        long t2 = Long.parseLong(n2.deadlineTimestamp);
                        return Long.compare(t1, t2);
                    } catch (Exception e) {
                        return 0;
                    }
                });

                noticeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load notices: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
