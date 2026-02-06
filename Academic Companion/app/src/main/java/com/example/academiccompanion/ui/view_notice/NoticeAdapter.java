package com.example.academiccompanion.ui.view_notice;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.academiccompanion.R;
import com.example.academiccompanion.ui.editnotice.EditNoticeFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder> {

    private final List<Notice> noticeList;
    private final OnNoticeDeletedListener deleteListener;
    private String currentUserType = "";

    private final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
    private final DatabaseReference noticeRef = FirebaseDatabase.getInstance().getReference("NoticeTable");

    // Constructor with delete callback
    public NoticeAdapter(List<Notice> noticeList, OnNoticeDeletedListener deleteListener) {
        this.noticeList = noticeList;
        this.deleteListener = deleteListener;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userRef.child(currentUser.getUid()).get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    currentUserType = snapshot.child("userType").getValue(String.class);
                    notifyDataSetChanged();  // refresh when userType is loaded
                }
            });
        }
    }

    @NonNull
    @Override
    public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notice_card, parent, false);
        return new NoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
        Notice notice = noticeList.get(position);

        holder.tvDeadline.setText(notice.deadline);
        holder.tvTypeSubject.setText(notice.noticeType + ": " + notice.subject);
        holder.tvIssueDate.setText("Issued: " + extractDate(notice.issueDateTime));
        holder.tvDescription.setText(notice.text);

        // Expand/collapse text
        holder.tvDescription.setMaxLines(3);
        holder.tvDescription.setEllipsize(TextUtils.TruncateAt.END);
        holder.tvReadMore.setText("More...");
        holder.tvReadMore.setOnClickListener(v -> {
            boolean isExpanded = holder.tvDescription.getMaxLines() > 3;
            if (isExpanded) {
                holder.tvDescription.setMaxLines(3);
                holder.tvDescription.setEllipsize(TextUtils.TruncateAt.END);
                holder.tvReadMore.setText("More...");
            } else {
                holder.tvDescription.setMaxLines(Integer.MAX_VALUE);
                holder.tvDescription.setEllipsize(null);
                holder.tvReadMore.setText("Less...");
            }
        });

        // Show edit/delete only for CR or Teacher
        if ("CR".equalsIgnoreCase(currentUserType) || "Faculty".equalsIgnoreCase(currentUserType)) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);

            // Edit action
            holder.btnEdit.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("noticeId", notice.noticeId);

                EditNoticeFragment editFragment = new EditNoticeFragment();
                editFragment.setArguments(bundle);

                ((FragmentActivity) v.getContext())
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment_content_main, editFragment)
                        .addToBackStack(null)
                        .commit();
            });

            // Delete action
            holder.btnDelete.setOnClickListener(v -> {
                int pos = holder.getAbsoluteAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                Notice noticeToDelete = noticeList.get(pos);

                new AlertDialog.Builder(v.getContext())
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete this notice?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            noticeRef.child(noticeToDelete.noticeId)
                                    .removeValue()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(v.getContext(), "Notice deleted", Toast.LENGTH_SHORT).show();
                                            if (deleteListener != null) {
                                                deleteListener.onNoticeDeleted(); // trigger reload
                                            }
                                        } else {
                                            Toast.makeText(v.getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

        } else {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return noticeList != null ? noticeList.size() : 0;
    }

    static class NoticeViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeadline, tvTypeSubject, tvIssueDate, tvDescription, tvReadMore;
        Button btnEdit, btnDelete;

        public NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            tvTypeSubject = itemView.findViewById(R.id.tvTypeSubject);
            tvIssueDate = itemView.findViewById(R.id.tvIssueDate);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvReadMore = itemView.findViewById(R.id.tvReadMore);
            btnEdit = itemView.findViewById(R.id.btnEditNotice);
            btnDelete = itemView.findViewById(R.id.btnDeleteNotice);
        }
    }

    private String extractDate(String dateTime) {
        if (dateTime != null && dateTime.contains(" ")) {
            return dateTime.split(" ")[0];
        }
        return dateTime != null ? dateTime : "";
    }

    public interface OnNoticeDeletedListener {
        void onNoticeDeleted();
    }
}
