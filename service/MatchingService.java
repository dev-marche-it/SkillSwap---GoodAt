package it.skillswap.service;

import it.skillswap.domain.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MatchingService {
    private SkillSwapState state;

    public MatchingService(SkillSwapState state) {
        this.state = state;
    }

    // Trova chi offre la skill che lo studente sta cercando
    public List<MatchResult> findOneWayMatches(String studentId) {
        List<MatchResult> results = new ArrayList<>();

        Student seeker = state.getStudents().get(studentId);
        if (seeker == null) return results;

        // Per ogni request dello studente
        for (Request request : state.getRequests().values()) {
            if (!request.getStudent().getStudentId().equals(studentId)) continue;

            // Cerca offerte che matchano
            for (Offer offer : state.getOffers().values()) {

                // Escludi offerte non attive
                if (!offer.isActive()) continue;

                // Escludi lo stesso studente
                if (offer.getStudent().getStudentId().equals(studentId)) continue;

                // Controlla che la skill sia la stessa
                if (!offer.getSkill().getSkillId().equals(request.getSkill().getSkillId())) continue;

                int score = calculateScore(offer, request);
                String reason = buildReason(offer, request);
                results.add(new MatchResult(offer.getOfferId(), request.getRequestId(), score, reason));
            }
        }

        // Ordina per score decrescente
        results.sort(Comparator.comparingInt(MatchResult::getScore).reversed());
        return results;
    }

    // Trova match reciproci: io offro X cerco Y, l'altro offre Y cerca X
    public List<MatchResult> findSwapMatches(String studentId) {
        List<MatchResult> results = new ArrayList<>();

        Student student = state.getStudents().get(studentId);
        if (student == null) return results;

        // Offerte e request dello studente
        List<Offer> myOffers = new ArrayList<>();
        List<Request> myRequests = new ArrayList<>();

        for (Offer o : state.getOffers().values()) {
            if (o.getStudent().getStudentId().equals(studentId) && o.isActive()) {
                myOffers.add(o);
            }
        }
        for (Request r : state.getRequests().values()) {
            if (r.getStudent().getStudentId().equals(studentId)) {
                myRequests.add(r);
            }
        }

        // Per ogni combinazione offerta mia / request mia
        for (Offer myOffer : myOffers) {
            for (Request myRequest : myRequests) {

                // Cerca un altro studente che offre ciò che cerco e cerca ciò che offro
                for (Offer theirOffer : state.getOffers().values()) {
                    if (!theirOffer.isActive()) continue;
                    if (theirOffer.getStudent().getStudentId().equals(studentId)) continue;

                    // L'altro offre quello che io cerco
                    if (!theirOffer.getSkill().getSkillId().equals(myRequest.getSkill().getSkillId())) continue;

                    String theirStudentId = theirOffer.getStudent().getStudentId();

                    // L'altro cerca quello che io offro
                    for (Request theirRequest : state.getRequests().values()) {
                        if (!theirRequest.getStudent().getStudentId().equals(theirStudentId)) continue;
                        if (!theirRequest.getSkill().getSkillId().equals(myOffer.getSkill().getSkillId())) continue;

                        int score = calculateScore(theirOffer, myRequest) + calculateScore(myOffer, theirRequest);
                        String reason = "SWAP: " + buildReason(theirOffer, myRequest);
                        results.add(new MatchResult(theirOffer.getOfferId(), myRequest.getRequestId(), score, reason));
                    }
                }
            }
        }

        results.sort(Comparator.comparingInt(MatchResult::getScore).reversed());
        return results;
    }

    // +3 skill uguale, +2 livello sufficiente, +1 stessa classe
    private int calculateScore(Offer offer, Request request) {
        int score = 0;

        // +3 skill uguale (già filtrato prima, ma lo confermiamo)
        if (offer.getSkill().getSkillId().equals(request.getSkill().getSkillId())) {
            score += 3;
        }

        // +2 livello sufficiente
        if (isLevelSufficient(offer.getLevel(), request.getMinLevel())) {
            score += 2;
        }

        // +1 stessa classe
        if (offer.getStudent().getClassName().equals(request.getStudent().getClassName())) {
            score += 1;
        }

        return score;
    }

    private String buildReason(Offer offer, Request request) {
        StringBuilder sb = new StringBuilder();

        if (offer.getSkill().getSkillId().equals(request.getSkill().getSkillId())) {
            sb.append("skill identica (+3)");
        }
        if (isLevelSufficient(offer.getLevel(), request.getMinLevel())) {
            sb.append(", livello sufficiente (+2)");
        }
        if (offer.getStudent().getClassName().equals(request.getStudent().getClassName())) {
            sb.append(", stessa classe (+1)");
        }

        return sb.toString();
    }

    // BEGINNER < INTERMEDIATE < ADVANCED
    private boolean isLevelSufficient(String offerLevel, String minLevel) {
        List<String> levels = List.of("BEGINNER", "INTERMEDIATE", "ADVANCED");
        int offerIdx = levels.indexOf(offerLevel);
        int minIdx = levels.indexOf(minLevel);
        return offerIdx >= minIdx;
    }
}