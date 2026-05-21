package it.skillswap.web.api.dto;

import it.skillswap.domain.Student;

public record StudentDto(
        String studentId,
        String name,
        String className,
        String email,
        double ratingAvg,
        int ratingCount) {

    public static StudentDto from(Student s) {
        return new StudentDto(
                s.getStudentId(),
                s.getName(),
                s.getClassName(),
                s.getEmail(),
                s.getRatingAvg(),
                s.getRatingCount());
    }
}
