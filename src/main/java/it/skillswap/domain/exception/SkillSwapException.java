package it.skillswap.domain.exception;

/**
 * Eccezione base per tutti gli errori di dominio di SkillSwap.
 * Unchecked exception che estende RuntimeException.
 */
public class SkillSwapException extends RuntimeException {
    public SkillSwapException(String message) {
        super(message);
    }

    public SkillSwapException(String message, Throwable cause) {
        super(message, cause);
    }
}
