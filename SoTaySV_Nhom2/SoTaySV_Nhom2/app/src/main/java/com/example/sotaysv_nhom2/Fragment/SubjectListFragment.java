package com.example.sotaysv_nhom2.Fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sotaysv_nhom2.Adapters.FilterAdapter;
import com.example.sotaysv_nhom2.Adapters.SubjectAdapter;
import com.example.sotaysv_nhom2.Models.Subject;
import com.example.sotaysv_nhom2.R;
import com.example.sotaysv_nhom2.SQLlite.DatabaseHelper;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SubjectListFragment extends Fragment {

    // View
    private RecyclerView rvFilter, rvSubjects;
    private ExtendedFloatingActionButton fabAdd;
    String[] coeffinient = {"20-20","15-15","10-20","20-30"};
    // Adapter & Data
    private FilterAdapter filterAdapter;
    private SubjectAdapter subjectAdapter;

    private List<Subject> listDisplay;
    private List<Subject> sourceList;

    private DatabaseHelper databaseHelper;
    private int currentFilterType = 0;
    private boolean isSelectionMode = false;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subject_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar_subject_list);
        if (getActivity() != null) { ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar); }

        databaseHelper = new DatabaseHelper(getContext());

        rvFilter = view.findViewById(R.id.rv_filter_subject);
        rvSubjects = view.findViewById(R.id.rv_subjects_main);
        fabAdd = view.findViewById(R.id.fab_add_subject);
        fabAdd.setText("THÊM MÔN");

        listDisplay = new ArrayList<>();
        sourceList = new ArrayList<>();

        // --- CẤU HÌNH ADAPTER VỚI LOGIC POPUP MENU ---
        rvSubjects.setLayoutManager(new LinearLayoutManager(getContext()));
        subjectAdapter = new SubjectAdapter(listDisplay, new SubjectAdapter.SubjectClickListener() {
            @Override
            public void onSubjectClick(View v, Subject subject) { // Đã có View v
                if (isSelectionMode) {
                    subjectAdapter.toggleSelection(subject.getId());
                } else {
                    // Hiện Popup Menu tại vị trí view v
                    showSubjectOptionsMenu(v, subject);
                }
            }

            @Override
            public void onSubjectLongClick(Subject subject) {
                startSelectionMode();
                subjectAdapter.toggleSelection(subject.getId());
            }

            @Override
            public void onSelectionChanged(int count) {
                if (isSelectionMode) fabAdd.setText("XÓA (" + count + ")");
                if (count == 0 && isSelectionMode) exitSelectionMode();
            }
        });
        rvSubjects.setAdapter(subjectAdapter);

        setupFilterList();

        fabAdd.setOnClickListener(v -> {
            if (isSelectionMode) confirmDeleteSelected();
            else showAddSubjectDialog();
        });

        loadData();
    }

    @Override public void onResume() { super.onResume(); loadData(); }

    // ==========================================
    // KHU VỰC: POPUP MENU & CẬP NHẬT (MỚI)
    // ==========================================

    private void showSubjectOptionsMenu(View view, Subject subject) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_subject_options, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_update_subject) {
                showUpdateSubjectDialog(subject); // Mở dialog cập nhật
                return true;
            } else if (item.getItemId() == R.id.action_view_note) {
                openSubjectNote(subject); // Xem note
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void showUpdateSubjectDialog(Subject subject) {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_subject);

        // 1. SỬA LỖI TVTITLE: Khai báo và ánh xạ biến này
        TextView tvTitle = dialog.findViewById(R.id.tv_note_title);

        EditText edtName = dialog.findViewById(R.id.edt_name);
        EditText edtCredits = dialog.findViewById(R.id.edt_credits);
        EditText edtSemester = dialog.findViewById(R.id.edt_semester);
        EditText edtFinalScore = dialog.findViewById(R.id.edt_final_score);
        CheckBox cbHasScore = dialog.findViewById(R.id.cb_has_score);
        TextInputLayout layoutScore = dialog.findViewById(R.id.layout_score);
        Button btnSave = dialog.findViewById(R.id.btn_save);
        Spinner spinnerCoefficient = dialog.findViewById(R.id.spinnerCoefficient);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, coeffinient);

        spinnerCoefficient.setAdapter(adapter);

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Kiểm tra null trước khi set text (đề phòng file xml không có id này)
        if(tvTitle != null) tvTitle.setText("CẬP NHẬT MÔN HỌC");

        btnSave.setText("LƯU THAY ĐỔI");
        spinnerCoefficient.setSelection(adapter.getPosition(subject.getCoefficient()));
        edtName.setText(subject.getName());
        edtCredits.setText(String.valueOf(subject.getCredits()));
        edtSemester.setText(String.valueOf(subject.getSemester()));

        // 2. SỬA LỖI GETSCORE -> GETSCORE10 (Theo model của bạn)
        if (subject.getScore10() >= 0) {
            cbHasScore.setChecked(true);
            layoutScore.setVisibility(View.VISIBLE);
            edtFinalScore.setText(String.valueOf(subject.getScore10())); // Sửa ở đây
        } else {
            cbHasScore.setChecked(false);
            layoutScore.setVisibility(View.GONE);
        }

        cbHasScore.setOnCheckedChangeListener((v, isChecked) ->
                layoutScore.setVisibility(isChecked ? View.VISIBLE : View.GONE)
        );

        btnSave.setOnClickListener(v -> {
            try {
                String name = edtName.getText().toString().trim();
                String creditsStr = edtCredits.getText().toString().trim();
                String semesterStr = edtSemester.getText().toString().trim();
                String coefficient = spinnerCoefficient.getSelectedItem().toString();

                if (name.isEmpty() || creditsStr.isEmpty() || semesterStr.isEmpty()) {
                    Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int credits = Integer.parseInt(creditsStr);
                int semester = Integer.parseInt(semesterStr);
                double finalScore = -1.0;

                if (cbHasScore.isChecked()) {
                    String scoreStr = edtFinalScore.getText().toString().trim();
                    if (scoreStr.isEmpty()) { Toast.makeText(getContext(), "Nhập điểm!", Toast.LENGTH_SHORT).show(); return; }
                    finalScore = Double.parseDouble(scoreStr);
                    if (finalScore < 0 || finalScore > 10) { Toast.makeText(getContext(), "Điểm 0-10!", Toast.LENGTH_SHORT).show(); return; }
                }

                // Cập nhật Object
                subject.setName(name);
                subject.setCoefficient(coefficient);
                subject.setCredits(credits);
                subject.setSemester(semester);

                // 3. SỬA LỖI SETSCORE -> SETSCORE10
                subject.setScore10(finalScore);

                // GỌI DATABASE UPDATE
                databaseHelper.updateSubject(subject);

                loadData();
                dialog.dismiss();
                Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                Toast.makeText(getContext(), "Lỗi nhập liệu!", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    // ==========================================
    // KHU VỰC: CÁC HÀM CŨ (GIỮ NGUYÊN)
    // ==========================================

    private void loadData() {
        if (databaseHelper != null) {
            sourceList.clear();
            sourceList.addAll(databaseHelper.getAllSubjects());
            filterList();
        }
    }

    private void setupFilterList() {
        List<String> filters = new ArrayList<>();
        filters.add("Tất cả");
        filters.add("Đang học");
        filters.add("Đã học");

        rvFilter.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        filterAdapter = new FilterAdapter(filters, position -> {
            currentFilterType = position;
            filterList();
        });
        rvFilter.setAdapter(filterAdapter);
    }

    private void filterList() {
        listDisplay.clear();
        if (currentFilterType == 0) listDisplay.addAll(sourceList);
        else if (currentFilterType == 1) {
            for (Subject sub : sourceList) if (sub.isStudying()) listDisplay.add(sub);
        } else {
            for (Subject sub : sourceList) if (!sub.isStudying()) listDisplay.add(sub);
        }
        subjectAdapter.notifyDataSetChanged();
    }

    private void startSelectionMode() {
        if (isSelectionMode) return;
        isSelectionMode = true;
        subjectAdapter.setSelectionMode(true);
        fabAdd.setText("XÓA");
        fabAdd.setIconResource(android.R.drawable.ic_menu_delete);
        fabAdd.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
    }

    private void exitSelectionMode() {
        isSelectionMode = false;
        subjectAdapter.setSelectionMode(false);
        fabAdd.setText("THÊM MÔN");
        fabAdd.setIconResource(android.R.drawable.ic_input_add);
        fabAdd.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));
    }

    private void confirmDeleteSelected() {
        Set<Integer> ids = subjectAdapter.getSelectedIds();
        if (ids.isEmpty()) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa " + ids.size() + " môn học?")
                .setPositiveButton("Xóa", (d, w) -> {
                    for (int id : ids) databaseHelper.deleteSubject(id);
                    exitSelectionMode();
                    loadData();
                })
                .setNegativeButton("Hủy", null).show();
    }

    private void openSubjectNote(Subject subject) {
        SubjectNoteFragment fragment = SubjectNoteFragment.newInstance(subject.getName(), subject.getCode());
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null).commit();
    }

    private void showAddSubjectDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_subject);

        EditText edtName = dialog.findViewById(R.id.edt_name);
        EditText edtCredits = dialog.findViewById(R.id.edt_credits);
        EditText edtSemester = dialog.findViewById(R.id.edt_semester);
        EditText edtFinalScore = dialog.findViewById(R.id.edt_final_score);
        CheckBox cbHasScore = dialog.findViewById(R.id.cb_has_score);
        TextInputLayout layoutScore = dialog.findViewById(R.id.layout_score);
        Button btnSave = dialog.findViewById(R.id.btn_save);
        Spinner spinnerCoefficient = dialog.findViewById(R.id.spinnerCoefficient);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, coeffinient);
        spinnerCoefficient.setAdapter(adapter);

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        cbHasScore.setOnCheckedChangeListener((v, isChecked) ->
                layoutScore.setVisibility(isChecked ? View.VISIBLE : View.GONE)
        );

        btnSave.setOnClickListener(v -> {
            try {
                String name = edtName.getText().toString().trim();
                String creditsStr = edtCredits.getText().toString().trim();
                String semesterStr = edtSemester.getText().toString().trim();
                String coefficient = spinnerCoefficient.getSelectedItem().toString();

                if (name.isEmpty() || creditsStr.isEmpty() || semesterStr.isEmpty()) {
                    Toast.makeText(getContext(), "Nhập đủ thông tin!", Toast.LENGTH_SHORT).show(); return;
                }

                int credits = Integer.parseInt(creditsStr);
                int semester = Integer.parseInt(semesterStr);
                double finalScore = -1.0;

                if (cbHasScore.isChecked()) {
                    String scoreStr = edtFinalScore.getText().toString().trim();
                    if (scoreStr.isEmpty()) return;
                    finalScore = Double.parseDouble(scoreStr);
                }

                // Check trùng tên
                for(Subject s : sourceList) {
                    if(s.getName().equalsIgnoreCase(name)) {
                        Toast.makeText(getContext(), "Môn này đã có rồi!", Toast.LENGTH_SHORT).show(); return;
                    }
                }

                databaseHelper.addSubject(new Subject(0, "", name,coefficient, credits, finalScore, semester));
                loadData();
                dialog.dismiss();
            } catch (Exception e) {
                Toast.makeText(getContext(), "Lỗi nhập liệu!", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }
}