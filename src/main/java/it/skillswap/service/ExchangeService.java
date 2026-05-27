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

        if (hasActiveExchangeFor(offerId, requestId)) {
            throw new IllegalStateException(
                    "Esiste già uno scambio attivo per questa coppia offerta/richiesta.");
        }

        Exchange exchange = new Exchange(exchangeId, offer, request);
        state.getExchanges().add(exchange);
        return exchange;
    }

    /**
     * Accetta uno scambio proposto (solo il proprietario dell'offerta).
     */
    public Exchange accept(String exchangeId, String studentId) {
        Exchange exchange = requireExchange(exchangeId);
        if (exchange.getStatus() != ExchangeStatus.PROPOSED) {
            throw new InvalidStateTransitionException(exchange.getStatus(), ExchangeStatus.ACCEPTED);
        }
        requireOfferOwner(exchange, studentId);
        exchange.setStatus(ExchangeStatus.ACCEPTED);
        return exchange;
    }

    /**
     * Segna come completato uno scambio accettato (entrambi i partecipanti).
     */
    public Exchange complete(String exchangeId, String studentId) {
        Exchange exchange = requireExchange(exchangeId);
        if (exchange.getStatus() != ExchangeStatus.ACCEPTED) {
            throw new InvalidStateTransitionException(exchange.getStatus(), ExchangeStatus.COMPLETED);
        }
        requireParticipant(exchange, studentId);
        exchange.setStatus(ExchangeStatus.COMPLETED);
        exchange.getOffer().setActive(false);
        return exchange;
    }

    /**
     * Annulla uno scambio proposto (entrambi i partecipanti).
     */
    public Exchange cancel(String exchangeId, String studentId) {
        Exchange exchange = requireExchange(exchangeId);
        if (exchange.getStatus() != ExchangeStatus.PROPOSED) {
            throw new InvalidStateTransitionException(exchange.getStatus(), ExchangeStatus.CANCELLED);
        }
        requireParticipant(exchange, studentId);
        exchange.setStatus(ExchangeStatus.CANCELLED);
        return exchange;
    }

    private boolean hasActiveExchangeFor(String offerId, String requestId) {
        return state.getExchanges().stream()
                .anyMatch(e -> e.getOffer().getOfferId().equals(offerId)
                        && e.getRequest().getRequestId().equals(requestId)
                        && (e.getStatus() == ExchangeStatus.PROPOSED
                                || e.getStatus() == ExchangeStatus.ACCEPTED));
    }

    private Exchange requireExchange(String exchangeId) {
        Exchange exchange = findExchange(exchangeId);
        if (exchange == null) {
            throw new IllegalArgumentException("Exchange non trovato: " + exchangeId);
        }
        return exchange;
    }

    private void requireParticipant(Exchange exchange, String studentId) {
        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("studentId obbligatorio");
        }
        boolean participant = exchange.getOffer().getStudent().getStudentId().equals(studentId)
                || exchange.getRequest().getStudent().getStudentId().equals(studentId);
        if (!participant) {
            throw new IllegalStateException("Non sei coinvolto in questo scambio.");
        }
    }

    private void requireOfferOwner(Exchange exchange, String studentId) {
        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("studentId obbligatorio");
        }
        if (!exchange.getOffer().getStudent().getStudentId().equals(studentId)) {
            throw new IllegalStateException(
                    "Solo chi ha pubblicato l'offerta può accettare la proposta di scambio.");
        }
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
