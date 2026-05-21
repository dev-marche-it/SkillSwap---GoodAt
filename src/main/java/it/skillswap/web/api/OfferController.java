package it.skillswap.web.api;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.skillswap.domain.Offer;
import it.skillswap.domain.Skill;
import it.skillswap.domain.SkillLevel;
import it.skillswap.domain.Student;
import it.skillswap.web.ApplicationState;
import it.skillswap.web.api.dto.OfferDto;

@RestController
@RequestMapping("/api/offers")
public class OfferController {

    private final ApplicationState app;

    public OfferController(ApplicationState app) {
        this.app = app;
    }

    @GetMapping
    public List<OfferDto> list(
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) Boolean active) {
        return app.getState().getOffers().stream()
                .filter(o -> studentId == null || o.getStudent().getStudentId().equals(studentId))
                .filter(o -> active == null || o.isActive() == active)
                .map(OfferDto::from)
                .toList();
    }

    @PostMapping
    public OfferDto create(@RequestBody Map<String, String> body) {
        Student student = findStudent(required(body, "studentId"));
        Skill skill = findSkill(required(body, "skillId"));
        SkillLevel level = SkillLevel.fromString(body.get("level"));
        String note = body.getOrDefault("note", "");
        String id = IdGenerator.nextOfferId(app.getState());
        Offer offer = new Offer(id, student, skill, level, note);
        app.getState().getOffers().add(offer);
        app.persist();
        return OfferDto.from(offer);
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
