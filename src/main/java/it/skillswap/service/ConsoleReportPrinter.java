package it.skillswap.service;

import java.util.List;

import it.skillswap.domain.Exchange;
import it.skillswap.domain.Review;
import it.skillswap.domain.Student;

public class ConsoleReportPrinter {

    private static final String SEPARATOR = "=".repeat(50);
    private static final String THIN_SEP  = "-".repeat(50);

    public String printStudentProfile(Student student, List<Review> reviews) {
        StringBuilder sb = new StringBuilder();
        sb.append(SEPARATOR).append("\n");
        sb.append("  PROFILO STUDENTE\n");
        sb.append(SEPARATOR).append("\n");
        sb.append("  ID    : ").append(student.getStudentId()).append("\n");
        sb.append("  Nome  : ").append(student.getName()).append("\n");
        sb.append("  Classe: ").append(student.getClassName()).append("\n");
        sb.append("  Email : ").append(student.getEmail()).append("\n");
        sb.append(THIN_SEP).append("\n");
        sb.append("  Rating: ").append(String.format("%.1f", student.getRatingAvg()))
          .append(" stelle (").append(student.getRatingCount()).append(" recensioni)\n");
        sb.append(THIN_SEP).append("\n");

        if (reviews.isEmpty()) {
            sb.append("  Nessuna recensione ricevuta.\n");
        } else {
            sb.append("  Recensioni:\n");
            for (Review r : reviews) {
                sb.append("    [").append(r.getStars()).append("★] ")
                  .append(r.getReviewer().getName()).append(": ")
                  .append(r.getComment()).append("\n");
            }
        }

        sb.append(SEPARATOR).append("\n");
        return sb.toString();
    }

    public String printMatches(List<MatchResult> matches) {
        StringBuilder sb = new StringBuilder();
        sb.append(SEPARATOR).append("\n");
        sb.append("  RISULTATI MATCHING\n");
        sb.append(SEPARATOR).append("\n");

        if (matches.isEmpty()) {
            sb.append("  Nessun match trovato.\n");
        } else {
            for (MatchResult m : matches) {
                sb.append(THIN_SEP).append("\n");
                sb.append("  Offer  : ").append(m.getOfferId()).append("\n");
                sb.append("  Request: ").append(m.getRequestId()).append("\n");
                sb.append("  Score  : ").append(m.getScore()).append("\n");
                sb.append("  Motivo : ");
                sb.append(m.getReason()).append("\n");
            }
        }

        sb.append(SEPARATOR).append("\n");
        return sb.toString();
    }

    public String printExchangeDetails(Exchange exchange) {
        StringBuilder sb = new StringBuilder();
        sb.append(SEPARATOR).append("\n");
        sb.append("  DETTAGLIO EXCHANGE\n");
        sb.append(SEPARATOR).append("\n");
        sb.append("  ID     : ").append(exchange.getExchangeId()).append("\n");
        sb.append("  Stato  : ").append(exchange.getStatus()).append("\n");
        sb.append("  Offerta: ").append(exchange.getOffer()).append("\n");
        sb.append("  Ricerca: ").append(exchange.getRequest()).append("\n");
        sb.append("  Creato : ").append(exchange.getCreatedAt()).append("\n");
        if (exchange.getClosedAt() != null) {
            sb.append("  Chiuso : ").append(exchange.getClosedAt()).append("\n");
        }
        sb.append(SEPARATOR).append("\n");
        return sb.toString();
    }

    public String printLeaderboard(List<Student> students) {
        StringBuilder sb = new StringBuilder();
        sb.append(SEPARATOR).append("\n");
        sb.append("  LEADERBOARD STUDENTI\n");
        sb.append(SEPARATOR).append("\n");

        List<Student> sorted = students.stream()
                .filter(s -> s.getRatingCount() > 0)
                .sorted((a, b) -> Double.compare(b.getRatingAvg(), a.getRatingAvg()))
                .toList();

        if (sorted.isEmpty()) {
            sb.append("  Nessuno studente con recensioni.\n");
        } else {
            int pos = 1;
            for (Student s : sorted) {
                sb.append("  ").append(pos++).append(". ")
                  .append(s.getName())
                  .append(" (").append(s.getClassName()).append(")")
                  .append(" - ").append(String.format("%.1f", s.getRatingAvg()))
                  .append("★ (").append(s.getRatingCount()).append(" voti)\n");
            }
        }

        sb.append(SEPARATOR).append("\n");
        return sb.toString();
    }
}