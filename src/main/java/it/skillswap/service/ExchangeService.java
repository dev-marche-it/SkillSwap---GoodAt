package it.skillswap.service;

import it.skillswap.domain.Exchange;
import it.skillswap.domain.ExchangeStatus;
import it.skillswap.domain.Offer;
import it.skillswap.domain.Request;
import it.skillswap.domain.SkillSwapState;
import it.skillswap.domain.exception.InvalidStateTransitionException;
import it.skillswap.domain.exception.OfferNotActiveException;

public class ExchangeService {
    private final SkillSwapState state;

    public ExchangeService(SkillSwapState state) {
        this.state = state;
    }

    public Exchange propose(String exchangeId, String offerId, String requestId) {
        Offer offer = findOffer(offerId);
        Request request = findRequest(requestId);

        if (offer == null) throw new IllegalArgumentException("Offer non trovata: " + offerId);
        if (request == null) throw new IllegalArgumentException("Request non trovata: " + requestId);
        if (!offer.isActive()) throw new OfferNotActiveException(offerId);
        if (offer.getStudent().getStudentId().equals(request.getStudent().getStudentId())) {
            throw new IllegalStateException("Uno studente non può fare match con sé stesso.");
        }

        Exchange exchange = new Exchange(exchangeId, offer, request);
        state.getExchanges().add(exchange);
        return exchange;
    }

    public Exchange accept(String exchangeId) {
        Exchange exchange = findExchange(exchangeId);
        if (exchange == null) throw new IllegalArgumentException("Exchange non trovato: " + exchangeId);
        if (exchange.getStatus() != ExchangeStatus.PROPOSED) {
            throw new InvalidStateTransitionException(exchange.getStatus(), ExchangeStatus.ACCEPTED);
        }

        exchange.setStatus(ExchangeStatus.ACCEPTED);
        return exchange;
    }

    public Exchange complete(String exchangeId) {
        Exchange exchange = findExchange(exchangeId);
        if (exchange == null) throw new IllegalArgumentException("Exchange non trovato: " + exchangeId);
        if (exchange.getStatus() != ExchangeStatus.ACCEPTED) {
            throw new InvalidStateTransitionException(exchange.getStatus(), ExchangeStatus.COMPLETED);
        }

        exchange.setStatus(ExchangeStatus.COMPLETED);
        exchange.getOffer().setActive(false);
        return exchange;
    }

    public Exchange cancel(String exchangeId) {
        Exchange exchange = findExchange(exchangeId);
        if (exchange == null) throw new IllegalArgumentException("Exchange non trovato: " + exchangeId);
        if (exchange.getStatus() != ExchangeStatus.PROPOSED) {
            throw new InvalidStateTransitionException(exchange.getStatus(), ExchangeStatus.CANCELLED);
        }

        exchange.setStatus(ExchangeStatus.CANCELLED);
        return exchange;
    }

    private Offer findOffer(String offerId) {
        return state.getOffers().stream()
                .filter(o -> o.getOfferId().equals(offerId))
                .findFirst().orElse(null);
    }

    private Request findRequest(String requestId) {
        return state.getRequests().stream()
                .filter(r -> r.getRequestId().equals(requestId))
                .findFirst().orElse(null);
    }

    private Exchange findExchange(String exchangeId) {
        return state.getExchanges().stream()
                .filter(e -> e.getExchangeId().equals(exchangeId))
                .findFirst().orElse(null);
    }
}