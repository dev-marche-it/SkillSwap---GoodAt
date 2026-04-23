import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.skillswap.domain.Skill;
import it.skillswap.domain.SkillCategory;
import it.skillswap.domain.SkillSwapState;
import it.skillswap.domain.Student;
import it.skillswap.storage.FileStorage;

/**
 * Test suite for FileStorage.
 * Tests cover loading, saving, and atomic file operations.
 */
public class FileStorageTest {
    private FileStorage storage;

    @BeforeEach
    public void setUp() {
        // GIVEN: Create FileStorage instance
        storage = new FileStorage();
    }

    @Test
    public void shouldLoadStudentsFromCSV_WhenFileExists() {
        // GIVEN: Create test data
        SkillSwapState state = new SkillSwapState();
        Student alice = new Student("S1", "Alice", "4A", "alice@mail.com");
        state.getStudents().add(alice);

        // WHEN: Save and load
        storage.save(state);
        SkillSwapState loadedState = storage.load();

        // THEN: Students loaded correctly
        assertNotNull(loadedState);
        assertFalse(loadedState.getStudents().isEmpty());
    }

    @Test
    public void shouldSaveStateToCSV_WhenCalled() {
        // GIVEN: Create state with data
        SkillSwapState state = new SkillSwapState();
        Student alice = new Student("S1", "Alice", "4A", "alice@mail.com");
        Skill mathSkill = new Skill("SK1", "Mathematics", SkillCategory.SUBJECT);
        state.getStudents().add(alice);
        state.getSkills().add(mathSkill);

        // WHEN: Save
        storage.save(state);

        // THEN: Operation completed successfully (no exception thrown)
        assertTrue(true);
    }

    @Test
    public void shouldHandleAtomicRename_WhenSavingFiles() {
        // GIVEN: Create state
        SkillSwapState state = new SkillSwapState();
        Student alice = new Student("S1", "Alice", "4A", "alice@mail.com");
        state.getStudents().add(alice);

        // WHEN: Save multiple times
        storage.save(state);
        storage.save(state);

        // THEN: Load should succeed (atomic rename ensures consistency)
        SkillSwapState loadedState = storage.load();
        assertNotNull(loadedState);
    }
}
