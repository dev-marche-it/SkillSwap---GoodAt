package it.skillswap.service;

/**
 * Represents a match between an Offer and a Request with quality metrics.
 */
public class MatchResult {
    private final String offerId;
    private final String requestId;
    private final int score;
    private final String reason;

    /**
     * Constructs a MatchResult with the specified details.
     *
     * @param offerId   ID of the matched offer
     * @param requestId ID of the matched request
     * @param score     quality score of the match (0-6)
     * @param reason    explanation of why this is a good match
     */
    public MatchResult(String offerId, String requestId, int score, String reason) {
        this.offerId = offerId;
        this.requestId = requestId;
        this.score = score;
        this.reason = reason;
    }

    /**
     * Gets the offer ID from this match.
     * @return the offer ID
     */
    public String getOfferId() { return offerId; }
    
    /**
     * Gets the request ID from this match.
     * @return the request ID
     */
    public String getRequestId() { return requestId; }
    
    /**
     * Gets the quality score of this match.
     * @return the score
     */
    public int getScore() { return score; }
    
    /**
     * Gets the reason for this match.
     * @return the reason string
     */
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
