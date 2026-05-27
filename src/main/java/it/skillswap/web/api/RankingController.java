package it.skillswap.web.api;

import java.util.Comparator;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.skillswap.domain.Student;
import it.skillswap.web.ApplicationState;
import it.skillswap.web.api.dto.StudentDto;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    private final ApplicationState app;

    public RankingController(ApplicationState app) {
        this.app = app;
    }

    @GetMapping
    public List<StudentDto> ranking() {
        return app.getState().getStudents().stream()
                .filter(s -> s.getRatingCount() > 0)
                .sorted(Comparator.comparingDouble(Student::getRatingAvg).reversed())
                .map(StudentDto::from)
                .toList();
    }
}
