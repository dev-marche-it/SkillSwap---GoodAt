package it.skillswap.service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.skillswap.domain.Exchange;
import it.skillswap.domain.Offer;
import it.skillswap.domain.Request;
import it.skillswap.domain.Review;
import it.skillswap.domain.Skill;
import it.skillswap.domain.SkillSwapState;
import it.skillswap.domain.Student;

/** Generazione id univoci per entità SkillSwap. */
public final class EntityIdGenerator {

    private EntityIdGenerator() {}

    public static String nextStudentId(SkillSwapState state) {
        return nextId("S", state.getStudents(), Student::getStudentId);
    }

    public static String nextSkillId(SkillSwapState state) {
        return nextId("K", state.getSkills(), Skill::getSkillId);
    }

    public static String nextOfferId(SkillSwapState state) {
        return nextId("O", state.getOffers(), Offer::getOfferId);
    }

    public static String nextRequestId(SkillSwapState state) {
        return nextId("R", state.getRequests(), Request::getRequestId);
    }

    public static String nextExchangeId(SkillSwapState state) {
        return nextId("E", state.getExchanges(), Exchange::getExchangeId);
    }

    public static String nextReviewId(SkillSwapState state) {
        return nextId("V", state.getReviews(), Review::getReviewId);
    }

    private static <T> String nextId(String prefix, List<T> items, java.util.function.Function<T, String> idFn) {
        int max = 0;
        Pattern pattern = Pattern.compile("^" + prefix + "(\\d+)$");
        for (T item : items) {
            Matcher m = pattern.matcher(idFn.apply(item));
            if (m.matches()) {
                max = Math.max(max, Integer.parseInt(m.group(1)));
            }
        }
        return prefix + (max + 1);
    }
}
