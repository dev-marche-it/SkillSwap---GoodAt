package it.skillswap.storage;

import it.skillswap.domain.SkillSwapState;

/**
 * {@link Storage} non persistente che mantiene un solo {@link SkillSwapState} in JVM (utile per i test).
 */
public class InMemoryStorage implements Storage {
    private SkillSwapState state;

    /** Inizializza con uno {@link SkillSwapState} vuoto. */
    public InMemoryStorage() {
        this.state = new SkillSwapState();
    }

    /**
     * {@inheritDoc}
     * @return la stessa istanza in memoria passata a {@link #save} o creata al costruttore
     */
    @Override
    public SkillSwapState load() {
        return state;
    }

    /**
     * {@inheritDoc}
     * @param state sostituisce il riferimento tenuto (non è una copia profonda)
     */
    @Override
    public void save(SkillSwapState state) {
        this.state = state;
    }
}
