package it.skillswap.domain;

/**
 * Dichiarazione attiva di uno studente: può insegnare o aiutare su una {@link Skill} a un dato {@link SkillLevel}.
 */
public class Offer {
    private final String offerId;
    private final Student student;
    private final Skill skill;
    private final SkillLevel level;
    private final String note;
    private boolean active;

    /**
     * Crea un'offerta attiva.
     *
     * @param offerId identificativo univoco dell'offerta
     * @param student studente che pubblica l'offerta
     * @param skill   competenza offerta
     * @param level   livello di competenza offerto
     * @param note    nota testuale opzionale
     */
    public Offer(String offerId, Student student, Skill skill, SkillLevel level, String note) {
        this.offerId = offerId;
        this.student = student;
        this.skill = skill;
        this.level = level;
        this.note = note;
        this.active = true;
    }

    /** @return identificativo univoco dell'offerta */
    public String getOfferId() { return offerId; }

    /** @return studente che ha pubblicato l'offerta */
    public Student getStudent() { return student; }

    /** @return competenza offerta */
    public Skill getSkill() { return skill; }

    /** @return livello di competenza offerto */
    public SkillLevel getLevel() { return level; }

    /** @return testo della nota opzionale */
    public String getNote() { return note; }

    /** @return {@code true} se l'offerta può ancora essere abbinata */
    public boolean isActive() { return active; }

    /**
     * @param active se l'offerta partecipa a nuovi abbinamenti
     */
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return offerId + " | " + student.getName() + " offre: " + skill.getName() + " [" + level + "]";
    }
}
