package it.skillswap.domain;

public enum SkillCategory {
    SUBJECT, LANGUAGE, SPORT, ART, OTHER;

    public static SkillCategory fromString(String s) {
        if (s == null) return SUBJECT;
        for (SkillCategory category : values()) {
            if (category.name().equalsIgnoreCase(s)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid SkillCategory: " + s);
    }
}