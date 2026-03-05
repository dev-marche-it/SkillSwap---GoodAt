package it.skillswap.storage;

import it.skillswap.domain.SkillSwapState;

public class InMemoryStorage implements Storage {
    private SkillSwapState state;

    public InMemoryStorage() {
        this.state = new SkillSwapState();
    }

    @Override
    public SkillSwapState load() {
        return state;
    }

    @Override
    public void save(SkillSwapState state) {
        this.state = state;
    }
}