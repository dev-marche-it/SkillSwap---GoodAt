package it.skillswap.service;

public class MatchResult {
    private String offerId;
    private String requestId;
    private int score;
    private String reason;

    public MatchResult(String offerId, String requestId, int score, String reason) {
        this.offerId = offerId;
        this.requestId = requestId;
        this.score = score;
        this.reason = reason;
    }

    public String getOfferId() { return offerId; }
    public String getRequestId() { return requestId; }
    public int getScore() { return score; }
    public String getReason() { return reason; }

    @Override
    public String toString() {
        return "MatchResult{" +
                "offerId='" + offerId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", score=" + score +
                ", reason='" + reason + '\'' +
                '}';
    }
}