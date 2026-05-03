package it.skillswap.domain.exception;

/**
 * Eccezione non controllata di base per tutti gli errori di dominio SkillSwap.
 */
public class SkillSwapException extends RuntimeException {
    /**
     * @param message descrizione leggibile dell'errore
     */
    public SkillSwapException(String message) {
        super(message);
    }

    /**
     * @param message descrizione leggibile dell'errore
     * @param cause   causa sottostante
     */
    public SkillSwapException(String message, Throwable cause) {
        super(message, cause);
    }
}
