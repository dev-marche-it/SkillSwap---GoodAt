package it.skillswap.web;

import org.springframework.stereotype.Component;

import it.skillswap.domain.SkillSwapState;
import it.skillswap.service.AuthService;
import it.skillswap.service.ExchangeService;
import it.skillswap.service.MatchingService;
import it.skillswap.service.ReviewService;
import it.skillswap.storage.FileStorage;
import it.skillswap.storage.Storage;

/**
 * Stato applicativo condiviso: carica da CSV all'avvio, espone i service esistenti, persiste dopo le mutazioni.
 */
@Component
public class ApplicationState {

    private final Storage storage;
    private final SkillSwapState state;
    private final AuthService authService;
    private final MatchingService matchingService;
    private final ExchangeService exchangeService;
    private final ReviewService reviewService;

    public ApplicationState() {
        this.storage = new FileStorage();
        this.state = storage.load();
        this.authService = new AuthService(state);
        this.matchingService = new MatchingService(state);
        this.exchangeService = new ExchangeService(state);
        this.reviewService = new ReviewService(state);
    }

    public AuthService getAuthService() {
        return authService;
    }

    public SkillSwapState getState() {
        return state;
    }

    public MatchingService getMatchingService() {
        return matchingService;
    }

    public ExchangeService getExchangeService() {
        return exchangeService;
    }

    public ReviewService getReviewService() {
        return reviewService;
    }

    public void persist() {
        storage.save(state);
    }
}
