package it.skillswap.domain;

public class Student {
    private String studentId;
    private String name;
    private String className;
    private String email;
    private double ratingAvg;
    private int ratingCount;

    public Student(String studentId, String name, String className, String email) {
        this.studentId = studentId;
        this.name = name;
        this.className = className;
        this.email = email;
        this.ratingAvg = 0.0;
        this.ratingCount = 0;
    }

    // Aggiorna la media quando arriva una nuova recensione
    public void addRating(int stars) {
        ratingAvg = ((ratingAvg * ratingCount) + stars) / (ratingCount + 1);
        ratingCount++;
    }

    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getClassName() { return className; }
    public String getEmail() { return email; }
    public double getRatingAvg() { return ratingAvg; }
    public int getRatingCount() { return ratingCount; }

    @Override
    public String toString() {
        return studentId + " - " + name + " (" + className + ")";
    }
}