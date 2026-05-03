package it.skillswap.domain;

/**
 * Raggruppamento ad alto livello per le {@link Skill} (materia, lingua, sport, ecc.).
 */
public enum SkillCategory {
    SUBJECT, LANGUAGE, SPORT, ART, OTHER;

    /**
     * Interpreta una categoria da CSV o input utente (nome enum case-insensitive).
     *
     * @param s stringa grezza, oppure {@code null} per il default {@link #SUBJECT}
     * @return categoria corrispondente
     * @throws IllegalArgumentException se {@code s} non è un nome di categoria noto
     */
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
