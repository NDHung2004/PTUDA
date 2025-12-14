package com.example.sotaysv_nhom2.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sotaysv_nhom2.Models.Note;
import com.example.sotaysv_nhom2.R;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<Note> mListNote;
    private NoteClickListener listener;

    private boolean isSelectionMode = false;
    private Set<Integer> selectedIds = new HashSet<>();

    public interface NoteClickListener {
        void onNoteClick(Note note);
        void onNoteLongClick(Note note);
        void onSelectionChanged(int count);
    }

    public NoteAdapter(List<Note> mListNote, NoteClickListener listener) {
        this.mListNote = mListNote;
        this.listener = listener;
    }

    public void setSelectionMode(boolean isSelectionMode) {
        this.isSelectionMode = isSelectionMode;
        if (!isSelectionMode) selectedIds.clear();
        notifyDataSetChanged();
    }

    public Set<Integer> getSelectedIds() { return selectedIds; }

    public void toggleSelection(int noteId) {
        if (selectedIds.contains(noteId)) selectedIds.remove(noteId);
        else selectedIds.add(noteId);
        notifyDataSetChanged();
        if (listener != null) listener.onSelectionChanged(selectedIds.size());
    }

    @NonNull @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = mListNote.get(position);
        if(note == null) return;

        holder.tvTitle.setText(note.getTitle());
        if (note.getContent() == null || note.getContent().isEmpty()) holder.tvContent.setVisibility(View.GONE);
        else { holder.tvContent.setVisibility(View.VISIBLE); holder.tvContent.setText(note.getContent()); }

        // --- LOGIC HIỂN THỊ THỜI GIAN (SỬA LỖI TẠI ĐÂY) ---
        // Chúng ta dùng chung 1 TextView tvTime cho cả 2 trường hợp
        holder.tvTime.setVisibility(View.VISIBLE);

        if (note.getAlarmTime() != null && !note.getAlarmTime().isEmpty()) {
            // === CÓ BÁO THỨC ===
            holder.tvTime.setVisibility(View.VISIBLE); // Hiện thời gian
            holder.tvTime.setText(note.getAlarmTime());

            holder.imgType.setVisibility(View.VISIBLE);
            holder.imgType.setImageResource(android.R.drawable.ic_lock_idle_alarm);

            if (note.isDaily()) {
                holder.viewAccent.setBackgroundColor(Color.parseColor("#4CAF50"));
                holder.imgType.setColorFilter(Color.parseColor("#4CAF50"));
            } else if (note.isWeekly()) {
                holder.viewAccent.setBackgroundColor(Color.parseColor("#9C27B0"));
                holder.imgType.setColorFilter(Color.parseColor("#9C27B0"));
            } else {
                holder.viewAccent.setBackgroundColor(Color.parseColor("#2196F3"));
                holder.imgType.setColorFilter(Color.parseColor("#2196F3"));
            }
        } else {
            // === NOTE THƯỜNG (KHÔNG CÓ BÁO THỨC) ===

            holder.tvTime.setVisibility(View.GONE);
            holder.imgType.setVisibility(View.VISIBLE);
            holder.imgType.setImageResource(android.R.drawable.ic_menu_edit);
            holder.imgType.setColorFilter(Color.parseColor("#E0E0E0")); // Màu xám rất nhạt
            holder.viewAccent.setBackgroundColor(Color.parseColor("#E0E0E0"));
        }

        // --- Xử lý Chọn nhiều ---
        if (isSelectionMode) {
            holder.checkBox.setVisibility(View.GONE);
            if (selectedIds.contains(note.getId())) holder.cardView.setCardBackgroundColor(Color.parseColor("#BBDEFB"));
            else holder.cardView.setCardBackgroundColor(Color.WHITE);
        } else {
            holder.checkBox.setVisibility(View.GONE);
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) toggleSelection(note.getId());
            else listener.onNoteClick(note);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                listener.onNoteLongClick(note);
                toggleSelection(note.getId());
            }
            return true;
        });
    }

    @Override public int getItemCount() { return mListNote.size(); }

    public class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvTime; // CHỈ GIỮ LẠI tvTime
        ImageView imgType;
        View viewAccent;
        CheckBox checkBox;
        CardView cardView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_note_title);
            tvContent = itemView.findViewById(R.id.tv_note_content);

            // SỬA: Ánh xạ tvTime vào ID tv_note_time (đúng như XML của bạn)
            tvTime = itemView.findViewById(R.id.tv_note_time);

            imgType = itemView.findViewById(R.id.img_note_type);
            viewAccent = itemView.findViewById(R.id.view_accent_bar);
            checkBox = itemView.findViewById(R.id.checkbox_select);

            // Tìm CardView cha
            if (itemView instanceof CardView) {
                cardView = (CardView) itemView;
            } else {
                cardView = itemView.findViewById(R.id.item_card_root);
            }
        }
    }
}