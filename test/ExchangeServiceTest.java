import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.skillswap.domain.Exchange;
import it.skillswap.domain.ExchangeStatus;
import it.skillswap.domain.Offer;
import it.skillswap.domain.Request;
import it.skillswap.domain.Skill;
import it.skillswap.domain.SkillCategory;
import it.skillswap.domain.SkillLevel;
import it.skillswap.domain.SkillSwapState;
import it.skillswap.domain.Student;
import it.skillswap.domain.exception.InvalidStateTransitionException;
import it.skillswap.domain.exception.OfferNotActiveException;
import it.skillswap.service.ExchangeService;

/**
 * Test suite for ExchangeService.
 * Tests cover the exchange lifecycle: propose, accept, complete, cancel.
 */
public class ExchangeServiceTest {
    private SkillSwapState state;
    private ExchangeService exchangeService;
    private Student alice;
    private Student bob;
    private Skill mathSkill;
    private Offer offer;
    private Request request;

    @BeforeEach
    public void setUp() {
        // GIVEN: Initialize state
        state = new SkillSwapState();
        exchangeService = new ExchangeService(state);

        // Create students
        alice = new Student("S1", "Alice", "4A", "alice@mail.com");
        bob = new Student("S2", "Bob", "4B", "bob@mail.com");
        state.getStudents().add(alice);
        state.getStudents().add(bob);

        // Create skill
        mathSkill = new Skill("SK1", "Mathematics", SkillCategory.SUBJECT);
        state.getSkills().add(mathSkill);

        // Create offer and request
        offer = new Offer("O1", alice, mathSkill, SkillLevel.INTERMEDIATE, "");
        request = new Request("R1", bob, mathSkill, SkillLevel.BEGINNER, "");
        state.getOffers().add(offer);
        state.getRequests().add(request);
    }

    @Test
    public void shouldProposeExchange_WhenOfferAndRequestValid() {
        // WHEN: Propose exchange
        Exchange exchange = exchangeService.propose("E1", "O1", "R1");

        // THEN: Exchange created with PROPOSED status
        assertNotNull(exchange);
        assertEquals("E1", exchange.getExchangeId());
        assertEquals(ExchangeStatus.PROPOSED, exchange.getStatus());
        assertEquals(1, state.getExchanges().size());
    }

    @Test
    public void shouldThrowOfferNotActiveException_WhenProposingWithInactiveOffer() {
        // GIVEN: Deactivate offer
        offer.setActive(false);

        // WHEN/THEN: Throw exception
        assertThrows(OfferNotActiveException.class, () -> {
            exchangeService.propose("E1", "O1", "R1");
        });
    }

    @Test
    public void shouldAcceptExchange_WhenStatusIsPROPOSED() {
        // GIVEN: Exchange in PROPOSED status
        Exchange exchange = exchangeService.propose("E1", "O1", "R1");

        // WHEN: Accept exchange
        Exchange result = exchangeService.accept("E1");

        // THEN: Status changed to ACCEPTED
        assertEquals(ExchangeStatus.ACCEPTED, result.getStatus());
    }

    @Test
    public void shouldThrowInvalidStateTransitionException_WhenAcceptingAlreadyAccepted() {
        // GIVEN: Exchange already accepted
        exchangeService.propose("E1", "O1", "R1");
        exchangeService.accept("E1");

        // WHEN/THEN: Cannot accept again
        assertThrows(InvalidStateTransitionException.class, () -> {
            exchangeService.accept("E1");
        });
    }

    @Test
    public void shouldCompleteExchange_WhenStatusIsACCEPTED() {
        // GIVEN: Exchange in ACCEPTED status
        exchangeService.propose("E1", "O1", "R1");
        exchangeService.accept("E1");

        // WHEN: Complete exchange
        Exchange result = exchangeService.complete("E1");

        // THEN: Status changed to COMPLETED and offer deactivated
        assertEquals(ExchangeStatus.COMPLETED, result.getStatus());
        assertFalse(offer.isActive());
    }
}
