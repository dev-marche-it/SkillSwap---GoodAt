package it.skillswap.service;

import it.skillswap.domain.Exchange;
import it.skillswap.domain.ExchangeStatus;
import it.skillswap.domain.Offer;
import it.skillswap.domain.Request;
import it.skillswap.domain.SkillSwapState;
import it.skillswap.domain.exception.InvalidStateTransitionException;
import it.skillswap.domain.exception.OfferNotActiveException;

/**
 * Servizio applicativo per il ciclo di vita degli {@link Exchange}: proposta, accettazione, completamento e annullamento.
 */
public class ExchangeService {
    private final SkillSwapState state;

    /**
     * @param state aggregato mutabile che contiene offerte, richieste e scambi
     */
    public ExchangeService(SkillSwapState state) {
        this.state = state;
    }

    /**
     * Crea un nuovo scambio proposto se l'offerta è attiva e coinvolge due studenti distinti.
     *
     * @param exchangeId nuovo id dello scambio
     * @param offerId    id dell'offerta da collegare
     * @param requestId  id della richiesta da collegare
     * @return lo scambio creato in stato {@link ExchangeStatus#PROPOSED}
     * @throws OfferNotActiveException se l'offerta non è attiva
     * @throws IllegalArgumentException se l'id offerta o richiesta è sconosciuto
     * @throws IllegalStateException se offerta e richiesta appartengono allo stesso studente
     */
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

    /**
     * Accetta uno scambio proposto.
     *
     * @param exchangeId id dello scambio
     * @return scambio aggiornato in stato {@link ExchangeStatus#ACCEPTED}
     * @throws IllegalArgumentException se l'id è sconosciuto
     * @throws InvalidStateTransitionException se lo stato attuale non è {@link ExchangeStatus#PROPOSED}
     */
    public Exchange accept(String exchangeId) {
        Exchange exchange = findExchange(exchangeId);
        if (exchange == null) throw new IllegalArgumentException("Exchange non trovato: " + exchangeId);
        if (exchange.getStatus() != ExchangeStatus.PROPOSED) {
            throw new InvalidStateTransitionException(exchange.getStatus(), ExchangeStatus.ACCEPTED);
        }

        exchange.setStatus(ExchangeStatus.ACCEPTED);
        return exchange;
    }

    /**
     * Segna come completato uno scambio accettato e disattiva l'offerta collegata.
     *
     * @param exchangeId id dello scambio
     * @return scambio aggiornato in stato {@link ExchangeStatus#COMPLETED}
     * @throws IllegalArgumentException se l'id è sconosciuto
     * @throws InvalidStateTransitionException se lo stato attuale non è {@link ExchangeStatus#ACCEPTED}
     */
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

    /**
     * Annulla uno scambio ancora solo proposto.
     *
     * @param exchangeId id dello scambio
     * @return scambio aggiornato in stato {@link ExchangeStatus#CANCELLED}
     * @throws IllegalArgumentException se l'id è sconosciuto
     * @throws InvalidStateTransitionException se lo stato attuale non è {@link ExchangeStatus#PROPOSED}
     */
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
