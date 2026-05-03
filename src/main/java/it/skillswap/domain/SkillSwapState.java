package it.skillswap.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregato in memoria di tutte le entità della piattaforma: studenti, competenze, offerte, richieste, scambi e recensioni.
 * Le liste mutabili sono esposte per semplicità; i service modificano questo stato e {@link it.skillswap.storage.Storage}
 * lo persiste quando necessario.
 */
public class SkillSwapState {
    private List<Student> students;
    private List<Skill> skills;
    private List<Offer> offers;
    private List<Request> requests;
    private List<Exchange> exchanges;
    private List<Review> reviews;

    /** Crea uno stato vuoto con liste inizialmente vuote. */
    public SkillSwapState() {
        this.students = new ArrayList<>();
        this.skills = new ArrayList<>();
        this.offers = new ArrayList<>();
        this.requests = new ArrayList<>();
        this.exchanges = new ArrayList<>();
        this.reviews = new ArrayList<>();
    }

    /** @return lista mutabile degli studenti registrati */
    public List<Student> getStudents() { return students; }

    /** @return lista mutabile delle competenze */
    public List<Skill> getSkills() { return skills; }

    /** @return lista mutabile delle offerte */
    public List<Offer> getOffers() { return offers; }

    /** @return lista mutabile delle richieste */
    public List<Request> getRequests() { return requests; }

    /** @return lista mutabile degli scambi */
    public List<Exchange> getExchanges() { return exchanges; }

    /** @return lista mutabile delle recensioni */
    public List<Review> getReviews() { return reviews; }
}
