package it.skillswap.domain;

/**
 * Studente iscritto alla piattaforma SkillSwap.
 * Dati anagrafici immutabili e metriche di valutazione aggiornabili (media e conteggio).
 */
public class Student {
    private final String studentId;
    private final String name;
    private final String className;
    private final String email;
    private String passwordHash;
    private double ratingAvg;
    private int ratingCount;

    /**
     * Costruisce uno studente senza password (compatibilità test/CLI legacy).
     */
    public Student(String studentId, String name, String className, String email) {
        this(studentId, name, className, email, "");
    }

    /**
     * Costruisce uno studente con hash password (registrazione web).
     *
     * @param passwordHash digest SHA-256, mai in chiaro
     */
    public Student(String studentId, String name, String className, String email, String passwordHash) {
        this.studentId = studentId;
        this.name = name;
        this.className = className;
        this.email = email;
        this.passwordHash = passwordHash != null ? passwordHash : "";
        this.ratingAvg = 0.0;
        this.ratingCount = 0;
    }

    /** @return hash SHA-256 della password (mai esporre via API pubbliche) */
    public String getPasswordHash() { return passwordHash; }

    /**
     * Ricostruisce uno studente da riga CSV (uso {@link it.skillswap.storage.FileStorage}).
     */
    public static Student fromPersistence(
            String studentId, String name, String className, String email,
            double ratingAvg, int ratingCount, String passwordHash) {
        Student s = new Student(studentId, name, className, email, passwordHash);
        s.ratingAvg = ratingAvg;
        s.ratingCount = ratingCount;
        return s;
    }

    /**
     * Aggiunge una valutazione al profilo e ricalcola la media.
     *
     * @param stars voto da 1 a 5
     */
    public void addRating(int stars) {
        ratingAvg = ((ratingAvg * ratingCount) + stars) / (ratingCount + 1);
        ratingCount++;
    }

    /**
     * @return identificativo univoco dello studente
     */
    public String getStudentId() { return studentId; }

    /**
     * @return nome dello studente
     */
    public String getName() { return name; }

    /**
     * @return nome della classe
     */
    public String getClassName() { return className; }

    /**
     * @return indirizzo email
     */
    public String getEmail() { return email; }

    /**
     * @return media delle valutazioni ricevute
     */
    public double getRatingAvg() { return ratingAvg; }

    /**
     * @return numero di valutazioni incluse in {@link #getRatingAvg()}
     */
    public int getRatingCount() { return ratingCount; }

    @Override
    public String toString() {
        return studentId + " - " + name + " (" + className + ")";
    }
}
