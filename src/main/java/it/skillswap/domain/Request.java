package it.skillswap.domain;

/**
 * Ricerca di aiuto su una {@link Skill} da parte di uno studente, con livello minimo richiesto {@link SkillLevel}.
 */
public class Request {
    private final String requestId;
    private final Student student;
    private final Skill skill;
    private final SkillLevel minLevel;
    private final String note;

    /**
     * Crea una richiesta per una competenza.
     *
     * @param requestId identificativo univoco della richiesta
     * @param student   studente che ha bisogno della competenza
     * @param skill     competenza cercata
     * @param minLevel  livello minimo accettabile dall'offerta di un pari
     * @param note      nota testuale opzionale
     */
    public Request(String requestId, Student student, Skill skill, SkillLevel minLevel, String note) {
        this.requestId = requestId;
        this.student = student;
        this.skill = skill;
        this.minLevel = minLevel;
        this.note = note;
    }

    /** @return identificativo univoco della richiesta */
    public String getRequestId() { return requestId; }

    /** @return studente che ha creato la richiesta */
    public Student getStudent() { return student; }

    /** @return competenza richiesta */
    public Skill getSkill() { return skill; }

    /** @return livello minimo richiesto alle offerte abbinate */
    public SkillLevel getMinLevel() { return minLevel; }

    /** @return testo della nota opzionale */
    public String getNote() { return note; }

    @Override
    public String toString() {
        return requestId + " | " + student.getName() + " cerca: " + skill.getName() + " [min: " + minLevel + "]";
    }
}
