package com.example.sotaysv_nhom2;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.sotaysv_nhom2.Models.Note;
import com.example.sotaysv_nhom2.SQLlite.DatabaseHelper;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NoteActivity extends AppCompatActivity {

    private EditText edtTitle, edtContent;
    private MaterialSwitch swAlarm;
    private LinearLayout layoutAlarm, layoutWeekCheckboxes;
    private RadioGroup radioGroup;
    private RadioButton radioDaily, radioOneTime, radioWeekly;
    private CheckBox cbMon, cbTue, cbWed, cbThu, cbFri, cbSat, cbSun;
    private TextView tvPickDate, tvPickTime;
    private ExtendedFloatingActionButton fabSave;

    private Calendar selectedCalendar = Calendar.getInstance();
    private DatabaseHelper databaseHelper;
    private boolean isUpdateMode = false;
    private int noteIdToUpdate = 0;
    private String currentSubjectCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        Toolbar toolbar = findViewById(R.id.toolbar_add_note);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        databaseHelper = new DatabaseHelper(this);

        edtTitle = findViewById(R.id.edt_note_title);
        edtContent = findViewById(R.id.edt_note_content);
        swAlarm = findViewById(R.id.sw_alarm);
        layoutAlarm = findViewById(R.id.layout_alarm_settings);

        radioGroup = findViewById(R.id.radio_group_repeat);
        radioOneTime = findViewById(R.id.radio_one_time);
        radioDaily = findViewById(R.id.radio_daily);
        radioWeekly = findViewById(R.id.radio_weekly);

        layoutWeekCheckboxes = findViewById(R.id.layout_week_checkboxes);
        cbMon = findViewById(R.id.cb_mon); cbTue = findViewById(R.id.cb_tue);
        cbWed = findViewById(R.id.cb_wed); cbThu = findViewById(R.id.cb_thu);
        cbFri = findViewById(R.id.cb_fri); cbSat = findViewById(R.id.cb_sat);
        cbSun = findViewById(R.id.cb_sun);

        tvPickDate = findViewById(R.id.tv_pick_date);
        tvPickTime = findViewById(R.id.tv_pick_time);
        fabSave = findViewById(R.id.fab_save);

        checkIntentData();

        swAlarm.setOnCheckedChangeListener((v, isChecked) -> {
            layoutAlarm.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if(isChecked && !isUpdateMode) selectedCalendar = Calendar.getInstance();
        });

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_daily) {
                tvPickDate.setVisibility(View.GONE);
                layoutWeekCheckboxes.setVisibility(View.GONE);
            } else if (checkedId == R.id.radio_weekly) {
                tvPickDate.setVisibility(View.GONE);
                layoutWeekCheckboxes.setVisibility(View.VISIBLE);
            } else {
                tvPickDate.setVisibility(View.VISIBLE);
                layoutWeekCheckboxes.setVisibility(View.GONE);
            }
        });

        tvPickDate.setOnClickListener(v -> {
            int year = selectedCalendar.get(Calendar.YEAR);
            int month = selectedCalendar.get(Calendar.MONTH);
            int day = selectedCalendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dpd = new DatePickerDialog(this, (view, y, m, d) -> {
                selectedCalendar.set(Calendar.YEAR, y); selectedCalendar.set(Calendar.MONTH, m); selectedCalendar.set(Calendar.DAY_OF_MONTH, d);
                tvPickDate.setText(d + "/" + (m + 1) + "/" + y);
            }, year, month, day);
            // Chặn chọn ngày quá khứ
            dpd.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dpd.show();
        });

        tvPickTime.setOnClickListener(v -> {
            int hour = selectedCalendar.get(Calendar.HOUR_OF_DAY);
            int minute = selectedCalendar.get(Calendar.MINUTE);
            new TimePickerDialog(this, (view, h, m) -> {
                selectedCalendar.set(Calendar.HOUR_OF_DAY, h); selectedCalendar.set(Calendar.MINUTE, m);
                tvPickTime.setText(String.format("%02d:%02d", h, m));
            }, hour, minute, true).show();
        });

        fabSave.setOnClickListener(v -> saveNote());
    }

    private void checkIntentData() {
        Intent intent = getIntent();
        String prefillTitle = intent.getStringExtra("PREFILL_TITLE");
        if (intent.hasExtra("SUBJECT_CODE")) currentSubjectCode = intent.getStringExtra("SUBJECT_CODE");
        if (prefillTitle != null && !prefillTitle.isEmpty()) edtTitle.setText(prefillTitle);

        if (intent.getBooleanExtra("IS_UPDATE", false)) {
            isUpdateMode = true;
            noteIdToUpdate = intent.getIntExtra("ID", 0);
            if(getSupportActionBar()!=null) getSupportActionBar().setTitle("Cập nhật ghi chú");
            fabSave.setText("Cập nhật");

            edtTitle.setText(intent.getStringExtra("TITLE"));
            edtContent.setText(intent.getStringExtra("CONTENT"));

            String alarmTime = intent.getStringExtra("ALARM_TIME");
            int repeatType = intent.getIntExtra("REPEAT_TYPE", 0);

            if (alarmTime != null && !alarmTime.isEmpty()) {
                swAlarm.setChecked(true);
                layoutAlarm.setVisibility(View.VISIBLE);

                if (repeatType == 1) radioDaily.setChecked(true);
                else if (repeatType == 2) {
                    radioWeekly.setChecked(true);
                    layoutWeekCheckboxes.setVisibility(View.VISIBLE);
                    tvPickDate.setVisibility(View.GONE);
                    if(alarmTime.contains("T2")) cbMon.setChecked(true);
                    if(alarmTime.contains("T3")) cbTue.setChecked(true);
                    if(alarmTime.contains("T4")) cbWed.setChecked(true);
                    if(alarmTime.contains("T5")) cbThu.setChecked(true);
                    if(alarmTime.contains("T6")) cbFri.setChecked(true);
                    if(alarmTime.contains("T7")) cbSat.setChecked(true);
                    if(alarmTime.contains("CN")) cbSun.setChecked(true);
                }
                else radioOneTime.setChecked(true);

                if (alarmTime.contains(" - ")) {
                    String[] parts = alarmTime.split(" - ");
                    if (parts.length > 1) {
                        if(repeatType != 1) tvPickDate.setText(parts[0]);
                        tvPickTime.setText(parts[1]);
                    }
                }
            }
        }
    }

    private void saveNote() {
        String title = edtTitle.getText().toString().trim();
        String content = edtContent.getText().toString().trim();
        if (title.isEmpty()) { Toast.makeText(this, "Nhập tiêu đề!", Toast.LENGTH_SHORT).show(); return; }

        String alarmTimeStr = "";
        int repeatType = 0;
        int noteId = isUpdateMode ? noteIdToUpdate : (int) (System.currentTimeMillis() % 100000);

        if (isUpdateMode) cancelAllAlarms(noteId);

        if (swAlarm.isChecked()) {
            String timeDisplay = tvPickTime.getText().toString();

            // Chỉ xử lý nếu ĐÃ CHỌN GIỜ
            if (!timeDisplay.toLowerCase().contains("chọn")) {
                String timeStr = timeDisplay;

                if (radioDaily.isChecked()) {
                    repeatType = 1;
                    alarmTimeStr = "Hàng ngày - " + timeStr;
                    scheduleNotification(noteId, title, content, selectedCalendar, 1);
                }
                else if (radioWeekly.isChecked()) {
                    repeatType = 2;
                    StringBuilder days = new StringBuilder();
                    if(cbMon.isChecked()) { days.append("T2, "); scheduleWeeklyAlarm(noteId, title, content, Calendar.MONDAY); }
                    if(cbTue.isChecked()) { days.append("T3, "); scheduleWeeklyAlarm(noteId, title, content, Calendar.TUESDAY); }
                    if(cbWed.isChecked()) { days.append("T4, "); scheduleWeeklyAlarm(noteId, title, content, Calendar.WEDNESDAY); }
                    if(cbThu.isChecked()) { days.append("T5, "); scheduleWeeklyAlarm(noteId, title, content, Calendar.THURSDAY); }
                    if(cbFri.isChecked()) { days.append("T6, "); scheduleWeeklyAlarm(noteId, title, content, Calendar.FRIDAY); }
                    if(cbSat.isChecked()) { days.append("T7, "); scheduleWeeklyAlarm(noteId, title, content, Calendar.SATURDAY); }
                    if(cbSun.isChecked()) { days.append("CN, "); scheduleWeeklyAlarm(noteId, title, content, Calendar.SUNDAY); }

                    if (days.length() > 0) {
                        if (days.length() > 2) days.setLength(days.length() - 2);
                        alarmTimeStr = days.toString() + " - " + timeStr;
                    }
                }
                else {
                    // --- MỘT LẦN ---
                    repeatType = 0;
                    String dateStr = tvPickDate.getText().toString();

                    // Chỉ đặt báo thức nếu ĐÃ CHỌN NGÀY
                    if (!dateStr.toLowerCase().contains("chọn")) {

                        // KIỂM TRA THỜI GIAN QUÁ KHỨ
                        if (selectedCalendar.getTimeInMillis() < System.currentTimeMillis()) {
                            Toast.makeText(this, "Thời gian đã qua! Vui lòng chọn lại.", Toast.LENGTH_LONG).show();
                            return; // Dừng lại ngay, không lưu
                        }

                        alarmTimeStr = dateStr + " - " + timeStr;
                        scheduleNotification(noteId, title, content, selectedCalendar, 0);
                    }
                }
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM - HH:mm", new Locale("vi", "VN"));
        String currentTime = sdf.format(new Date());

        Note note = new Note(noteId, "", title, content, alarmTimeStr, currentTime, repeatType, currentSubjectCode);

        if (isUpdateMode) databaseHelper.updateNote(note);
        else databaseHelper.addNote(note);

        Toast.makeText(this, "Đã lưu!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleNotification(int noteId, String title, String content, Calendar calendar, int repeatType) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("TITLE", title); intent.putExtra("CONTENT", content); intent.putExtra("ID", noteId);
        PendingIntent pi = PendingIntent.getBroadcast(this, noteId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Logic tự động tăng ngày: CHỈ ÁP DỤNG CHO HÀNG NGÀY (Daily)
        if (repeatType == 1 && calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }
        // Với "Một lần" (repeatType == 0), ta KHÔNG tự cộng ngày nữa
        // Vì ở hàm saveNote ta đã check quá khứ rồi.

        if (repeatType == 1) am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
                else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
            } else am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
        }
    }

    private void scheduleWeeklyAlarm(int noteId, String title, String content, int dayOfWeek) {
        int hour = selectedCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = selectedCalendar.get(Calendar.MINUTE);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int alarmId = noteId * 100 + dayOfWeek;
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("TITLE", title); intent.putExtra("CONTENT", content); intent.putExtra("ID", noteId);
        PendingIntent pi = PendingIntent.getBroadcast(this, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour); cal.set(Calendar.MINUTE, minute); cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        if (cal.before(Calendar.getInstance())) cal.add(Calendar.WEEK_OF_YEAR, 1);
        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pi);
    }

    private void cancelAllAlarms(int noteId) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, noteId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (am != null) am.cancel(pi);
        for(int i=1; i<=7; i++) {
            PendingIntent piW = PendingIntent.getBroadcast(this, noteId * 100 + i, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            if (am != null) am.cancel(piW);
        }
    }
}