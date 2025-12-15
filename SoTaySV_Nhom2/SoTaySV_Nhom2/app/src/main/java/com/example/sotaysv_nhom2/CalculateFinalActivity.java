package com.example.sotaysv_nhom2;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.sotaysv_nhom2.Models.GradeUtils;
import com.example.sotaysv_nhom2.Models.Subject;
import com.example.sotaysv_nhom2.SQLlite.DatabaseHelper;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CalculateFinalActivity extends AppCompatActivity {

    private AutoCompleteTextView edtName;
    private EditText edtCredits, edtSemester, edtTx1, edtTx2, edtTx3, edtExam;
    private TextInputLayout inputLayoutTx3;
    private TextView tvScore, tvLetter;
    private Button btnCalc, btnSave;
    private DatabaseHelper dbHelper;

    // Danh sách môn học để kiểm tra trùng
    private List<Subject> allSubjectsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_final);

        Toolbar toolbar = findViewById(R.id.toolbar_final);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        dbHelper = new DatabaseHelper(this);
        // Lấy danh sách môn học ngay từ đầu
        allSubjectsList = dbHelper.getAllSubjects();

        edtName = findViewById(R.id.edt_subject_name);
        edtCredits = findViewById(R.id.edt_credits);
        edtSemester = findViewById(R.id.edt_semester);
        edtTx1 = findViewById(R.id.edt_tx1);
        edtTx2 = findViewById(R.id.edt_tx2);
        edtTx3 = findViewById(R.id.edt_tx3);
        inputLayoutTx3 = findViewById(R.id.layout_tx3);
        edtExam = findViewById(R.id.edt_exam_score);
        tvScore = findViewById(R.id.tv_final_score);
        tvLetter = findViewById(R.id.tv_final_letter);
        btnCalc = findViewById(R.id.btn_calc);
        btnSave = findViewById(R.id.btn_save);

        // 1. Logic ẩn hiện GK theo tín chỉ
        edtCredits.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty() && Integer.parseInt(s.toString()) >= 4) {
                    inputLayoutTx3.setVisibility(View.VISIBLE);
                } else {
                    inputLayoutTx3.setVisibility(View.GONE);
                    edtTx3.setText("");
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // 2. Setup Gợi ý + Logic Khóa/Mở khóa ô nhập
        setupSubjectSuggestionAndAutoLock();

        btnCalc.setOnClickListener(v -> calculateScore(false));
        btnSave.setOnClickListener(v -> saveToSubject());
    }

    private void setupSubjectSuggestionAndAutoLock() {
        Set<String> subjectNames = new HashSet<>();
        for (Subject sub : allSubjectsList) if (sub.getName() != null) subjectNames.add(sub.getName());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>(subjectNames));
        edtName.setAdapter(adapter);

        edtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String inputName = s.toString().trim();
                Subject foundSubject = null;

                // Tìm xem tên vừa nhập có trong DB chưa
                for (Subject sub : allSubjectsList) {
                    if (sub.getName().equalsIgnoreCase(inputName)) {
                        foundSubject = sub;
                        break;
                    }
                }

                if (foundSubject != null) {
                    // --- ĐÃ CÓ MÔN NÀY ---
                    // 1. Tự động điền thông tin cũ
                    edtCredits.setText(String.valueOf(foundSubject.getCredits()));
                    edtSemester.setText(String.valueOf(foundSubject.getSemester()));

                    // 2. KHÓA ô nhập (Không cho sửa Tín chỉ & Kỳ)
                    edtCredits.setEnabled(false);
                    edtSemester.setEnabled(false);

                    // (Tùy chọn) Đổi màu nền xám để báo hiệu bị khóa
                    // edtCredits.setBackgroundColor(Color.LTGRAY);

                } else {
                    // --- MÔN MỚI ---
                    // MỞ KHÓA ô nhập cho người dùng tự điền
                    edtCredits.setEnabled(true);
                    edtSemester.setEnabled(true);

                    // (Tùy chọn) Trả lại màu nền trắng
                    // edtCredits.setBackgroundColor(Color.TRANSPARENT);
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private double getValidScore(EditText edt, String fieldName) throws Exception {
        String txt = edt.getText().toString().trim();
        if (txt.isEmpty()) throw new Exception("Vui lòng nhập " + fieldName);
        double score = Double.parseDouble(txt);
        if (score < 0 || score > 10) throw new Exception(fieldName + " phải từ 0-10!");
        return score;
    }

    private double calculateData(String coefficient) throws Exception {
        String[] phan_tu = coefficient.split("-");
        int coefficient_tx1 = Integer.parseInt(phan_tu[0])/10;
        int coefficient_tx2 = Integer.parseInt(phan_tu[1])/10;
        double tx1 = getValidScore(edtTx1, "TX1");
        double tx2 = getValidScore(edtTx2, "TX2");
        double exam = getValidScore(edtExam, "Điểm thi");
        double finalScore = (tx1*coefficient_tx1 + tx2*coefficient_tx2 + exam*(10-coefficient_tx1-coefficient_tx2))/10;
        return finalScore;



    }

    private void calculateScore(boolean isSaving) {
        try {
            Subject existingSubject = null;
            for (Subject s : allSubjectsList) {
                if (s.getName().equalsIgnoreCase(edtName.getText().toString().trim())) {
                    existingSubject = s;
                    break;
                }
            }
            String coefficient="10-20";
            if(existingSubject != null){
                coefficient = existingSubject.getCoefficient();
            }
            if(isSaving) validateInfo();

            double finalScore = calculateData(coefficient);
            String letter = GradeUtils.convertToLetter(finalScore);
            double scale4 = GradeUtils.convertToScale4(finalScore);
            tvScore.setText(String.valueOf(finalScore));
            tvLetter.setText("Điểm chữ: " + letter + " (" + scale4 + ")");
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToSubject() {
        try {
            validateInfo();

            double finalScore = calculateData("10-20");

            String name = edtName.getText().toString().trim();
            int credits = Integer.parseInt(edtCredits.getText().toString());
            int semester = Integer.parseInt(edtSemester.getText().toString());

            // Kiểm tra lại lần cuối xem môn này đã có chưa để quyết định Update hay Insert
            Subject existingSubject = null;
            for (Subject s : allSubjectsList) {
                if (s.getName().equalsIgnoreCase(name)) {
                    existingSubject = s;
                    break;
                }
            }

            if (existingSubject != null) {
                finalScore = calculateData(existingSubject.getCoefficient());
                // CẬP NHẬT (Dùng ID cũ)
                Subject updateSubject = new Subject(
                        existingSubject.getId(),
                        existingSubject.getCode(),
                        name, existingSubject.getCoefficient(),credits, finalScore, semester
                );
                dbHelper.updateSubject(updateSubject);
                Toast.makeText(this, "Đã cập nhật điểm môn: " + name, Toast.LENGTH_SHORT).show();
            } else {
                // THÊM MỚI
                Subject newSubject = new Subject(0, "", name,"10-20", credits, finalScore, semester);
                dbHelper.addSubject(newSubject);
                Toast.makeText(this, "Đã thêm môn mới: " + name, Toast.LENGTH_SHORT).show();
            }

            finish();

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void validateInfo() throws Exception {
        if (edtName.getText().toString().trim().isEmpty()) throw new Exception("Nhập tên môn!");
        if (edtCredits.getText().toString().isEmpty()) throw new Exception("Nhập số tín chỉ!");
        if (edtSemester.getText().toString().isEmpty()) throw new Exception("Nhập học kỳ!");
    }
}