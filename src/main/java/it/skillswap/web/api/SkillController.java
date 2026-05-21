package it.skillswap.web.api;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.skillswap.domain.Skill;
import it.skillswap.domain.SkillCategory;
import it.skillswap.web.ApplicationState;
import it.skillswap.web.api.dto.SkillDto;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final ApplicationState app;

    public SkillController(ApplicationState app) {
        this.app = app;
    }

    @GetMapping
    public List<SkillDto> list() {
        return app.getState().getSkills().stream().map(SkillDto::from).toList();
    }

    @PostMapping
    public SkillDto create(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "").trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Nome skill obbligatorio");
        }
        SkillCategory category = SkillCategory.fromString(body.get("category"));
        String id = IdGenerator.nextSkillId(app.getState());
        Skill skill = new Skill(id, name, category);
        app.getState().getSkills().add(skill);
        app.persist();
        return SkillDto.from(skill);
    }
}
