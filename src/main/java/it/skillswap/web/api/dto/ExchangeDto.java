package it.skillswap.web.api.dto;

import it.skillswap.domain.Exchange;

public record ExchangeDto(
        String exchangeId,
        String offerId,
        String requestId,
        String status,
        String offerSummary,
        String requestSummary,
        String createdAt,
        String closedAt) {

    public static ExchangeDto from(Exchange e) {
        return new ExchangeDto(
                e.getExchangeId(),
                e.getOffer().getOfferId(),
                e.getRequest().getRequestId(),
                e.getStatus().name(),
                e.getOffer().toString(),
                e.getRequest().toString(),
                e.getCreatedAt() != null ? e.getCreatedAt().toString() : null,
                e.getClosedAt() != null ? e.getClosedAt().toString() : null);
    }
}
