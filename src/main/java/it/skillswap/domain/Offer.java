package it.skillswap.domain;

public class Offer {
    private String offerId;
    private Student student;
    private Skill skill;
    private String level;
    private String note;
    private boolean active;

    public Offer(String offerId, Student student, Skill skill, String level, String note) {
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
    public String getLevel() { return level; }
    public String getNote() { return note; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return offerId + " | " + student.getName() + " offre: " + skill.getName() + " [" + level + "]";
    }
}
