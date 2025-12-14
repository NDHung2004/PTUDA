package com.example.sotaysv_nhom2.SQLlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.sotaysv_nhom2.Models.Note;
import com.example.sotaysv_nhom2.Models.Subject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SoTaySinhVien_Final.db"; // Đổi tên DB để reset sạch sẽ
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_SUBJECT = "subjects";
    private static final String TABLE_NOTE = "notes";

    public DatabaseHelper(Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_SUBJECT + "(id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT, name TEXT, coefficient TEXT, credits INTEGER, score REAL, semester INTEGER)");
        db.execSQL("CREATE TABLE " + TABLE_NOTE + "(id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT, title TEXT, content TEXT, alarm_time TEXT, date_time TEXT, repeat_type INTEGER, subject_code TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBJECT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTE);
        onCreate(db);
    }

    private String generateUUID(String prefix) { return prefix + "_" + UUID.randomUUID().toString(); }

    // --- SUBJECT ---
    public void addSubject(Subject subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        String code = (subject.getCode() == null || subject.getCode().isEmpty()) ? generateUUID("MH") : subject.getCode();
        values.put("code", code);
        values.put("name", subject.getName());
        values.put("coefficient",subject.getCoefficient());
        values.put("credits", subject.getCredits());
        values.put("score", subject.getScore10());
        values.put("semester", subject.getSemester());
        db.insert(TABLE_SUBJECT, null, values);
        db.close();
    }

    public void deleteSubject(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        String subjectCode = "";
        Cursor cursor = db.rawQuery("SELECT code FROM " + TABLE_SUBJECT + " WHERE id = ?", new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            subjectCode = cursor.getString(0);
        }
        cursor.close();

        if (subjectCode != null && !subjectCode.isEmpty()) {
            db.delete(TABLE_NOTE, "subject_code = ?", new String[]{subjectCode});
        }

        db.delete(TABLE_SUBJECT, "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void updateSubject(Subject subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", subject.getName());
        values.put("coefficient",subject.getCoefficient());
        values.put("credits", subject.getCredits());
        values.put("score", subject.getScore10());
        values.put("semester", subject.getSemester());
        db.update(TABLE_SUBJECT, values, "id = ?", new String[]{String.valueOf(subject.getId())});
        db.close();
    }

    public List<Subject> getAllSubjects() {
        List<Subject> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SUBJECT + " ORDER BY semester ASC", null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new Subject(cursor.getInt(0), cursor.getString(1), cursor.getString(2),cursor.getString(3), cursor.getInt(4), cursor.getDouble(5), cursor.getInt(6)));
            } while (cursor.moveToNext());
        }
        cursor.close(); db.close(); return list;
    }

    public void clearAllSubjects() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SUBJECT, null, null);
        db.close();
    }

    // --- NOTE ---
    public void addNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        String code = (note.getCode() == null || note.getCode().isEmpty()) ? generateUUID("NT") : note.getCode();
        values.put("code", code);
        values.put("title", note.getTitle());
        values.put("content", note.getContent());
        values.put("alarm_time", note.getAlarmTime());
        values.put("date_time", note.getDateTime());
        values.put("repeat_type", note.getRepeatType());
        values.put("subject_code", note.getSubjectCode());
        db.insert(TABLE_NOTE, null, values);
        db.close();
    }

    public void updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", note.getTitle());
        values.put("content", note.getContent());
        values.put("alarm_time", note.getAlarmTime());
        values.put("date_time", note.getDateTime());
        values.put("repeat_type", note.getRepeatType());
        values.put("subject_code", note.getSubjectCode());
        db.update(TABLE_NOTE, values, "id = ?", new String[]{String.valueOf(note.getId())});
        db.close();
    }

    public void deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTE, "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<Note> getAllNotes() {
        List<Note> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NOTE + " ORDER BY id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new Note(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getInt(6), cursor.getString(7)));
            } while (cursor.moveToNext());
        }
        cursor.close(); db.close(); return list;
    }
}