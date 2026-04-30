package it.skillswap.domain;

/**
 * Represents a student participating in the SkillSwap platform.
 * Each student has immutable personal information and mutable rating metrics.
 */
public class Student {
    private final String studentId;
    private final String name;
    private final String className;
    private final String email;
    private double ratingAvg;
    private int ratingCount;

    /**
     * Constructs a Student with the specified details.
     *
     * @param studentId unique identifier for the student
     * @param name      full name of the student
     * @param className class or cohort name
     * @param email     email address for notifications
     */
    public Student(String studentId, String name, String className, String email) {
        this.studentId = studentId;
        this.name = name;
        this.className = className;
        this.email = email;
        this.ratingAvg = 0.0;
        this.ratingCount = 0;
    }

    /**
     * Adds a rating to the student's profile and updates the average.
     *
     * @param stars rating value (1-5)
     */
    public void addRating(int stars) {
        ratingAvg = ((ratingAvg * ratingCount) + stars) / (ratingCount + 1);
        ratingCount++;
    }

    /**
     * Gets the student's unique identifier.
     * @return the student ID
     */
    public String getStudentId() { return studentId; }
    
    /**
     * Gets the student's name.
     * @return the student name
     */
    public String getName() { return name; }
    
    /**
     * Gets the student's class name.
     * @return the class name
     */
    public String getClassName() { return className; }
    
    /**
     * Gets the student's email address.
     * @return the email address
     */
    public String getEmail() { return email; }
    
    /**
     * Gets the student's average rating.
     * @return average rating value
     */
    public double getRatingAvg() { return ratingAvg; }
    public int getRatingCount() { return ratingCount; }

    @Override
    public String toString() {
        return studentId + " - " + name + " (" + className + ")";
    }
}
