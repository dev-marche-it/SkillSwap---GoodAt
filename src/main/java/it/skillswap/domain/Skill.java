package it.skillswap.domain;

/**
 * Competenza insegnabile nella piattaforma, classificata tramite {@link SkillCategory}.
 */
public class Skill {
    private final String skillId;
    private final String name;
    private final SkillCategory category;

    /**
     * @param skillId  identificativo univoco della competenza
     * @param name     nome visualizzato
     * @param category macro-categoria (materia, lingua, ecc.)
     */
    public Skill(String skillId, String name, SkillCategory category) {
        this.skillId = skillId;
        this.name = name;
        this.category = category;
    }

    /** @return identificativo univoco della competenza */
    public String getSkillId() { return skillId; }

    /** @return nome visualizzato */
    public String getName() { return name; }

    /** @return valore enumerato della categoria */
    public SkillCategory getCategory() { return category; }

    @Override
    public String toString() {
        return skillId + " - " + name + " [" + category + "]";
    }
}
