package it.skillswap.service;

import it.skillswap.domain.Exchange;
import it.skillswap.domain.ExchangeStatus;
import it.skillswap.domain.Offer;
import it.skillswap.domain.Review;
import it.skillswap.domain.SkillSwapState;
import it.skillswap.domain.exception.InvalidStarsException;
import it.skillswap.domain.exception.InvalidStateTransitionException;

public class Validator {

    private static final int MIN_STARS = 1;
    private static final int MAX_STARS = 5;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    public static ValidationResult validateStars(int stars) {
        if (stars < MIN_STARS || stars > MAX_STARS) {
            return ValidationResult.failure("Le stelle devono essere tra " + MIN_STARS + " e " + MAX_STARS);
        }
        return ValidationResult.success();
    }

    public static void validateStarsStrict(int stars) {
        if (stars < MIN_STARS || stars > MAX_STARS) {
            throw new InvalidStarsException(stars);
        }
    }

    public static ValidationResult validateStateTransition(ExchangeStatus from, ExchangeStatus to) {
        if (from == ExchangeStatus.COMPLETED && to != ExchangeStatus.COMPLETED) {
            return ValidationResult.failure("Non è possibile cambiare lo stato di uno scambio completato");
        }
        if (from == ExchangeStatus.CANCELLED && to != ExchangeStatus.CANCELLED) {
            return ValidationResult.failure("Non è possibile cambiare lo stato di uno scambio cancellato");
        }
        return ValidationResult.success();
    }

    public static void validateStateTransitionStrict(ExchangeStatus from, ExchangeStatus to) {
        if (from == ExchangeStatus.COMPLETED && to != ExchangeStatus.COMPLETED) {
            throw new InvalidStateTransitionException(from, to);
        }
        if (from == ExchangeStatus.CANCELLED && to != ExchangeStatus.CANCELLED) {
            throw new InvalidStateTransitionException(from, to);
        }
    }

    public static ValidationResult validateOfferActive(Offer offer) {
        if (!offer.isActive()) {
            return ValidationResult.failure("L'offerta non è più attiva");
        }
        return ValidationResult.success();
    }

    public static ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.failure("L'email non può essere vuota");
        }
        if (!email.matches(EMAIL_REGEX)) {
            return ValidationResult.failure("Formato email non valido: " + email);
        }
        return ValidationResult.success();
    }

    public static ValidationResult validateUniqueReview(Exchange exchange, SkillSwapState state) {
        for (Review review : state.getReviews()) {
            if (review.getExchange().getExchangeId().equals(exchange.getExchangeId())) {
                return ValidationResult.failure("Una recensione per questo scambio esiste già");
            }
        }
        return ValidationResult.success();
    }
}
