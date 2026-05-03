import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.skillswap.domain.Offer;
import it.skillswap.domain.Request;
import it.skillswap.domain.Skill;
import it.skillswap.domain.SkillCategory;
import it.skillswap.domain.SkillLevel;
import it.skillswap.domain.SkillSwapState;
import it.skillswap.domain.Student;
import it.skillswap.service.MatchResult;
import it.skillswap.service.MatchingService;

/**
 * Test suite for MatchingService.
 * Tests cover one-way matches, swap matches, score calculation, and edge cases.
 */
public class MatchingServiceTest {
    private SkillSwapState state;
    private MatchingService matchingService;
    private Student alice;
    private Student bob;
    private Skill mathSkill;

    @BeforeEach
    public void setUp() {
        // GIVEN: Initialize state
        state = new SkillSwapState();
        matchingService = new MatchingService(state);

        // Create students
        alice = new Student("S1", "Alice", "4A", "alice@mail.com");
        bob = new Student("S2", "Bob", "4B", "bob@mail.com");
        state.getStudents().add(alice);
        state.getStudents().add(bob);

        // Create skill
        mathSkill = new Skill("SK1", "Mathematics", SkillCategory.SUBJECT);
        state.getSkills().add(mathSkill);
    }

    @Test
    public void shouldFindOneWayMatches_WhenStudentHasRequests() {
        // GIVEN: Alice requests Math, Bob offers Math
        Request aliceRequest = new Request("R1", alice, mathSkill, SkillLevel.BEGINNER, "");
        Offer bobOffer = new Offer("O1", bob, mathSkill, SkillLevel.INTERMEDIATE, "");
        state.getRequests().add(aliceRequest);
        state.getOffers().add(bobOffer);

        // WHEN: Find one-way matches for Alice
        List<MatchResult> results = matchingService.findOneWayMatches("S1");

        // THEN: One match found
        assertEquals(1, results.size());
        assertEquals("O1", results.get(0).getOfferId());
    }

    @Test
    public void shouldFindSwapMatches_WhenMutualSkillsExist() {
        // GIVEN: Alice offers Math seeks English, Bob offers English seeks Math
        Skill englishSkill = new Skill("SK2", "English", SkillCategory.LANGUAGE);
        state.getSkills().add(englishSkill);

        Offer aliceOffer = new Offer("O1", alice, mathSkill, SkillLevel.BEGINNER, "");
        Request aliceRequest = new Request("R1", alice, englishSkill, SkillLevel.BEGINNER, "");
        Offer bobOffer = new Offer("O2", bob, englishSkill, SkillLevel.BEGINNER, "");
        Request bobRequest = new Request("R2", bob, mathSkill, SkillLevel.BEGINNER, "");

        state.getOffers().add(aliceOffer);
        state.getRequests().add(aliceRequest);
        state.getOffers().add(bobOffer);
        state.getRequests().add(bobRequest);

        // WHEN: Find swap matches for Alice
        List<MatchResult> results = matchingService.findSwapMatches("S1");

        // THEN: Swap match found
        assertFalse(results.isEmpty());
    }

    @Test
    public void shouldNotMatchStudentWithItself() {
        // GIVEN: Alice offers and requests Math
        Offer aliceOffer = new Offer("O1", alice, mathSkill, SkillLevel.BEGINNER, "");
        Request aliceRequest = new Request("R1", alice, mathSkill, SkillLevel.BEGINNER, "");
        state.getOffers().add(aliceOffer);
        state.getRequests().add(aliceRequest);

        // WHEN: Find one-way matches for Alice
        List<MatchResult> results = matchingService.findOneWayMatches("S1");

        // THEN: No match found (self-match excluded)
        assertEquals(0, results.size());
    }

    @Test
    public void shouldExcludeInactiveOffers_WhenFindingMatches() {
        // GIVEN: Alice requests Math, Bob offers Math but offer is inactive
        Request aliceRequest = new Request("R1", alice, mathSkill, SkillLevel.BEGINNER, "");
        Offer bobOffer = new Offer("O1", bob, mathSkill, SkillLevel.INTERMEDIATE, "");
        bobOffer.setActive(false);

        state.getRequests().add(aliceRequest);
        state.getOffers().add(bobOffer);

        // WHEN: Find one-way matches for Alice
        List<MatchResult> results = matchingService.findOneWayMatches("S1");

        // THEN: No match found (inactive offer excluded)
        assertEquals(0, results.size());
    }

    @Test
    public void shouldCalculateScoreProperly() {
        // GIVEN: Multiple offers with different scores
        Request aliceRequest = new Request("R1", alice, mathSkill, SkillLevel.BEGINNER, "");
        Offer bobOffer = new Offer("O1", bob, mathSkill, SkillLevel.INTERMEDIATE, "");
        Offer charlieOffer = new Offer("O2", new Student("S3", "Charlie", "4C", "charlie@mail.com"), 
                                       mathSkill, SkillLevel.ADVANCED, "");

        state.getStudents().add(charlieOffer.getStudent());
        state.getRequests().add(aliceRequest);
        state.getOffers().add(bobOffer);
        state.getOffers().add(charlieOffer);

        // WHEN: Find one-way matches for Alice
        List<MatchResult> results = matchingService.findOneWayMatches("S1");

        // THEN: Results sorted by score descending
        assertEquals(2, results.size());
        assertTrue(results.get(0).getScore() >= results.get(1).getScore());
    }

    @Test
    public void shouldReturnEmptyList_WhenNoMatchesFound() {
        // GIVEN: Alice requests Math, no one offers Math
        Request aliceRequest = new Request("R1", alice, mathSkill, SkillLevel.BEGINNER, "");
        state.getRequests().add(aliceRequest);

        // WHEN: Find one-way matches for Alice
        List<MatchResult> results = matchingService.findOneWayMatches("S1");

        // THEN: Empty list returned
        assertEquals(0, results.size());
    }
}
