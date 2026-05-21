package it.skillswap.web.api.dto;

import it.skillswap.service.MatchResult;

public record MatchResultDto(String offerId, String requestId, int score, String reason) {

    public static MatchResultDto from(MatchResult m) {
        return new MatchResultDto(m.getOfferId(), m.getRequestId(), m.getScore(), m.getReason());
    }
}
