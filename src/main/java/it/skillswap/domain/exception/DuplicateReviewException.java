package it.skillswap.domain.exception;

/**
 * Lanciata quando un recensore tenta una seconda recensione per lo stesso {@link it.skillswap.domain.Exchange}.
 */
public class DuplicateReviewException extends SkillSwapException {
    /**
     * @param exchangeId scambio per cui esiste già una recensione da questo recensore
     */
    public DuplicateReviewException(String exchangeId) {
        super("Una recensione per questo scambio esiste già: " + exchangeId);
    }
}
