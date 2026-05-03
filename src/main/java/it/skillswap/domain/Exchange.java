package it.skillswap.domain;

import java.time.LocalDateTime;

/**
 * Proposta di scambio competenze che collega un'{@link Offer} a una {@link Request},
 * con ciclo di vita da {@link ExchangeStatus#PROPOSED} fino a completamento o annullamento.
 */
public class Exchange {
    private String exchangeId;
    private Offer offer;
    private Request request;
    private ExchangeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;

    /**
     * Crea un nuovo scambio in stato {@link ExchangeStatus#PROPOSED} con l'istante corrente.
     *
     * @param exchangeId identificativo univoco dello scambio
     * @param offer      lato offerta dello scambio
     * @param request    lato richiesta dello scambio
     */
    public Exchange(String exchangeId, Offer offer, Request request) {
        this.exchangeId = exchangeId;
        this.offer = offer;
        this.request = request;
        this.status = ExchangeStatus.PROPOSED;
        this.createdAt = LocalDateTime.now();
        this.closedAt = null;
    }

    /** @return identificativo univoco dello scambio */
    public String getExchangeId() { return exchangeId; }

    /** @return offerta collegata */
    public Offer getOffer() { return offer; }

    /** @return richiesta collegata */
    public Request getRequest() { return request; }

    /** @return stato attuale del ciclo di vita */
    public ExchangeStatus getStatus() { return status; }

    /** @return data/ora di creazione */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** @return data/ora di chiusura se completato o annullato, altrimenti {@code null} */
    public LocalDateTime getClosedAt() { return closedAt; }

    /**
     * Aggiorna lo stato e imposta {@link #closedAt} passando a {@link ExchangeStatus#COMPLETED}
     * o {@link ExchangeStatus#CANCELLED}.
     *
     * @param status nuovo stato
     */
    public void setStatus(ExchangeStatus status) {
        this.status = status;
        if (status == ExchangeStatus.COMPLETED || status == ExchangeStatus.CANCELLED) {
            this.closedAt = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return exchangeId + " | " + offer.getOfferId() + " <-> " + request.getRequestId() + " [" + status + "]";
    }
}
