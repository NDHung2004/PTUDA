package com.example.sotaysv_nhom2.Fragment;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sotaysv_nhom2.NoteActivity;
import com.example.sotaysv_nhom2.AlarmReceiver;
import com.example.sotaysv_nhom2.Adapters.NoteAdapter;
import com.example.sotaysv_nhom2.Adapters.FilterAdapter;
import com.example.sotaysv_nhom2.Models.Note;
import com.example.sotaysv_nhom2.R;
import com.example.sotaysv_nhom2.SQLlite.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class NotesFragment extends Fragment {

    private List<Note> listNotes;
    private List<Note> sourceList;
    private NoteAdapter noteAdapter;
    private FilterAdapter filterAdapter;
    private RecyclerView rvNotes, rvFilter;
    private DatabaseHelper databaseHelper;
    private FloatingActionButton fabMain;

    private boolean isSelectionMode = false;
    private int currentFilterType = -1; // -1: Tất cả

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar_notes);
        if (getActivity() != null) { ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar); }

        databaseHelper = new DatabaseHelper(getContext());
        listNotes = new ArrayList<>();
        sourceList = new ArrayList<>();

        rvNotes = view.findViewById(R.id.rv_notes);
        rvFilter = view.findViewById(R.id.rv_filter);
        fabMain = view.findViewById(R.id.fab_add_note);

        rvNotes.setLayoutManager(new LinearLayoutManager(getContext()));

        // --- SETUP ADAPTER ---
        noteAdapter = new NoteAdapter(listNotes, new NoteAdapter.NoteClickListener() {
            @Override
            public void onNoteClick(Note note) {
                if (isSelectionMode) {
                    noteAdapter.toggleSelection(note.getId());
                } else {
                    Intent intent = new Intent(getActivity(), NoteActivity.class);
                    intent.putExtra("ID", note.getId());
                    intent.putExtra("TITLE", note.getTitle());
                    intent.putExtra("CONTENT", note.getContent());
                    intent.putExtra("ALARM_TIME", note.getAlarmTime());
                    intent.putExtra("REPEAT_TYPE", note.getRepeatType());
                    intent.putExtra("IS_UPDATE", true);
                    startActivity(intent);
                }
            }

            @Override
            public void onNoteLongClick(Note note) {
                startSelectionMode();
                // Adapter tự toggle selection, không cần gọi ở đây
            }

            @Override
            public void onSelectionChanged(int count) {
                if (count == 0 && isSelectionMode) {
                    exitSelectionMode();
                }
            }
        });
        rvNotes.setAdapter(noteAdapter);

        // Setup lọc lần đầu
        setupFilterList();

        // --- SỰ KIỆN FAB ---
        fabMain.setOnClickListener(v -> {
            if (isSelectionMode) {
                confirmDeleteSelected();
            } else {
                Intent intent = new Intent(getActivity(), NoteActivity.class);
                intent.putExtra("IS_UPDATE", false); // Báo rõ là thêm mới
                startActivity(intent);
            }
        });
    }

    // --- SỬA QUAN TRỌNG: RESET VỀ "TẤT CẢ" KHI QUAY LẠI ---
    @Override
    public void onResume() {
        super.onResume();

        // 1. Đặt lại bộ lọc về "Tất cả" để nhìn thấy note mới thêm
        currentFilterType = -1;

        // 2. Vẽ lại thanh lọc (để nút "Tất cả" sáng màu xanh)
        setupFilterList();

        // 3. Tải lại dữ liệu
        refreshData();
    }

    private void refreshData() {
        if (databaseHelper != null) {
            sourceList.clear();
            sourceList.addAll(databaseHelper.getAllNotes()); // Lấy list mới nhất (ID DESC)
            filterList(); // Lọc và hiển thị
        }
    }

    private void setupFilterList() {
        List<String> filters = new ArrayList<>();
        filters.add("Tất cả"); filters.add("Một lần"); filters.add("Hàng ngày"); filters.add("Hàng tuần");

        rvFilter.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        filterAdapter = new FilterAdapter(filters, position -> {
            // Logic chọn lọc
            if (position == 0) currentFilterType = -1;
            else currentFilterType = position - 1; // 0: Once, 1: Daily, 2: Weekly
            filterList();
        });
        rvFilter.setAdapter(filterAdapter);
    }

    private void filterList() {
        listNotes.clear();
        if (currentFilterType == -1) {
            // Hiện tất cả
            listNotes.addAll(sourceList);
        } else {
            // Hiện theo loại
            for (Note note : sourceList) {
                if (note.getRepeatType() == currentFilterType) {
                    listNotes.add(note);
                }
            }
        }
        noteAdapter.notifyDataSetChanged();
    }

    // --- QUẢN LÝ CHẾ ĐỘ CHỌN ---
    private void startSelectionMode() {
        isSelectionMode = true;
        noteAdapter.setSelectionMode(true);
        fabMain.setImageResource(android.R.drawable.ic_menu_delete);
        fabMain.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
    }

    private void exitSelectionMode() {
        isSelectionMode = false;
        noteAdapter.setSelectionMode(false);
        fabMain.setImageResource(android.R.drawable.ic_input_add);
        fabMain.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));
    }

    private void confirmDeleteSelected() {
        List<Integer> idsToDelete = new ArrayList<>(noteAdapter.getSelectedIds());
        if (idsToDelete.isEmpty()) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Xóa " + idsToDelete.size() + " ghi chú?")
                .setMessage("Bạn có chắc chắn muốn xóa không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    for (int id : idsToDelete) {
                        databaseHelper.deleteNote(id);
                        cancelAlarm(id);
                    }
                    refreshData();
                    exitSelectionMode();
                    Toast.makeText(getContext(), "Đã xóa!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void cancelAlarm(int noteId) {
        if (getContext() == null) return;
        AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), AlarmReceiver.class);
        // Hủy báo thức thường
        PendingIntent pi = PendingIntent.getBroadcast(getContext(), noteId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (am != null) am.cancel(pi);
        // Hủy báo thức tuần (ID con)
        for(int i=1; i<=7; i++) {
            PendingIntent piW = PendingIntent.getBroadcast(getContext(), noteId * 100 + i, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            if (am != null) am.cancel(piW);
        }
    }
}