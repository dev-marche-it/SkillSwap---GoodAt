package it.skillswap.domain;

public class Request {
    private String requestId;
    private Student student;
    private Skill skill;
    private String minLevel;
    private String note;

    public Request(String requestId, Student student, Skill skill, String minLevel, String note) {
        this.requestId = requestId;
        this.student = student;
        this.skill = skill;
        this.minLevel = minLevel;
        this.note = note;
    }

    public String getRequestId() { return requestId; }
    public Student getStudent() { return student; }
    public Skill getSkill() { return skill; }
    public String getMinLevel() { return minLevel; }
    public String getNote() { return note; }

    @Override
    public String toString() {
        return requestId + " | " + student.getName() + " cerca: " + skill.getName() + " [min: " + minLevel + "]";
    }
}