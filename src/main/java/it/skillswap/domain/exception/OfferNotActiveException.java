package it.skillswap.domain.exception;

/**
 * Eccezione lanciata quando si tenta di proporre uno scambio con un'offerta non attiva.
 */
public class OfferNotActiveException extends SkillSwapException {
    public OfferNotActiveException(String offerId) {
        super("L'offerta non è più attiva: " + offerId);
    }
}
