package it.skillswap.domain.exception;

/**
 * Eccezione lanciata quando si tenta di aggiungere una seconda recensione per lo stesso scambio.
 */
public class DuplicateReviewException extends SkillSwapException {
    public DuplicateReviewException(String exchangeId) {
        super("Una recensione per questo scambio esiste già: " + exchangeId);
    }
}
