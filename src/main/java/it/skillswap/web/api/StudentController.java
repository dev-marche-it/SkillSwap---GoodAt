package it.skillswap.web.api;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.skillswap.domain.Student;
import it.skillswap.web.ApplicationState;
import it.skillswap.web.api.dto.ReviewDto;
import it.skillswap.web.api.dto.StudentDto;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final ApplicationState app;

    public StudentController(ApplicationState app) {
        this.app = app;
    }

    @GetMapping
    public List<StudentDto> list() {
        return app.getState().getStudents().stream().map(StudentDto::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentDto> get(@PathVariable String id) {
        return findStudent(id)
                .map(StudentDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<ReviewDto>> reviews(@PathVariable String id) {
        if (findStudent(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<ReviewDto> list = app.getReviewService().getReviewsForStudent(id).stream()
                .map(ReviewDto::from)
                .toList();
        return ResponseEntity.ok(list);
    }

    private java.util.Optional<Student> findStudent(String id) {
        return app.getState().getStudents().stream()
                .filter(s -> s.getStudentId().equals(id))
                .findFirst();
    }

}
