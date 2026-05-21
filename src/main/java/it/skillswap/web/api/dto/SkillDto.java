package it.skillswap.web.api.dto;

import it.skillswap.domain.Skill;

public record SkillDto(String skillId, String name, String category) {

    public static SkillDto from(Skill s) {
        return new SkillDto(s.getSkillId(), s.getName(), s.getCategory().name());
    }
}
