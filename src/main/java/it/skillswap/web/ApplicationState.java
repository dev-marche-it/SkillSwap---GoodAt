package it.skillswap.web;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import it.skillswap.domain.Exchange;
import it.skillswap.domain.ExchangeStatus;
import it.skillswap.domain.SkillSwapState;
import it.skillswap.domain.Student;
import it.skillswap.service.AuthService;
import it.skillswap.service.PasswordHasher;
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
        migrateLegacyPasswords();
        reconcileDuplicateActiveExchanges();
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

    /**
     * Account CSV creati prima dell'introduzione password: imposta hash di default e salva una volta.
     */
    private void migrateLegacyPasswords() {
        boolean changed = false;
        String defaultHash = PasswordHasher.hash("SkillSwap123");
        for (Student s : state.getStudents()) {
            if (s.getPasswordHash() == null || s.getPasswordHash().isBlank()) {
                s.setPasswordHash(defaultHash);
                changed = true;
            }
        }
        if (changed) {
            storage.save(state);
        }
    }

    /**
     * Se nel CSV esistono più scambi PROPOSED/ACCEPTED sulla stessa coppia offerta/richiesta,
     * mantiene il più recente (id numerico maggiore) e annulla i duplicati.
     */
    private void reconcileDuplicateActiveExchanges() {
        Map<String, Exchange> bestByPair = new HashMap<>();
        boolean changed = false;

        for (Exchange exchange : state.getExchanges()) {
            if (exchange.getStatus() != ExchangeStatus.PROPOSED
                    && exchange.getStatus() != ExchangeStatus.ACCEPTED) {
                continue;
            }
            String key = exchange.getOffer().getOfferId() + "|" + exchange.getRequest().getRequestId();
            Exchange current = bestByPair.get(key);
            if (current == null) {
                bestByPair.put(key, exchange);
                continue;
            }
            Exchange keeper = preferKeeper(current, exchange);
            Exchange duplicate = keeper == current ? exchange : current;
            duplicate.setStatus(ExchangeStatus.CANCELLED);
            bestByPair.put(key, keeper);
            changed = true;
        }

        if (changed) {
            storage.save(state);
        }
    }

    private static Exchange preferKeeper(Exchange a, Exchange b) {
        int numA = numericSuffix(a.getExchangeId());
        int numB = numericSuffix(b.getExchangeId());
        if (numA != numB) {
            return numA > numB ? a : b;
        }
        if (a.getStatus() == ExchangeStatus.ACCEPTED && b.getStatus() != ExchangeStatus.ACCEPTED) {
            return a;
        }
        if (b.getStatus() == ExchangeStatus.ACCEPTED && a.getStatus() != ExchangeStatus.ACCEPTED) {
            return b;
        }
        return a;
    }

    private static int numericSuffix(String id) {
        if (id == null || id.length() < 2) {
            return 0;
        }
        try {
            return Integer.parseInt(id.substring(1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
