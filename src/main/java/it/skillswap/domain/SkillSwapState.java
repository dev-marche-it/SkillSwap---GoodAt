package it.skillswap.domain;

import java.util.ArrayList;
import java.util.List;

public class SkillSwapState {
    private List<Student> students;
    private List<Skill> skills;
    private List<Offer> offers;
    private List<Request> requests;
    private List<Exchange> exchanges;
    private List<Review> reviews;

    public SkillSwapState() {
        this.students = new ArrayList<>();
        this.skills = new ArrayList<>();
        this.offers = new ArrayList<>();
        this.requests = new ArrayList<>();
        this.exchanges = new ArrayList<>();
        this.reviews = new ArrayList<>();
    }

    public List<Student> getStudents() { return students; }
    public List<Skill> getSkills() { return skills; }
    public List<Offer> getOffers() { return offers; }
    public List<Request> getRequests() { return requests; }
    public List<Exchange> getExchanges() { return exchanges; }
    public List<Review> getReviews() { return reviews; }
}
