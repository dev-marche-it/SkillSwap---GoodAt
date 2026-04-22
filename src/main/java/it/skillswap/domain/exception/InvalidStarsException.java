package it.skillswap.domain.exception;

/**
 * Eccezione lanciata quando il numero di stelle inserito non è valido (non compreso tra 1 e 5).
 */
public class InvalidStarsException extends SkillSwapException {
    public InvalidStarsException(int stars) {
        super("Il voto deve essere compreso tra 1 e 5, ricevuto: " + stars);
    }
}
