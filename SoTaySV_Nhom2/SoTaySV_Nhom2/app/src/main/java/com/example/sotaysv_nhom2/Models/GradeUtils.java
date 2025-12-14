package com.example.sotaysv_nhom2.Models;

import java.util.List;

public class GradeUtils {
    public static double calculateSubjectScoreHaUI(double processScore, double finalExamScore) {
        double z = (processScore + (finalExamScore * 2)) / 3;
        return (double) Math.round(z * 10) / 10;
    }

    public static String convertToLetter(double score10) {
        if (score10 >= 8.5) return "A";
        if (score10 >= 8.0) return "B+";
        if (score10 >= 7.0) return "B";
        if (score10 >= 6.5) return "C+";
        if (score10 >= 5.5) return "C";
        if (score10 >= 5.0) return "D+";
        if (score10 >= 4.0) return "D";
        if (score10 < 0) return "---";
        return "F";
    }

    public static double convertToScale4(double score10) {
        if (score10 >= 8.5) return 4.0;
        if (score10 >= 8.0) return 3.5;
        if (score10 >= 7.0) return 3.0;
        if (score10 >= 6.5) return 2.5;
        if (score10 >= 5.5) return 2.0;
        if (score10 >= 5.0) return 1.5;
        if (score10 >= 4.0) return 1.0;
        return 0.0;
    }

    public static double calculateGPA(List<Subject> subjects) {
        double totalPoints = 0;
        int totalCredits = 0;
        for (Subject sub : subjects) {
            double scale4 = convertToScale4(sub.getScore10());
            totalPoints += (scale4 * sub.getCredits());
            totalCredits += sub.getCredits();
        }
        if (totalCredits == 0) return 0.0;
        return (double) Math.round((totalPoints / totalCredits) * 100) / 100;
    }
}