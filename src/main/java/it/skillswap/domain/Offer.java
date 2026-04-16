package it.skillswap.domain;

public class Offer {
    private final String offerId;
    private final Student student;
    private final Skill skill;
    private final SkillLevel level;
    private final String note;
    private boolean active;

    public Offer(String offerId, Student student, Skill skill, SkillLevel level, String note) {
        this.offerId = offerId;
        this.student = student;
        this.skill = skill;
        this.level = level;
        this.note = note;
        this.active = true;
    }

    public String getOfferId() { return offerId; }
    public Student getStudent() { return student; }
    public Skill getSkill() { return skill; }
    public SkillLevel getLevel() { return level; }
    public String getNote() { return note; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return offerId + " | " + student.getName() + " offre: " + skill.getName() + " [" + level + "]";
    }
}
