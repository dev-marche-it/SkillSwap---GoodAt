package it.skillswap.domain.exception;

/**
 * Lanciata quando un id studente non corrisponde a uno studente registrato.
 */
public class StudentNotFoundException extends SkillSwapException {
    /**
     * @param studentId identificativo studente assente
     */
    public StudentNotFoundException(String studentId) {
        super("Studente non trovato: " + studentId);
    }
}
