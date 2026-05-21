package it.skillswap.web.api;

import it.skillswap.domain.SkillSwapState;

/**
 * Genera id sequenziali come la CLI ({@code S1}, {@code O1}, …).
 */
public final class IdGenerator {

    private IdGenerator() {}

    public static String nextStudentId(SkillSwapState state) {
        return "S" + (state.getStudents().size() + 1);
    }

    public static String nextSkillId(SkillSwapState state) {
        return "K" + (state.getSkills().size() + 1);
    }

    public static String nextOfferId(SkillSwapState state) {
        return "O" + (state.getOffers().size() + 1);
    }

    public static String nextRequestId(SkillSwapState state) {
        return "R" + (state.getRequests().size() + 1);
    }

    public static String nextExchangeId(SkillSwapState state) {
        return "E" + (state.getExchanges().size() + 1);
    }

    public static String nextReviewId(SkillSwapState state) {
        return "V" + (state.getReviews().size() + 1);
    }
}
