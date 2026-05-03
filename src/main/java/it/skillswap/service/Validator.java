package it.skillswap.service;

import it.skillswap.domain.Exchange;
import it.skillswap.domain.ExchangeStatus;
import it.skillswap.domain.Offer;
import it.skillswap.domain.Review;
import it.skillswap.domain.SkillSwapState;
import it.skillswap.domain.exception.InvalidStarsException;
import it.skillswap.domain.exception.InvalidStateTransitionException;

/**
 * Utilità di validazione centralizzata per le regole di business.
 * Offre validazione "soft" (tramite {@link ValidationResult}) e "strict" (tramite eccezioni).
 */
public class Validator {

    private static final int MIN_STARS = 1;
    private static final int MAX_STARS = 5;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    /**
     * Valida il voto in stelle in modalità soft (restituisce {@link ValidationResult}).
     *
     * @param stars valutazione da controllare
     * @return esito con messaggio in caso di fallimento
     */
    public static ValidationResult validateStars(int stars) {
        if (stars < MIN_STARS || stars > MAX_STARS) {
            return ValidationResult.failure("Le stelle devono essere tra " + MIN_STARS + " e " + MAX_STARS);
        }
        return ValidationResult.success();
    }

    /**
     * Valida il voto in stelle in modalità strict (lancia eccezione).
     *
     * @param stars valutazione da controllare
     * @throws InvalidStarsException se fuori dall'intervallo valido
     */
    public static void validateStarsStrict(int stars) {
        if (stars < MIN_STARS || stars > MAX_STARS) {
            throw new InvalidStarsException(stars);
        }
    }

    /**
     * Valida la transizione di stato in modalità soft.
     *
     * @param from stato corrente
     * @param to   stato target
     * @return esito con messaggio in caso di fallimento
     */
    public static ValidationResult validateStateTransition(ExchangeStatus from, ExchangeStatus to) {
        if (from == ExchangeStatus.COMPLETED && to != ExchangeStatus.COMPLETED) {
            return ValidationResult.failure("Non è possibile cambiare lo stato di uno scambio completato");
        }
        if (from == ExchangeStatus.CANCELLED && to != ExchangeStatus.CANCELLED) {
            return ValidationResult.failure("Non è possibile cambiare lo stato di uno scambio cancellato");
        }
        return ValidationResult.success();
    }

    /**
     * Valida la transizione di stato in modalità strict.
     *
     * @param from stato corrente
     * @param to   stato target
     * @throws InvalidStateTransitionException se la transizione non è consentita
     */
    public static void validateStateTransitionStrict(ExchangeStatus from, ExchangeStatus to) {
        if (from == ExchangeStatus.COMPLETED && to != ExchangeStatus.COMPLETED) {
            throw new InvalidStateTransitionException(from, to);
        }
        if (from == ExchangeStatus.CANCELLED && to != ExchangeStatus.CANCELLED) {
            throw new InvalidStateTransitionException(from, to);
        }
    }

    /**
     * Verifica che un'offerta sia ancora attiva.
     *
     * @param offer offerta da controllare
     * @return esito positivo se attiva
     */
    public static ValidationResult validateOfferActive(Offer offer) {
        if (!offer.isActive()) {
            return ValidationResult.failure("L'offerta non è più attiva");
        }
        return ValidationResult.success();
    }

    /**
     * Valida email non vuota e forma minima locale@dominio.
     *
     * @param email indirizzo grezzo
     * @return successo o fallimento con messaggio
     */
    public static ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.failure("L'email non può essere vuota");
        }
        if (!email.matches(EMAIL_REGEX)) {
            return ValidationResult.failure("Formato email non valido: " + email);
        }
        return ValidationResult.success();
    }

    /**
     * Controllo soft: nessuna recensione esistente per lo scambio indicato (qualsiasi recensore).
     *
     * @param exchange scambio candidato
     * @param state    stato la cui lista recensioni viene scandita
     * @return successo se nessuna recensione punta a questo id scambio, altrimenti fallimento
     */
    public static ValidationResult validateUniqueReview(Exchange exchange, SkillSwapState state) {
        for (Review review : state.getReviews()) {
            if (review.getExchange().getExchangeId().equals(exchange.getExchangeId())) {
                return ValidationResult.failure("Una recensione per questo scambio esiste già");
            }
        }
        return ValidationResult.success();
    }
}
