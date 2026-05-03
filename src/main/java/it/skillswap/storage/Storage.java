package it.skillswap.storage;

import it.skillswap.domain.SkillSwapState;

/**
 * Porta di persistenza per {@link SkillSwapState}: caricamento all'avvio, salvataggio dopo le modifiche.
 */
public interface Storage {
    /**
     * Legge lo stato applicativo completo dal backend di persistenza.
     *
     * @return {@link SkillSwapState} nuovo o reidratato, mai {@code null}
     */
    SkillSwapState load();

    /**
     * Persiste lo stato corrente, sostituendo l'istantanea precedente secondo l'implementazione.
     *
     * @param state stato da serializzare
     */
    void save(SkillSwapState state);
}
