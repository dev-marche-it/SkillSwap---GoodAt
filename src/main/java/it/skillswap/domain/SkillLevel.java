package it.skillswap.domain;

/**
 * Livello di competenza per le offerte e livello minimo per le richieste
 * (ordine: principiante &lt; intermedio &lt; avanzato, come costanti enum).
 */
public enum SkillLevel {
    BEGINNER, INTERMEDIATE, ADVANCED;

    /**
     * Interpreta un livello da CSV o input utente (nome enum case-insensitive).
     *
     * @param s stringa grezza, oppure {@code null} per il default {@link #BEGINNER}
     * @return livello corrispondente
     * @throws IllegalArgumentException se {@code s} non è un nome di livello noto
     */
    public static SkillLevel fromString(String s) {
        if (s == null) return BEGINNER;
        for (SkillLevel level : values()) {
            if (level.name().equalsIgnoreCase(s)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Invalid SkillLevel: " + s);
    }

    /**
     * @param minLevel livello minimo richiesto da una {@link Request}
     * @return {@code true} se questo livello di offerta soddisfa o supera {@code minLevel}
     */
    public boolean isSufficientFor(SkillLevel minLevel) {
        return this.ordinal() >= minLevel.ordinal();
    }
}
