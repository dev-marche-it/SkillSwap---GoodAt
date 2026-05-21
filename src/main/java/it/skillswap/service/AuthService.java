package it.skillswap.service;

import it.skillswap.domain.SkillSwapState;
import it.skillswap.domain.Student;

/**
 * Registrazione e autenticazione studenti con email e password.
 */
public class AuthService {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final SkillSwapState state;

    public AuthService(SkillSwapState state) {
        this.state = state;
    }

    /**
     * Crea un nuovo account se l'email non è già registrata.
     */
    public Student register(String name, String className, String email, String password) {
        validateRegistration(name, className, email, password);

        if (findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email già registrata: " + email);
        }

        String id = "S" + (state.getStudents().size() + 1);
        Student student = new Student(id, name, className, email, PasswordHasher.hash(password));
        state.getStudents().add(student);
        return student;
    }

    /**
     * Autentica uno studente con email e password.
     */
    public Student login(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email obbligatoria");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password obbligatoria");
        }

        Student student = findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email o password non corretti"));

        if (student.getPasswordHash().isBlank()) {
            throw new IllegalArgumentException(
                    "Account senza password. Registrati con la stessa email o contatta l'amministratore.");
        }

        if (!PasswordHasher.matches(password, student.getPasswordHash())) {
            throw new IllegalArgumentException("Email o password non corretti");
        }

        return student;
    }

    private void validateRegistration(String name, String className, String email, String password) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome obbligatorio");
        }
        if (className == null || className.isBlank()) {
            throw new IllegalArgumentException("Classe obbligatoria");
        }
        ValidationResult emailCheck = Validator.validateEmail(email);
        if (!emailCheck.isValid()) {
            throw new IllegalArgumentException(emailCheck.getMessage());
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("La password deve avere almeno " + MIN_PASSWORD_LENGTH + " caratteri");
        }
    }

    private java.util.Optional<Student> findByEmail(String email) {
        return state.getStudents().stream()
                .filter(s -> s.getEmail().equalsIgnoreCase(email.trim()))
                .findFirst();
    }
}
