package it.skillswap.domain.exception;

/**
 * Lanciata quando si propone uno scambio usando un'{@link it.skillswap.domain.Offer} non attiva.
 */
public class OfferNotActiveException extends SkillSwapException {
    /**
     * @param offerId identificativo dell'offerta non attiva
     */
    public OfferNotActiveException(String offerId) {
        super("L'offerta non è più attiva: " + offerId);
    }
}
