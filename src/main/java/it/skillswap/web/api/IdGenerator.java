package it.skillswap.web.api;

import it.skillswap.domain.SkillSwapState;
import it.skillswap.service.EntityIdGenerator;

/**
 * Delega a {@link EntityIdGenerator} (stessa logica della CLI).
 */
public final class IdGenerator {

    private IdGenerator() {}

    public static String nextStudentId(SkillSwapState state) {
        return EntityIdGenerator.nextStudentId(state);
    }

    public static String nextSkillId(SkillSwapState state) {
        return EntityIdGenerator.nextSkillId(state);
    }

    public static String nextOfferId(SkillSwapState state) {
        return EntityIdGenerator.nextOfferId(state);
    }

    public static String nextRequestId(SkillSwapState state) {
        return EntityIdGenerator.nextRequestId(state);
    }

    public static String nextExchangeId(SkillSwapState state) {
        return EntityIdGenerator.nextExchangeId(state);
    }

    public static String nextReviewId(SkillSwapState state) {
        return EntityIdGenerator.nextReviewId(state);
    }
}
