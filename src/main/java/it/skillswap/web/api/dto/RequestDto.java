package it.skillswap.web.api.dto;

import it.skillswap.domain.Request;

public record RequestDto(
        String requestId,
        String studentId,
        String studentName,
        String skillId,
        String skillName,
        String minLevel,
        String note) {

    public static RequestDto from(Request r) {
        return new RequestDto(
                r.getRequestId(),
                r.getStudent().getStudentId(),
                r.getStudent().getName(),
                r.getSkill().getSkillId(),
                r.getSkill().getName(),
                r.getMinLevel().name(),
                r.getNote());
    }
}
