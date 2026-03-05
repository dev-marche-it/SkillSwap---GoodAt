package it.skillswap.domain;

import java.time.LocalDateTime;

public class Review {
    private String reviewId;
    private Exchange exchange;
    private Student reviewer;
    private Student reviewee;
    private int stars;
    private String comment;
    private LocalDateTime createdAt;

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

    public String getReviewId() { return reviewId; }
    public Exchange getExchange() { return exchange; }
    public Student getReviewer() { return reviewer; }
    public Student getReviewee() { return reviewee; }
    public int getStars() { return stars; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return reviewId + " | " + reviewer.getName() + " -> " + reviewee.getName() + " : " + stars + "★ \"" + comment + "\"";
    }
}
