package it.skillswap.app;

import it.skillswap.domain.SkillSwapState;
import it.skillswap.storage.FileStorage;
import it.skillswap.storage.Storage;

public class Main {
    public static void main(String[] args) {
        Storage storage = new FileStorage();
        SkillSwapState state = storage.load();
        new AppController(state, storage).run();
    }
}