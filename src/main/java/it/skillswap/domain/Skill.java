package it.skillswap.domain;

public class Skill {
    private final String skillId;
    private final String name;
    private final SkillCategory category;

    public Skill(String skillId, String name, SkillCategory category) {
        this.skillId = skillId;
        this.name = name;
        this.category = category;
    }

    public String getSkillId() { return skillId; }
    public String getName() { return name; }
    public SkillCategory getCategory() { return category; }

    @Override
    public String toString() {
        return skillId + " - " + name + " [" + category + "]";
    }
}
