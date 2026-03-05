package it.skillswap.domain;

import java.time.LocalDateTime;

public class Exchange {
    private String exchangeId;
    private Offer offer;
    private Request request;
    private ExchangeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;

    public Exchange(String exchangeId, Offer offer, Request request) {
        this.exchangeId = exchangeId;
        this.offer = offer;
        this.request = request;
        this.status = ExchangeStatus.PROPOSED;
        this.createdAt = LocalDateTime.now();
        this.closedAt = null;
    }

    public String getExchangeId() { return exchangeId; }
    public Offer getOffer() { return offer; }
    public Request getRequest() { return request; }
    public ExchangeStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getClosedAt() { return closedAt; }

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