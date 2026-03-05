package it.skillswap.storage;

import it.skillswap.domain.SkillSwapState;

public interface Storage {
    SkillSwapState load();
    void save(SkillSwapState state);
}
