package it.skillswap.service;

import java.util.List;

import it.skillswap.domain.Exchange;
import it.skillswap.domain.ExchangeStatus;
import it.skillswap.domain.Review;
import it.skillswap.domain.SkillSwapState;
import it.skillswap.domain.Student;
import it.skillswap.domain.exception.DuplicateReviewException;
import it.skillswap.domain.exception.InvalidStarsException;

/**
 * Servizio applicativo per creare {@link Review} e interrogare le recensioni per studente valutato.
 */
public class ReviewService {
    private final SkillSwapState state;

    /**
     * @param state aggregato mutabile che contiene scambi, studenti e recensioni
     */
    public ReviewService(SkillSwapState state) {
        this.state = state;
    }

    /**
     * Aggiunge una recensione per uno scambio completato, aggiorna la media del valutato e impone una sola recensione per recensore per scambio.
     *
     * @param reviewId   nuovo id recensione
     * @param exchangeId scambio recensito (deve essere {@link ExchangeStatus#COMPLETED})
     * @param reviewerId id dello studente che scrive (deve essere partecipante)
     * @param stars      voto 1–5
     * @param comment    commento libero
     * @return la recensione persistita
     * @throws IllegalArgumentException se scambio o recensore sconosciuti
     * @throws IllegalStateException se lo scambio non è completato o il recensore non è tra i partecipanti
     * @throws DuplicateReviewException se questo recensore ha già recensito questo scambio
     * @throws InvalidStarsException se le stelle sono fuori dall'intervallo 1–5
     */
    public Review addReview(String reviewId, String exchangeId, String reviewerId, int stars, String comment) {
        Exchange exchange = findExchange(exchangeId);
        if (exchange == null) throw new IllegalArgumentException("Exchange non trovato: " + exchangeId);
        if (exchange.getStatus() != ExchangeStatus.COMPLETED) {
            throw new IllegalStateException("La recensione è possibile solo per exchange COMPLETED.");
        }

        Student reviewer = findStudent(reviewerId);
        if (reviewer == null) throw new IllegalArgumentException("Studente non trovato: " + reviewerId);

        Student reviewee;
        if (exchange.getOffer().getStudent().getStudentId().equals(reviewerId)) {
            reviewee = exchange.getRequest().getStudent();
        } else if (exchange.getRequest().getStudent().getStudentId().equals(reviewerId)) {
            reviewee = exchange.getOffer().getStudent();
        } else {
            throw new IllegalStateException("Il reviewer non è coinvolto in questo exchange.");
        }

        boolean alreadyReviewed = state.getReviews().stream()
                .anyMatch(r -> r.getExchange().getExchangeId().equals(exchangeId)
                        && r.getReviewer().getStudentId().equals(reviewerId));
        if (alreadyReviewed) {
            throw new DuplicateReviewException(exchangeId);
        }

        if (stars < 1 || stars > 5) {
            throw new InvalidStarsException(stars);
        }

        Review review = new Review(reviewId, exchange, reviewer, reviewee, stars, comment);
        state.getReviews().add(review);

        reviewee.addRating(stars);

        return review;
    }

    /**
     * Restituisce tutte le recensioni in cui il valutato è lo studente indicato.
     *
     * @param studentId id dello studente che riceve il feedback
     * @return lista immutabile di recensioni (può essere vuota)
     */
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
