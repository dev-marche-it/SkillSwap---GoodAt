package it.skillswap.domain.exception;

/**
 * Lanciata quando il numero di stelle non è nell'intervallo consentito 1–5.
 */
public class InvalidStarsException extends SkillSwapException {
    /**
     * @param stars valore non valido fornito
     */
    public InvalidStarsException(int stars) {
        super("Il voto deve essere compreso tra 1 e 5, ricevuto: " + stars);
    }
}
