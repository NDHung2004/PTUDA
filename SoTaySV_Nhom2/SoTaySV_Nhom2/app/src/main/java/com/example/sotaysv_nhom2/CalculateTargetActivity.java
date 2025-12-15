package com.example.sotaysv_nhom2;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.sotaysv_nhom2.Models.Note;
import com.example.sotaysv_nhom2.Models.Subject;
import com.example.sotaysv_nhom2.SQLlite.DatabaseHelper;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CalculateTargetActivity extends AppCompatActivity {

    private AutoCompleteTextView edtSubjectName; // Dùng AutoCompleteTextView
    private EditText edtCredits, edtTx1, edtTx2, edtTx3;
    private TextInputLayout inputLayoutTx3;
    private Spinner spinnerTarget;
    private TextView tvResult;
    private Button btnCalculate, btnSaveNote;
    private DatabaseHelper databaseHelper;
    private List<Subject> allSubjectsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_target);

        Toolbar toolbar = findViewById(R.id.toolbar_calculate);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        databaseHelper = new DatabaseHelper(this);
        allSubjectsList = databaseHelper.getAllSubjects();

        // Ánh xạ
        edtSubjectName = findViewById(R.id.edt_subject_name_target);
        edtCredits = findViewById(R.id.edt_credits_target);
        edtTx1 = findViewById(R.id.edt_tx1);
        edtTx2 = findViewById(R.id.edt_tx2);
        edtTx3 = findViewById(R.id.edt_tx3);
        inputLayoutTx3 = findViewById(R.id.layout_tx3);
        spinnerTarget = findViewById(R.id.spinner_target_grade);
        tvResult = findViewById(R.id.tv_result_target);
        btnCalculate = findViewById(R.id.btn_calculate_target);
        btnSaveNote = findViewById(R.id.btn_save_note);

        // Setup Spinner
        List<String> grades = new ArrayList<>();
        grades.add("A (8.5)"); grades.add("B+ (8.0)"); grades.add("B (7.0)");
        grades.add("C+ (6.5)"); grades.add("C (5.5)"); grades.add("D+ (5.0)"); grades.add("D (4.0)");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, grades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTarget.setAdapter(adapter);

        // Logic ẩn hiện GK
        edtCredits.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty() && Integer.parseInt(s.toString()) >= 4) {
                    inputLayoutTx3.setVisibility(View.VISIBLE);
                } else {
                    inputLayoutTx3.setVisibility(View.GONE);
                    edtTx3.setText("");
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Cài đặt gợi ý và khóa/mở khóa tín chỉ
        setupSubjectSuggestion();

        btnCalculate.setOnClickListener(v -> calculateRequiredScore());
        btnSaveNote.setOnClickListener(v -> saveResultAsNote());
    }

    // --- HÀM XỬ LÝ GỢI Ý VÀ KHÓA Ô NHẬP ---
    private void setupSubjectSuggestion() {
        Set<String> subjectNames = new HashSet<>();
        for (Subject sub : allSubjectsList) if (sub.getName() != null) subjectNames.add(sub.getName());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>(subjectNames));
        edtSubjectName.setAdapter(adapter);

        // 1. Xử lý khi người dùng CLICK chọn từ danh sách gợi ý
        edtSubjectName.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);

            for (Subject sub : allSubjectsList) {
                if (sub.getName().equalsIgnoreCase(selectedName)) {
                    // Tìm thấy môn: Điền tín chỉ và KHÓA lại
                    edtCredits.setText(String.valueOf(sub.getCredits()));
                    edtCredits.setEnabled(false);

                    // Cảnh báo nếu môn đã học xong
                    if (!sub.isStudying()) {
                        Toast.makeText(this, "Môn này đã có điểm tổng kết: " + sub.getScore10(), Toast.LENGTH_LONG).show();
                    } else {
                        tvResult.setText("---");
                        tvResult.setTextColor(getResources().getColor(R.color.colorSecondary)); // Hoặc màu mặc định
                    }
                    break;
                }
            }
        });

        // 2. Xử lý khi người dùng GÕ phím (Thay đổi tên)
        edtSubjectName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String inputName = s.toString().trim();
                boolean isExist = false;

                // Kiểm tra xem tên đang gõ có khớp với môn nào trong DB không
                for (Subject sub : allSubjectsList) {
                    if (sub.getName().equalsIgnoreCase(inputName)) {
                        isExist = true;
                        // Nếu khớp (do gõ tay đúng): Điền tín chỉ và KHÓA
                        if(edtCredits.isEnabled()) { // Chỉ điền nếu chưa điền
                            edtCredits.setText(String.valueOf(sub.getCredits()));
                            edtCredits.setEnabled(false);
                        }
                        break;
                    }
                }

                // Nếu tên KHÔNG tồn tại trong DB -> MỞ KHÓA để nhập mới
                if (!isExist) {
                    edtCredits.setEnabled(true);
                    // Xóa cảnh báo cũ nếu có
                    if (tvResult.getText().toString().contains("Môn đã học xong")) {
                        tvResult.setText("---");
                    }
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private double getValidScore(EditText edt, String name) throws Exception {
        String txt = edt.getText().toString().trim();
        if(txt.isEmpty()) throw new Exception("Chưa nhập điểm " + name);
        double val = Double.parseDouble(txt);
        if(val < 0 || val > 10) throw new Exception(name + " phải từ 0-10");
        return val;
    }

    private double calculateRequiredScoreAndGetData(String coefficient) throws Exception {
        double targetZ = 8.5;
        int pos = spinnerTarget.getSelectedItemPosition();
        if(pos==1) targetZ=8.0; else if(pos==2) targetZ=7.0; else if(pos==3) targetZ=6.5;
        else if(pos==4) targetZ=5.5; else if(pos==5) targetZ=5.0; else if(pos==6) targetZ=4.0;
        String[] phan_tu = coefficient.split("-");
        int coefficient_tx1 = Integer.parseInt(phan_tu[0])/10;
        int coefficient_tx2 = Integer.parseInt(phan_tu[1])/10;
        double tx1 = getValidScore(edtTx1, "TX1");
        double tx2 = getValidScore(edtTx2, "TX2");

        double avgX;
        if (inputLayoutTx3.getVisibility() == View.VISIBLE) {
            double tx3 = getValidScore(edtTx3, "GK");
            avgX = (tx1 + tx2 + tx3) / 3;
        } else {
            avgX = tx1*coefficient_tx1+tx2*coefficient_tx2;
        }

        double requiredY = (targetZ*10-avgX)/(10-coefficient_tx1-coefficient_tx2);
        return requiredY;
    }

    private void calculateRequiredScore() {
        try {
            String creditsStr = edtCredits.getText().toString().trim();
            if (creditsStr.isEmpty()) throw new Exception("Chưa nhập số tín chỉ!");
            int credits = Integer.parseInt(creditsStr);

            String subjectName = edtSubjectName.getText().toString().trim();
            String gradeTarget = spinnerTarget.getSelectedItem().toString();

            if (subjectName.isEmpty()) throw new Exception("Nhập tên môn học!");
            if(edtCredits.getText().toString().isEmpty()) throw new Exception("Nhập tín chỉ!");
            double requiredY = calculateRequiredScoreAndGetData("10-20");
            Subject existingSubject = null;
            for (Subject s : allSubjectsList) {
                if (s.getName().equalsIgnoreCase(subjectName)) {
                    existingSubject = s;
                    break;
                }
            }
            if(existingSubject!=null) {
                requiredY = calculateRequiredScoreAndGetData(existingSubject.getCoefficient());
            }else{
                Toast.makeText(this, "Môn học chưa có đang tính điểm hệ sô TX1-TX2 là 10-20" + subjectName, Toast.LENGTH_SHORT).show();
            }

            if (requiredY > 10) {
                tvResult.setText("Cần thi: " + String.format("%.1f", requiredY) + "\n(Quá sức!)");
                tvResult.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else if (requiredY <= 0) {
                tvResult.setText("Đã đạt mục tiêu!");
                tvResult.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvResult.setText(String.format("%.1f", requiredY));
                tvResult.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ... (Các phần khác giữ nguyên)

    private void saveResultAsNote() {
        try {
            // 1. Kiểm tra dữ liệu đầu vào
            String creditsStr = edtCredits.getText().toString().trim();
            if (creditsStr.isEmpty()) throw new Exception("Chưa nhập số tín chỉ!");
            int credits = Integer.parseInt(creditsStr);

            String subjectName = edtSubjectName.getText().toString().trim();
            String gradeTarget = spinnerTarget.getSelectedItem().toString();

            if (subjectName.isEmpty()) throw new Exception("Nhập tên môn học!");

            // Tính toán kết quả
            ;
            double requiredY = calculateRequiredScoreAndGetData("10-20");

            // 2. TÌM HOẶC TẠO MÔN HỌC (QUAN TRỌNG)
            String targetSubjectCode = ""; // Mã môn để liên kết

            // Kiểm tra xem môn này đã có trong DB chưa
            Subject existingSubject = null;
            for (Subject s : allSubjectsList) {
                if (s.getName().equalsIgnoreCase(subjectName)) {
                    existingSubject = s;
                    break;
                }
            }

            if (existingSubject != null) {
                requiredY = calculateRequiredScoreAndGetData(existingSubject.getCoefficient());

                // Còn nếu muốn chặn:
                if (!existingSubject.isStudying()) {
                    Toast.makeText(this, "Lưu ý: Môn này đã có điểm tổng kết rồi!", Toast.LENGTH_SHORT).show();
                }
            } else {
                // --- TRƯỜNG HỢP 2: MÔN CHƯA CÓ -> TẠO MỚI ---
                // Chúng ta cần tự tạo mã UUID ở đây để gán cho cả Subject và Note
                // Thay vì để DB tự sinh, ta sinh thủ công bằng hàm trong Java để lấy được mã ngay lập tức
                targetSubjectCode = "MH_" + java.util.UUID.randomUUID().toString();

                // Tạo môn mới với mã vừa sinh
                Subject newSubject = new Subject(0, targetSubjectCode, subjectName,"10-20", credits, -1.0, 1);
                databaseHelper.addSubject(newSubject);

                Toast.makeText(this, "Đã tạo môn học mới: " + subjectName, Toast.LENGTH_SHORT).show();
            }

            // 3. LƯU GHI CHÚ (CÓ LIÊN KẾT subjectCode)
            SimpleDateFormat sdfCard = new SimpleDateFormat("dd/MM - HH:mm", new Locale("vi", "VN"));
            String currentTimeCard = sdfCard.format(new Date());

            String tx1 = edtTx1.getText().toString();
            String tx2 = edtTx2.getText().toString();
            String detailPoints = "• TX1: " + tx1 + "\n• TX2: " + tx2 + "\n";

            if (inputLayoutTx3.getVisibility() == View.VISIBLE) {
                detailPoints += "• GK: " + edtTx3.getText() + "\n";
            }

            String title = "🎯 Dự tính: " + subjectName + " (" + gradeTarget + ")";
            String content = "Môn học: " + subjectName + "\n"
                    + "\n--- ĐIỂM ĐÃ CÓ ---\n" + detailPoints
                    + "\n--- MỤC TIÊU ---\n"
                    + "• Cần điểm thi: " + String.format("%.1f", requiredY) + "\n"
                    + "• Đánh giá: " + (requiredY > 10 ? "Vô vọng 😭" : (requiredY <= 0 ? "Đã đạt ✅" : "Cần cố gắng ✏️"));

            // Lưu Note với mã môn vừa tìm được/tạo mới
            Note newNote = new Note(0, "", title, content, "", currentTimeCard, 0, targetSubjectCode);
            databaseHelper.addNote(newNote);

            Toast.makeText(this, "Đã lưu và liên kết với môn học!", Toast.LENGTH_SHORT).show();
            finish();

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}