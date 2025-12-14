package com.example.sotaysv_nhom2.Models;

public class Subject {
    private int id;
    private String code; // Mã tự sinh (UUID)
    private String name;
    private String coefficient;
    private int credits;
    private double score10;
    private int semester;

    // Constructor đầy đủ (6 tham số)
    public Subject(int id, String code, String name ,String coefficient, int credits, double score10, int semester) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.coefficient = coefficient;
        this.credits = credits;
        this.score10 = score10;
        this.semester = semester;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setCoefficient(String coefficient) {
        this.coefficient = coefficient;
    }
    public void setCredits(int credits) {
        this.credits = credits;
    }

    public void setScore10(double score10) {
        this.score10 = score10;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public int getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getCoefficient() { return coefficient; }
    public int getCredits() { return credits; }
    public double getScore10() { return score10; }
    public int getSemester() { return semester; }

    public boolean isStudying() { return score10 < 0; }
}