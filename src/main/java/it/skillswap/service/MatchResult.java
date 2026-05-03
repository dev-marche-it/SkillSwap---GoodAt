package it.skillswap.service;

/**
 * Risultato di abbinamento: id di un'{@link it.skillswap.domain.Offer} e di una {@link it.skillswap.domain.Request},
 * con punteggio e motivazione prodotti da {@link MatchingService}.
 */
public class MatchResult {
    private final String offerId;
    private final String requestId;
    private final int score;
    private final String reason;

    /**
     * @param offerId   id dell'offerta abbinata
     * @param requestId id della richiesta abbinata
     * @param score     punteggio di qualità del match (es. 0–6 per one-way)
     * @param reason    spiegazione testuale del punteggio
     */
    public MatchResult(String offerId, String requestId, int score, String reason) {
        this.offerId = offerId;
        this.requestId = requestId;
        this.score = score;
        this.reason = reason;
    }

    /** @return id dell'offerta */
    public String getOfferId() { return offerId; }

    /** @return id della richiesta */
    public String getRequestId() { return requestId; }

    /** @return punteggio di qualità */
    public int getScore() { return score; }

    /** @return motivazione del match */
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
