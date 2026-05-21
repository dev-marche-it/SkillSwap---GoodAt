package it.skillswap.web.api.dto;

import it.skillswap.domain.Offer;

public record OfferDto(
        String offerId,
        String studentId,
        String studentName,
        String skillId,
        String skillName,
        String level,
        String note,
        boolean active) {

    public static OfferDto from(Offer o) {
        return new OfferDto(
                o.getOfferId(),
                o.getStudent().getStudentId(),
                o.getStudent().getName(),
                o.getSkill().getSkillId(),
                o.getSkill().getName(),
                o.getLevel().name(),
                o.getNote(),
                o.isActive());
    }
}
