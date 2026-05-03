package it.skillswap.domain;

/**
 * Ciclo di vita di uno {@link Exchange}: proposto, accettato, completato o annullato.
 */
public enum ExchangeStatus {
    /** In attesa di accettazione da parte dell'altro studente. */
    PROPOSED,
    /** Accettato e in corso. */
    ACCEPTED,
    /** Completato con successo; si possono aggiungere recensioni. */
    COMPLETED,
    /** Ritirato mentre era ancora solo proposto. */
    CANCELLED
}
