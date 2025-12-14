package com.example.sotaysv_nhom2.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sotaysv_nhom2.Models.GradeUtils;
import com.example.sotaysv_nhom2.Models.Subject;
import com.example.sotaysv_nhom2.R;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {
    private List<Subject> mListSubject;
    private SubjectClickListener listener;
    private boolean isSelectionMode = false;
    private Set<Integer> selectedIds = new HashSet<>();

    // --- SỬA INTERFACE: Thêm View view ---
    public interface SubjectClickListener {
        void onSubjectClick(View view, Subject subject);
        void onSubjectLongClick(Subject subject);
        void onSelectionChanged(int count);
    }

    public SubjectAdapter(List<Subject> mListSubject, SubjectClickListener listener) {
        this.mListSubject = mListSubject;
        this.listener = listener;
    }

    public void setSelectionMode(boolean isSelectionMode) {
        this.isSelectionMode = isSelectionMode;
        if (!isSelectionMode) selectedIds.clear();
        notifyDataSetChanged();
    }

    public Set<Integer> getSelectedIds() { return selectedIds; }

    public void toggleSelection(int id) {
        if (selectedIds.contains(id)) selectedIds.remove(id); else selectedIds.add(id);
        notifyDataSetChanged();
        if (listener != null) listener.onSelectionChanged(selectedIds.size());
    }

    @NonNull @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject subject = mListSubject.get(position);
        if (subject == null) return;

        // 1. Gán dữ liệu Text
        holder.tvName.setText(subject.getName());
        holder.tvCredits.setText(subject.getCredits() + " tín chỉ - Kỳ " + subject.getSemester());

        // 2. Logic hiển thị Điểm
        if (subject.isStudying()) {
            holder.tvScore10.setText("---");
            holder.tvScoreLetter.setText("Đang học");
            holder.tvScore10.setTextColor(Color.parseColor("#757575"));
            holder.tvScoreLetter.setTextColor(Color.parseColor("#757575"));
        } else {
            holder.tvScore10.setText(String.valueOf(subject.getScore10()));
            holder.tvScoreLetter.setText("Điểm " + GradeUtils.convertToLetter(subject.getScore10()));
            int color = (subject.getScore10() >= 8.0) ? Color.parseColor("#4CAF50") : (subject.getScore10() >= 5.0 ? Color.parseColor("#FF9800") : Color.parseColor("#F44336"));
            holder.tvScore10.setTextColor(color);
            holder.tvScoreLetter.setTextColor(color);
        }

        // 3. Logic Màu nền (Khi chọn/Không chọn)
        if (isSelectionMode && selectedIds.contains(subject.getId())) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#BBDEFB"));
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(subject.getId());
            } else {
                listener.onSubjectClick(v, subject); // Truyền v vào đây
            }
        });

        // 5. Sự kiện Long Click
        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                listener.onSubjectLongClick(subject);
            }
            return true;
        });
    }

    @Override public int getItemCount() { return mListSubject.size(); }

    public class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCredits, tvScore10, tvScoreLetter;
        CardView cardView;
        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.item_subject_name);
            tvCredits = itemView.findViewById(R.id.item_subject_credits);
            tvScore10 = itemView.findViewById(R.id.item_score_10);
            tvScoreLetter = itemView.findViewById(R.id.item_score_letter);
            cardView = (CardView) itemView;
        }
    }
}