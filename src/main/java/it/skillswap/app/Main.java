package it.skillswap.app;

import it.skillswap.domain.SkillSwapState;
import it.skillswap.storage.FileStorage;
import it.skillswap.storage.Storage;

/**
 * Punto di ingresso CLI: carica lo stato da CSV, esegue il menu console, salva in uscita.
 */
public class Main {
    /**
     * Avvia {@link FileStorage}, carica {@link SkillSwapState} e delega a {@link AppController}.
     *
     * @param args non utilizzati
     */
    public static void main(String[] args) {
        ConsoleBanner.print();
        Storage storage = new FileStorage();
        SkillSwapState state = storage.load();
        new AppController(state, storage).run();
    }
}
