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
    private double ratingAvg;
    private int ratingCount;

    /**
     * Costruisce uno studente con i dati indicati.
     *
     * @param studentId identificativo univoco dello studente
     * @param name      nome completo
     * @param className classe o cohort
     * @param email     indirizzo email
     */
    public Student(String studentId, String name, String className, String email) {
        this.studentId = studentId;
        this.name = name;
        this.className = className;
        this.email = email;
        this.ratingAvg = 0.0;
        this.ratingCount = 0;
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
