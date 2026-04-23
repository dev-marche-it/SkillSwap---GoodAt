package it.skillswap.domain;

public enum SkillLevel {
    BEGINNER, INTERMEDIATE, ADVANCED;

    public static SkillLevel fromString(String s) {
        if (s == null) return BEGINNER;
        for (SkillLevel level : values()) {
            if (level.name().equalsIgnoreCase(s)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Invalid SkillLevel: " + s);
    }

    public boolean isSufficientFor(SkillLevel minLevel) {
        return this.ordinal() >= minLevel.ordinal();
    }
}