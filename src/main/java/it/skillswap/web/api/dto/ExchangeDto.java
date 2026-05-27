package it.skillswap.web.api.dto;

import it.skillswap.domain.Exchange;
import it.skillswap.domain.ExchangeStatus;
import it.skillswap.domain.SkillSwapState;

public record ExchangeDto(
        String exchangeId,
        String offerId,
        String requestId,
        String status,
        String offerSummary,
        String requestSummary,
        String offerStudentId,
        String offerStudentName,
        String requestStudentId,
        String requestStudentName,
        String offerSkillName,
        String requestSkillName,
        String createdAt,
        String closedAt,
        boolean canAccept,
        boolean canCancel,
        boolean canComplete,
        boolean canReview) {

    public static ExchangeDto from(Exchange e, String viewerStudentId, SkillSwapState state) {
        String offerStudentId = e.getOffer().getStudent().getStudentId();
        String requestStudentId = e.getRequest().getStudent().getStudentId();
        ExchangeStatus status = e.getStatus();

        boolean isOfferOwner = offerStudentId.equals(viewerStudentId);
        boolean isRequestOwner = requestStudentId.equals(viewerStudentId);
        boolean isParticipant = isOfferOwner || isRequestOwner;

        boolean alreadyReviewed = state != null
                && viewerStudentId != null
                && !viewerStudentId.isBlank()
                && state.getReviews().stream()
                        .anyMatch(r -> r.getExchange().getExchangeId().equals(e.getExchangeId())
                                && r.getReviewer().getStudentId().equals(viewerStudentId));

        boolean canAccept = status == ExchangeStatus.PROPOSED && isOfferOwner;
        boolean canCancel = status == ExchangeStatus.PROPOSED && isParticipant;
        boolean canComplete = status == ExchangeStatus.ACCEPTED && isParticipant;
        boolean canReview = status == ExchangeStatus.COMPLETED && isParticipant && !alreadyReviewed;

        return new ExchangeDto(
                e.getExchangeId(),
                e.getOffer().getOfferId(),
                e.getRequest().getRequestId(),
                status.name(),
                e.getOffer().toString(),
                e.getRequest().toString(),
                offerStudentId,
                e.getOffer().getStudent().getName(),
                requestStudentId,
                e.getRequest().getStudent().getName(),
                e.getOffer().getSkill().getName(),
                e.getRequest().getSkill().getName(),
                e.getCreatedAt() != null ? e.getCreatedAt().toString() : null,
                e.getClosedAt() != null ? e.getClosedAt().toString() : null,
                canAccept,
                canCancel,
                canComplete,
                canReview);
    }

    public static ExchangeDto from(Exchange e, String viewerStudentId) {
        return from(e, viewerStudentId, null);
    }

    /** Senza permessi (dettaglio pubblico). */
    public static ExchangeDto from(Exchange e) {
        return from(e, "", null);
    }
}
