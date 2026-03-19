package it.skillswap.service;

import it.skillswap.domain.*;

import java.util.List;

public class ReviewService {
    private SkillSwapState state;

    public ReviewService(SkillSwapState state) {
        this.state = state;
    }

    public Review addReview(String reviewId, String exchangeId, String reviewerId, int stars, String comment) {
        Exchange exchange = findExchange(exchangeId);
        if (exchange == null) throw new IllegalArgumentException("Exchange non trovato: " + exchangeId);
        if (exchange.getStatus() != ExchangeStatus.COMPLETED) {
            throw new IllegalStateException("La recensione è possibile solo per exchange COMPLETED.");
        }

        Student reviewer = findStudent(reviewerId);
        if (reviewer == null) throw new IllegalArgumentException("Studente non trovato: " + reviewerId);

        // Il reviewee è l'altro studente nello scambio
        Student reviewee;
        if (exchange.getOffer().getStudent().getStudentId().equals(reviewerId)) {
            reviewee = exchange.getRequest().getStudent();
        } else if (exchange.getRequest().getStudent().getStudentId().equals(reviewerId)) {
            reviewee = exchange.getOffer().getStudent();
        } else {
            throw new IllegalStateException("Il reviewer non è coinvolto in questo exchange.");
        }

        // Uno studente può lasciare al massimo una recensione per scambio
        boolean alreadyReviewed = state.getReviews().stream()
                .anyMatch(r -> r.getExchange().getExchangeId().equals(exchangeId)
                        && r.getReviewer().getStudentId().equals(reviewerId));
        if (alreadyReviewed) {
            throw new IllegalStateException("Hai già lasciato una recensione per questo exchange.");
        }

        Review review = new Review(reviewId, exchange, reviewer, reviewee, stars, comment);
        state.getReviews().add(review);

        // Aggiorna il rating del reviewee
        reviewee.addRating(stars);

        return review;
    }

    public List<Review> getReviewsForStudent(String studentId) {
        return state.getReviews().stream()
                .filter(r -> r.getReviewee().getStudentId().equals(studentId))
                .toList();
    }

    private Exchange findExchange(String exchangeId) {
        return state.getExchanges().stream()
                .filter(e -> e.getExchangeId().equals(exchangeId))
                .findFirst().orElse(null);
    }

    private Student findStudent(String studentId) {
        return state.getStudents().stream()
                .filter(s -> s.getStudentId().equals(studentId))
                .findFirst().orElse(null);
    }
}