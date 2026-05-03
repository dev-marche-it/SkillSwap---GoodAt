package it.skillswap.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import it.skillswap.domain.Offer;
import it.skillswap.domain.Request;
import it.skillswap.domain.SkillLevel;
import it.skillswap.domain.SkillSwapState;
import it.skillswap.domain.Student;

/**
 * Servizio per trovare abbinamenti tra offerte e richieste.
 * Implementa algoritmo di matching bidirezionale con punteggio di qualità.
 */
public class MatchingService {
    private final SkillSwapState state;

    /**
     * @param state stato applicativo con offerte e richieste
     */
    public MatchingService(SkillSwapState state) {
        this.state = state;
    }

    /**
     * Trova abbinamenti one-way: offerte di altri che soddisfano le richieste dello studente.
     *
     * @param studentId id dello studente che cerca match
     * @return risultati ordinati per punteggio decrescente
     */
    public List<MatchResult> findOneWayMatches(String studentId) {
        List<MatchResult> results = new ArrayList<>();

        // Trova lo studente che sta cercando
        Student seeker = state.getStudents().stream()
                .filter(s -> s.getStudentId().equals(studentId))
                .findFirst()
                .orElse(null);
        if (seeker == null) {
            return results;
        }

        // Per ogni request dello studente
        for (Request request : state.getRequests()) {
            if (!request.getStudent().getStudentId().equals(studentId)) continue;

            // Cerca offerte che matchano
            for (Offer offer : state.getOffers()) {

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

    /**
     * Trova scambi reciproci: l'offerta di un altro soddisfa la richiesta di questo studente e viceversa.
     *
     * @param studentId id dello studente che cerca opportunità di swap
     * @return righe {@link MatchResult} con punteggio totale decrescente; vuota se studente assente o nessuno scambio reciproco
     */
    public List<MatchResult> findSwapMatches(String studentId) {
        List<MatchResult> results = new ArrayList<>();

        Student student = state.getStudents().stream()
                .filter(s -> s.getStudentId().equals(studentId))
                .findFirst()
                .orElse(null);
        if (student == null) {
            return results;
        }

        // Offerte e request dello studente
        List<Offer> myOffers = new ArrayList<>();
        List<Request> myRequests = new ArrayList<>();

        for (Offer o : state.getOffers()) {
            if (o.getStudent().getStudentId().equals(studentId) && o.isActive()) {
                myOffers.add(o);
            }
        }
        for (Request r : state.getRequests()) {
            if (r.getStudent().getStudentId().equals(studentId)) {
                myRequests.add(r);
            }
        }

        // Per ogni combinazione offerta mia / request mia
        for (Offer myOffer : myOffers) {
            for (Request myRequest : myRequests) {

                // Cerca un altro studente che offre ciò che cerco e cerca ciò che offro
                for (Offer theirOffer : state.getOffers()) {
                    if (!theirOffer.isActive()) continue;
                    if (theirOffer.getStudent().getStudentId().equals(studentId)) continue;

                    // L'altro offre quello che io cerco
                    if (!theirOffer.getSkill().getSkillId().equals(myRequest.getSkill().getSkillId())) continue;

                    String theirStudentId = theirOffer.getStudent().getStudentId();

                    // L'altro cerca quello che io offro
                    for (Request theirRequest : state.getRequests()) {
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
    private boolean isLevelSufficient(SkillLevel offerLevel, SkillLevel minLevel) {
        return offerLevel.isSufficientFor(minLevel);
    }
}
