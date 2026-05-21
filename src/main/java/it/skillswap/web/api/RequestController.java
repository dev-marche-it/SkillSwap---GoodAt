package it.skillswap.web.api;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.skillswap.domain.Request;
import it.skillswap.domain.Skill;
import it.skillswap.domain.SkillLevel;
import it.skillswap.domain.Student;
import it.skillswap.web.ApplicationState;
import it.skillswap.web.api.dto.RequestDto;

@RestController
@RequestMapping("/api/requests")
public class RequestController {

    private final ApplicationState app;

    public RequestController(ApplicationState app) {
        this.app = app;
    }

    @GetMapping
    public List<RequestDto> list(@RequestParam(required = false) String studentId) {
        return app.getState().getRequests().stream()
                .filter(r -> studentId == null || r.getStudent().getStudentId().equals(studentId))
                .map(RequestDto::from)
                .toList();
    }

    @PostMapping
    public RequestDto create(@RequestBody Map<String, String> body) {
        Student student = findStudent(required(body, "studentId"));
        Skill skill = findSkill(required(body, "skillId"));
        SkillLevel minLevel = SkillLevel.fromString(body.get("minLevel"));
        String note = body.getOrDefault("note", "");
        String id = IdGenerator.nextRequestId(app.getState());
        Request request = new Request(id, student, skill, minLevel, note);
        app.getState().getRequests().add(request);
        app.persist();
        return RequestDto.from(request);
    }

    private Student findStudent(String id) {
        return app.getState().getStudents().stream()
                .filter(s -> s.getStudentId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Studente non trovato: " + id));
    }

    private Skill findSkill(String id) {
        return app.getState().getSkills().stream()
                .filter(s -> s.getSkillId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Skill non trovata: " + id));
    }

    private static String required(Map<String, String> body, String key) {
        String v = body.getOrDefault(key, "").trim();
        if (v.isEmpty()) {
            throw new IllegalArgumentException("Campo obbligatorio: " + key);
        }
        return v;
    }
}
