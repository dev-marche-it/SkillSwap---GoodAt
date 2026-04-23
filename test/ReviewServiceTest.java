import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.skillswap.domain.Exchange;
import it.skillswap.domain.ExchangeStatus;
import it.skillswap.domain.Offer;
import it.skillswap.domain.Request;
import it.skillswap.domain.Review;
import it.skillswap.domain.Skill;
import it.skillswap.domain.SkillCategory;
import it.skillswap.domain.SkillLevel;
import it.skillswap.domain.SkillSwapState;
import it.skillswap.domain.Student;
import it.skillswap.domain.exception.DuplicateReviewException;
import it.skillswap.domain.exception.InvalidStarsException;
import it.skillswap.service.ReviewService;

/**
 * Test suite for ReviewService.
 * Tests cover review creation, validation, and constraints.
 */
public class ReviewServiceTest {
    private SkillSwapState state;
    private ReviewService reviewService;
    private Student alice;
    private Student bob;
    private Exchange completedExchange;

    @BeforeEach
    public void setUp() {
        // GIVEN: Initialize state
        state = new SkillSwapState();
        reviewService = new ReviewService(state);

        // Create students
        alice = new Student("S1", "Alice", "4A", "alice@mail.com");
        bob = new Student("S2", "Bob", "4B", "bob@mail.com");
        state.getStudents().add(alice);
        state.getStudents().add(bob);

        // Create skill, offer, request, and exchange
        Skill mathSkill = new Skill("SK1", "Mathematics", SkillCategory.SUBJECT);
        state.getSkills().add(mathSkill);

        Offer offer = new Offer("O1", alice, mathSkill, SkillLevel.INTERMEDIATE, "");
        Request request = new Request("R1", bob, mathSkill, SkillLevel.BEGINNER, "");
        state.getOffers().add(offer);
        state.getRequests().add(request);

        // Create and complete exchange
        completedExchange = new Exchange("E1", offer, request);
        completedExchange.setStatus(ExchangeStatus.COMPLETED);
        state.getExchanges().add(completedExchange);
    }

    @Test
    public void shouldAddReview_WhenExchangeIsCompleted() {
        // WHEN: Add review
        Review review = reviewService.addReview("V1", "E1", "S1", 5, "Great experience!");

        // THEN: Review created and added to state
        assertNotNull(review);
        assertEquals("V1", review.getReviewId());
        assertEquals(5, review.getStars());
        assertEquals(1, state.getReviews().size());
    }

    @Test
    public void shouldThrowDuplicateReviewException_WhenStudentReviewsTwice() {
        // GIVEN: First review added
        reviewService.addReview("V1", "E1", "S1", 5, "Great!");

        // WHEN/THEN: Second review throws exception
        assertThrows(DuplicateReviewException.class, () -> {
            reviewService.addReview("V2", "E1", "S1", 4, "Also great!");
        });
    }

    @Test
    public void shouldThrowInvalidStarsException_WhenStarsOutOfRange() {
        // WHEN/THEN: Stars < 1 throws exception
        assertThrows(InvalidStarsException.class, () -> {
            reviewService.addReview("V1", "E1", "S1", 0, "Bad");
        });

        // WHEN/THEN: Stars > 5 throws exception
        assertThrows(InvalidStarsException.class, () -> {
            reviewService.addReview("V2", "E1", "S1", 6, "Too high");
        });
    }

    @Test
    public void shouldUpdateStudentRating_WhenReviewIsAdded() {
        // GIVEN: Initial rating count is 0
        int countBefore = bob.getRatingCount();

        // WHEN: Add review with 5 stars
        reviewService.addReview("V1", "E1", "S1", 5, "Excellent!");

        // THEN: Bob's rating count updated
        assertEquals(countBefore + 1, bob.getRatingCount());
    }
}
