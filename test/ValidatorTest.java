import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import it.skillswap.domain.ExchangeStatus;
import it.skillswap.domain.exception.InvalidStarsException;
import it.skillswap.domain.exception.InvalidStateTransitionException;
import it.skillswap.service.ValidationResult;
import it.skillswap.service.Validator;

/**
 * Test suite for Validator.
 * Tests cover all validation methods and error conditions.
 */
public class ValidatorTest {

    @Test
    public void shouldThrowInvalidStarsException_WhenStarsInvalid() {
        // WHEN/THEN: Stars below minimum throws exception
        InvalidStarsException ex1 = assertThrows(InvalidStarsException.class, () -> {
            Validator.validateStarsStrict(0);
        });
        assertNotNull(ex1);

        // WHEN/THEN: Stars above maximum throws exception
        InvalidStarsException ex2 = assertThrows(InvalidStarsException.class, () -> {
            Validator.validateStarsStrict(6);
        });
        assertNotNull(ex2);

        // WHEN/THEN: Valid stars do not throw
        assertDoesNotThrow(() -> {
            Validator.validateStarsStrict(3);
        });
    }

    @Test
    public void shouldThrowInvalidStateTransitionException_WhenTransitionInvalid() {
        // WHEN/THEN: COMPLETED to ACCEPTED is invalid
        InvalidStateTransitionException ex1 = assertThrows(InvalidStateTransitionException.class, () -> {
            Validator.validateStateTransitionStrict(ExchangeStatus.COMPLETED, ExchangeStatus.ACCEPTED);
        });
        assertNotNull(ex1);

        // WHEN/THEN: CANCELLED to PROPOSED is invalid
        InvalidStateTransitionException ex2 = assertThrows(InvalidStateTransitionException.class, () -> {
            Validator.validateStateTransitionStrict(ExchangeStatus.CANCELLED, ExchangeStatus.PROPOSED);
        });
        assertNotNull(ex2);

        // WHEN/THEN: Valid transition does not throw
        assertDoesNotThrow(() -> {
            Validator.validateStateTransitionStrict(ExchangeStatus.PROPOSED, ExchangeStatus.ACCEPTED);
        });
    }

    @Test
    public void shouldReturnFailure_WhenEmailMalformed() {
        // WHEN: Validate malformed emails
        ValidationResult resultNoAt = Validator.validateEmail("invalidemail.com");
        ValidationResult resultEmpty = Validator.validateEmail("");
        ValidationResult resultValid = Validator.validateEmail("valid@mail.com");

        // THEN: Invalid emails fail, valid email passes
        assertFalse(resultNoAt.isValid());
        assertFalse(resultEmpty.isValid());
        assertTrue(resultValid.isValid());
    }
}
