package it.skillswap.service;

/**
 * Incapsula l'esito di un'operazione di validazione.
 * Consente validazione non bloccante con messaggio descrittivo.
 */
public class ValidationResult {
    private final boolean valid;
    private final String message;

    /**
     * Costruisce un risultato con stato e messaggio.
     *
     * @param valid   {@code true} se la validazione è passata
     * @param message messaggio che descrive l'esito
     */
    public ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    /**
     * @return esito positivo senza messaggio
     */
    public static ValidationResult success() {
        return new ValidationResult(true, "");
    }

    /**
     * @param message spiegazione del fallimento
     * @return esito negativo con il messaggio indicato
     */
    public static ValidationResult failure(String message) {
        return new ValidationResult(false, message);
    }

    /**
     * @return {@code true} se la validazione è passata
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * @return messaggio descrittivo (vuoto in caso di successo da {@link #success()})
     */
    public String getMessage() {
        return message;
    }
}
