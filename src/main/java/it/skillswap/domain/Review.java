package it.skillswap.domain;

import java.time.LocalDateTime;

/**
 * Feedback tra pari dopo il completamento di uno {@link Exchange}: uno studente valuta l'altro con stelle e commento.
 */
public class Review {
    private String reviewId;
    private Exchange exchange;
    private Student reviewer;
    private Student reviewee;
    private int stars;
    private String comment;
    private LocalDateTime createdAt;

    /**
     * Crea una recensione con l'istante corrente.
     *
     * @param reviewId identificativo univoco della recensione
     * @param exchange scambio completato oggetto della recensione
     * @param reviewer studente che scrive la recensione
     * @param reviewee studente che riceve la recensione
     * @param stars    voto da 1 a 5
     * @param comment  commento libero
     * @throws IllegalArgumentException se {@code stars} non è compreso tra 1 e 5
     */
    public Review(String reviewId, Exchange exchange, Student reviewer, Student reviewee, int stars, String comment) {
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Il voto deve essere compreso tra 1 e 5");
        }
        this.reviewId = reviewId;
        this.exchange = exchange;
        this.reviewer = reviewer;
        this.reviewee = reviewee;
        this.stars = stars;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }

    /** @return identificativo univoco della recensione */
    public String getReviewId() { return reviewId; }

    /** @return scambio a cui si riferisce la recensione */
    public Exchange getExchange() { return exchange; }

    /** @return autore della recensione */
    public Student getReviewer() { return reviewer; }

    /** @return studente valutato */
    public Student getReviewee() { return reviewee; }

    /** @return voto in stelle (1–5) */
    public int getStars() { return stars; }

    /** @return testo del commento */
    public String getComment() { return comment; }

    /** @return data/ora di creazione della recensione */
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return reviewId + " | " + reviewer.getName() + " -> " + reviewee.getName() + " : " + stars + "★ \"" + comment + "\"";
    }
}
