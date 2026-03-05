package it.skillswap.domain;

public class Skill {
    private String skillId;
    private String name;
    private String category;

    public Skill(String skillId, String name, String category) {
        this.skillId = skillId;
        this.name = name;
        this.category = category;
    }

    public String getSkillId() { return skillId; }
    public String getName() { return name; }
    public String getCategory() { return category; }

    @Override
    public String toString() {
        return skillId + " - " + name + " [" + category + "]";
    }
}
