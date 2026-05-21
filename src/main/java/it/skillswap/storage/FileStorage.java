package it.skillswap.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.skillswap.domain.Exchange;
import it.skillswap.domain.ExchangeStatus;
import it.skillswap.domain.Offer;
import it.skillswap.domain.Request;
import it.skillswap.domain.Review;
import it.skillswap.domain.Skill;
import it.skillswap.domain.SkillCategory;
import it.skillswap.domain.SkillLevel;
import it.skillswap.domain.SkillSwapState;
import it.skillswap.domain.Student;

/**
 * {@link Storage} basato su CSV nella cartella {@code data/}: file separati per tipo di entità, delimitatore punto e virgola.
 * Errori in lettura producono righe vuote; errori in scrittura vanno su uscita standard senza lanciare eccezioni.
 */
public class FileStorage implements Storage {

    private static final String DATA_DIR = "data/";

    /**
     * {@inheritDoc}
     * <p>File mancanti equivalgono a contenuto vuoto. Righe malformate vengono ignorate.</p>
     *
     * @return stato appena costruito e popolato dai CSV se presenti
     */
    @Override
    public SkillSwapState load() {
        SkillSwapState state = new SkillSwapState();

        Map<String, Student> studentMap = new HashMap<>();
        Map<String, Skill> skillMap = new HashMap<>();
        Map<String, Offer> offerMap = new HashMap<>();
        Map<String, Request> requestMap = new HashMap<>();
        Map<String, Exchange> exchangeMap = new HashMap<>();

        // 1. Carica studenti (id;nome;classe;email;rating_avg;rating_count;password_hash)
        for (String line : readLines("students.csv")) {
            String[] p = line.split(";", -1);
            if (p.length < 4) continue;
            double ratingAvg = 0.0;
            int ratingCount = 0;
            if (p.length >= 6) {
                try {
                    ratingAvg = Double.parseDouble(p[4]);
                    ratingCount = Integer.parseInt(p[5]);
                } catch (NumberFormatException ignored) {
                    // mantieni default 0
                }
            }
            String passwordHash = p.length >= 7 ? p[6] : "";
            Student s = Student.fromPersistence(p[0], p[1], p[2], p[3], ratingAvg, ratingCount, passwordHash);
            studentMap.put(p[0], s);
            state.getStudents().add(s);
        }

        // 2. Carica skill
        for (String line : readLines("skills.csv")) {
            String[] p = line.split(";");
            if (p.length < 3) continue;
            SkillCategory category = SkillCategory.fromString(p[2]);
            Skill sk = new Skill(p[0], p[1], category);
            skillMap.put(p[0], sk);
            state.getSkills().add(sk);
        }

        // 3. Carica offer
        for (String line : readLines("offers.csv")) {
            String[] p = line.split(";");
            if (p.length < 6) continue;
            Student st = studentMap.get(p[1]);
            Skill sk = skillMap.get(p[2]);
            if (st == null || sk == null) continue;
            SkillLevel level = SkillLevel.fromString(p[3]);
            Offer o = new Offer(p[0], st, sk, level, p[4]);
            o.setActive(Boolean.parseBoolean(p[5]));
            offerMap.put(p[0], o);
            state.getOffers().add(o);
        }

        // 4. Carica request
        for (String line : readLines("requests.csv")) {
            String[] p = line.split(";");
            if (p.length < 5) continue;
            Student st = studentMap.get(p[1]);
            Skill sk = skillMap.get(p[2]);
            if (st == null || sk == null) continue;
            SkillLevel minLevel = SkillLevel.fromString(p[3]);
            Request r = new Request(p[0], st, sk, minLevel, p[4]);
            requestMap.put(p[0], r);
            state.getRequests().add(r);
        }

        // 5. Carica exchange
        for (String line : readLines("exchanges.csv")) {
            String[] p = line.split(";", -1);
            if (p.length < 4) continue;
            Offer o = offerMap.get(p[1]);
            Request r = requestMap.get(p[2]);
            if (o == null || r == null) continue;
            Exchange e = new Exchange(p[0], o, r);
            e.setStatus(ExchangeStatus.valueOf(p[3]));
            exchangeMap.put(p[0], e);
            state.getExchanges().add(e);
        }

        // 6. Carica review
        for (String line : readLines("reviews.csv")) {
            String[] p = line.split(";", -1);
            Exchange e = exchangeMap.get(p[1]);
            Student reviewer = studentMap.get(p[2]);
            Student reviewee = studentMap.get(p[3]);
            if (e == null || reviewer == null || reviewee == null) continue;
            int stars = Integer.parseInt(p[4]);
            Review rv = new Review(p[0], e, reviewer, reviewee, stars, p[5]);
            state.getReviews().add(rv);
        }

        return state;
    }

    /**
     * {@inheritDoc}
     * <p>Scrittura tramite file temporaneo e rinomina atomica per ogni CSV.</p>
     *
     * @param state stato completo da serializzare
     */
    @Override
    public void save(SkillSwapState state) {
        saveStudents(state.getStudents());
        saveSkills(state.getSkills());
        saveOffers(state.getOffers());
        saveRequests(state.getRequests());
        saveExchanges(state.getExchanges());
        saveReviews(state.getReviews());
    }

    // ─── SAVE ─────────────────────────────────────────────────
    private void saveStudents(List<Student> list) {
        StringBuilder sb = new StringBuilder();
        for (Student s : list) {
            sb.append(s.getStudentId()).append(";")
              .append(s.getName()).append(";")
              .append(s.getClassName()).append(";")
              .append(s.getEmail()).append(";")
              .append(String.format("%.1f", s.getRatingAvg())).append(";")
              .append(s.getRatingCount()).append(";")
              .append(s.getPasswordHash()).append("\n");
        }
        writeFile("students.csv", sb.toString());
    }

    private void saveSkills(List<Skill> list) {
        StringBuilder sb = new StringBuilder();
        for (Skill sk : list) {
            sb.append(sk.getSkillId()).append(";")
              .append(sk.getName()).append(";")
              .append(sk.getCategory()).append("\n");
        }
        writeFile("skills.csv", sb.toString());
    }

    private void saveOffers(List<Offer> list) {
        StringBuilder sb = new StringBuilder();
        for (Offer o : list) {
            sb.append(o.getOfferId()).append(";")
              .append(o.getStudent().getStudentId()).append(";")
              .append(o.getSkill().getSkillId()).append(";")
              .append(o.getLevel()).append(";")
              .append(o.getNote()).append(";")
              .append(o.isActive()).append("\n");
        }
        writeFile("offers.csv", sb.toString());
    }

    private void saveRequests(List<Request> list) {
        StringBuilder sb = new StringBuilder();
        for (Request r : list) {
            sb.append(r.getRequestId()).append(";")
              .append(r.getStudent().getStudentId()).append(";")
              .append(r.getSkill().getSkillId()).append(";")
              .append(r.getMinLevel()).append(";")
              .append(r.getNote()).append("\n");
        }
        writeFile("requests.csv", sb.toString());
    }

    private void saveExchanges(List<Exchange> list) {
        StringBuilder sb = new StringBuilder();
        for (Exchange e : list) {
            sb.append(e.getExchangeId()).append(";")
              .append(e.getOffer().getOfferId()).append(";")
              .append(e.getRequest().getRequestId()).append(";")
              .append(e.getStatus()).append(";")
              .append(e.getCreatedAt()).append(";")
              .append(e.getClosedAt() != null ? e.getClosedAt() : "").append("\n");
        }
        writeFile("exchanges.csv", sb.toString());
    }

    private void saveReviews(List<Review> list) {
        StringBuilder sb = new StringBuilder();
        for (Review r : list) {
            sb.append(r.getReviewId()).append(";")
              .append(r.getExchange().getExchangeId()).append(";")
              .append(r.getReviewer().getStudentId()).append(";")
              .append(r.getReviewee().getStudentId()).append(";")
              .append(r.getStars()).append(";")
              .append(r.getComment()).append(";")
              .append(r.getCreatedAt()).append("\n");
        }
        writeFile("reviews.csv", sb.toString());
    }

    // ─── UTILITY ──────────────────────────────────────────────
    private List<String> readLines(String filename) {
        Path path = Paths.get(DATA_DIR + filename);
        if (!Files.exists(path)) return List.of();
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            System.out.println("Errore lettura " + filename + ": " + e.getMessage());
            return List.of();
        }
    }

    private void writeFile(String filename, String content) {
        Path tmp = Paths.get(DATA_DIR + filename + ".tmp");
        Path target = Paths.get(DATA_DIR + filename);
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            Files.writeString(tmp, content);
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("Errore scrittura " + filename + ": " + e.getMessage());
        }
    }
}