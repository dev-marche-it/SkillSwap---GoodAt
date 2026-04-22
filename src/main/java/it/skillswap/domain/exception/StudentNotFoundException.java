package it.skillswap.domain.exception;

/**
 * Eccezione lanciata quando uno studente non viene trovato nel sistema.
 */
public class StudentNotFoundException extends SkillSwapException {
    public StudentNotFoundException(String studentId) {
        super("Studente non trovato: " + studentId);
    }
}
