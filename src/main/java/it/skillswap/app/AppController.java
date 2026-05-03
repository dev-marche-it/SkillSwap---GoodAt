package it.skillswap.app;

import java.util.List;
import java.util.Scanner;

import it.skillswap.domain.Exchange;
import it.skillswap.domain.Offer;
import it.skillswap.domain.Request;
import it.skillswap.domain.Review;
import it.skillswap.domain.Skill;
import it.skillswap.domain.SkillCategory;
import it.skillswap.domain.SkillLevel;
import it.skillswap.domain.SkillSwapState;
import it.skillswap.domain.Student;
import it.skillswap.domain.exception.SkillSwapException;
import it.skillswap.service.ConsoleReportPrinter;
import it.skillswap.service.ExchangeService;
import it.skillswap.service.MatchResult;
import it.skillswap.service.MatchingService;
import it.skillswap.service.ReviewService;
import it.skillswap.storage.Storage;

/**
 * Front-end console interattivo: legge l'input utente, modifica {@link SkillSwapState}, delega ai service
 * e persiste tramite {@link Storage} all'uscita.
 */
public class AppController {
    private final SkillSwapState state;
    private final Storage storage;
    private final Scanner scanner;
    private final ExchangeService exchangeService;
    private final ReviewService reviewService;
    private final MatchingService matchingService;
    private final ConsoleReportPrinter printer;

    /**
     * @param state   stato applicativo caricato o vuoto
     * @param storage destinazione di {@link Storage#save} in uscita
     */
    public AppController(SkillSwapState state, Storage storage) {
        this.state = state;
        this.storage = storage;
        this.scanner = new Scanner(System.in);
        this.exchangeService = new ExchangeService(state);
        this.reviewService = new ReviewService(state);
        this.matchingService = new MatchingService(state);
        this.printer = new ConsoleReportPrinter();
    }

    /**
     * Esegue il ciclo del menu principale finché l'utente non sceglie l'uscita (opzione {@code 0}).
     */
    public void run() {
        System.out.println("=== SkillSwap School ===");
        boolean running = true;
        while (running) {
            printMenu();
            running = handleChoice(scanner.nextLine().trim());
        }
    }

    /**
     * @return {@code false} se l'utente ha scelto l'uscita (stato già salvato)
     */
    private boolean handleChoice(String choice) {
        return switch (choice) {
            case "1"  -> { handleCreaStudente(); yield true; }
            case "2"  -> { handleAggiungiSkill(); yield true; }
            case "3"  -> { handleAggiungiOffer(); yield true; }
            case "4"  -> { handleAggiungiRequest(); yield true; }
            case "5"  -> { handleListaStudenti(); yield true; }
            case "6"  -> { handleListaOffer(); yield true; }
            case "7"  -> { handleListaRequest(); yield true; }
            case "8"  -> { handleProponiExchange(); yield true; }
            case "9"  -> { handleAccettaExchange(); yield true; }
            case "10" -> { handleCompletaExchange(); yield true; }
            case "11" -> { handleCancellaExchange(); yield true; }
            case "12" -> { handleListaExchange(); yield true; }
            case "13" -> { handleAggiungiRecensione(); yield true; }
            case "14" -> { handleListaRecensioni(); yield true; }
            case "15" -> { handleProfiloStudente(); yield true; }
            case "16" -> { handleDettaglioExchange(); yield true; }
            case "17" -> { handleLeaderboard(); yield true; }
            case "18" -> { handleMatchOneWay(); yield true; }
            case "19" -> { handleMatchSwap(); yield true; }
            case "0"  -> { persistAndExit(); yield false; }
            default -> { invalidChoice(); yield true; }
        };
    }

    private void persistAndExit() {
        storage.save(state);
        System.out.println("Arrivederci!");
    }

    private void invalidChoice() {
        System.out.println("Scelta non valida.");
    }

    private void printMenu() {
        System.out.println("\n--- Menu ---");
        System.out.println("1.  Crea studente");
        System.out.println("2.  Aggiungi skill");
        System.out.println("3.  Aggiungi offer");
        System.out.println("4.  Aggiungi request");
        System.out.println("5.  Lista studenti");
        System.out.println("6.  Lista offer");
        System.out.println("7.  Lista request");
        System.out.println("8.  Proponi exchange");
        System.out.println("9.  Accetta exchange");
        System.out.println("10. Completa exchange");
        System.out.println("11. Cancella exchange");
        System.out.println("12. Lista exchange");
        System.out.println("13. Aggiungi recensione");
        System.out.println("14. Recensioni di uno studente");
        System.out.println("15. Profilo studente");
        System.out.println("16. Dettaglio exchange");
        System.out.println("17. Leaderboard");
        System.out.println("18. Match one-way (offerte per le tue request)");
        System.out.println("19. Match swap (scambi reciproci)");
        System.out.println("0.  Esci");
        System.out.print("Scelta: ");
    }

    // ─── STUDENTI ─────────────────────────────────────────────
    private void handleCreaStudente() {
        System.out.print("Nome: ");
        String name = scanner.nextLine().trim();
        System.out.print("Classe: ");
        String className = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        String id = "S" + (state.getStudents().size() + 1);

        Student s = new Student(id, name, className, email);
        state.getStudents().add(s);
        System.out.println("Studente aggiunto: " + s);
    }

    private void handleListaStudenti() {
        if (state.getStudents().isEmpty()) {
            System.out.println("Nessuno studente registrato.");
            return;
        }
        state.getStudents().forEach(System.out::println);
    }

    // ─── SKILL ────────────────────────────────────────────────
    private void handleAggiungiSkill() {
        System.out.print("Nome: ");
        String name = scanner.nextLine().trim();
        System.out.print("Categoria: ");
        String categoryStr = scanner.nextLine().trim();
        String id = "K" + (state.getSkills().size() + 1);

        try {
            SkillCategory category = SkillCategory.fromString(categoryStr);
            Skill skill = new Skill(id, name, category);
            state.getSkills().add(skill);
            System.out.println("Skill aggiunta: " + skill);
        } catch (IllegalArgumentException ex) {
            System.out.println("Categoria non valida: " + categoryStr);
        }
    }

    // ─── OFFER ────────────────────────────────────────────────
    private void handleAggiungiOffer() {
        System.out.print("ID studente: ");
        String studentId = scanner.nextLine().trim();
        System.out.print("ID skill: ");
        String skillId = scanner.nextLine().trim();
        System.out.print("Livello (BEGINNER/INTERMEDIATE/ADVANCED): ");
        String levelStr = scanner.nextLine().trim();
        System.out.print("Note: ");
        String note = scanner.nextLine().trim();
        String id = "O" + (state.getOffers().size() + 1);

        Student student = findStudentById(studentId);
        Skill skill = findSkillById(skillId);
        if (student == null || skill == null) {
            System.out.println("Studente o skill non trovati.");
            return;
        }

        try {
            SkillLevel level = SkillLevel.fromString(levelStr);
            Offer offer = new Offer(id, student, skill, level, note);
            state.getOffers().add(offer);
            System.out.println("Offer aggiunta: " + offer);
        } catch (IllegalArgumentException ex) {
            System.out.println("Livello non valido: " + levelStr);
        }
    }

    private void handleListaOffer() {
        if (state.getOffers().isEmpty()) {
            System.out.println("Nessuna offer registrata.");
            return;
        }
        state.getOffers().forEach(System.out::println);
    }

    // ─── REQUEST ──────────────────────────────────────────────
    private void handleAggiungiRequest() {
        System.out.print("ID studente: ");
        String studentId = scanner.nextLine().trim();
        System.out.print("ID skill: ");
        String skillId = scanner.nextLine().trim();
        System.out.print("Livello minimo (BEGINNER/INTERMEDIATE/ADVANCED): ");
        String minLevelStr = scanner.nextLine().trim();
        System.out.print("Note: ");
        String note = scanner.nextLine().trim();
        String id = "R" + (state.getRequests().size() + 1);

        Student student = findStudentById(studentId);
        Skill skill = findSkillById(skillId);
        if (student == null || skill == null) {
            System.out.println("Studente o skill non trovati.");
            return;
        }

        try {
            SkillLevel minLevel = SkillLevel.fromString(minLevelStr);
            Request request = new Request(id, student, skill, minLevel, note);
            state.getRequests().add(request);
            System.out.println("Request aggiunta: " + request);
        } catch (IllegalArgumentException ex) {
            System.out.println("Livello minimo non valido: " + minLevelStr);
        }
    }

    private void handleListaRequest() {
        if (state.getRequests().isEmpty()) {
            System.out.println("Nessuna request registrata.");
            return;
        }
        state.getRequests().forEach(System.out::println);
    }

    // ─── EXCHANGE ─────────────────────────────────────────────
    private void handleProponiExchange() {
        System.out.print("ID offer: ");
        String offerId = scanner.nextLine().trim();
        System.out.print("ID request: ");
        String requestId = scanner.nextLine().trim();
        String id = "E" + (state.getExchanges().size() + 1);

        try {
            Exchange e = exchangeService.propose(id, offerId, requestId);
            System.out.println("Exchange proposto: " + e);
        } catch (SkillSwapException ex) {
            System.out.println("Errore di dominio: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Errore: " + ex.getMessage());
        }
    }

    private void handleAccettaExchange() {
        System.out.print("ID exchange: ");
        String id = scanner.nextLine().trim();
        try {
            Exchange e = exchangeService.accept(id);
            System.out.println("Exchange accettato: " + e);
        } catch (SkillSwapException ex) {
            System.out.println("Errore di dominio: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Errore: " + ex.getMessage());
        }
    }

    private void handleCompletaExchange() {
        System.out.print("ID exchange: ");
        String id = scanner.nextLine().trim();
        try {
            Exchange e = exchangeService.complete(id);
            System.out.println("Exchange completato: " + e);
        } catch (SkillSwapException ex) {
            System.out.println("Errore di dominio: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Errore: " + ex.getMessage());
        }
    }

    private void handleCancellaExchange() {
        System.out.print("ID exchange: ");
        String id = scanner.nextLine().trim();
        try {
            Exchange e = exchangeService.cancel(id);
            System.out.println("Exchange cancellato: " + e);
        } catch (SkillSwapException ex) {
            System.out.println("Errore di dominio: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Errore: " + ex.getMessage());
        }
    }

    private void handleListaExchange() {
        if (state.getExchanges().isEmpty()) {
            System.out.println("Nessun exchange registrato.");
            return;
        }
        state.getExchanges().forEach(System.out::println);
    }

    // ─── REVIEW ───────────────────────────────────────────────
    private void handleAggiungiRecensione() {
        System.out.print("ID exchange: ");
        String exchangeId = scanner.nextLine().trim();
        System.out.print("ID studente recensore: ");
        String reviewerId = scanner.nextLine().trim();
        System.out.print("Stelle (1-5): ");
        int stars = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Commento: ");
        String comment = scanner.nextLine().trim();
        String id = "V" + (state.getReviews().size() + 1);

        try {
            Review r = reviewService.addReview(id, exchangeId, reviewerId, stars, comment);
            System.out.println("Recensione aggiunta: " + r);
        } catch (SkillSwapException ex) {
            System.out.println("Errore di dominio: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Errore: " + ex.getMessage());
        }
    }

    private void handleListaRecensioni() {
        System.out.print("ID studente: ");
        String studentId = scanner.nextLine().trim();
        List<Review> reviews = reviewService.getReviewsForStudent(studentId);
        if (reviews.isEmpty()) {
            System.out.println("Nessuna recensione trovata.");
            return;
        }
        reviews.forEach(System.out::println);
    }

    // ─── REPORT ───────────────────────────────────────────────
    private void handleProfiloStudente() {
        System.out.print("ID studente: ");
        String id = scanner.nextLine().trim();
        Student student = findStudentById(id);
        if (student == null) {
            System.out.println("Studente non trovato.");
            return;
        }
        List<Review> reviews = reviewService.getReviewsForStudent(id);
        System.out.println(printer.printStudentProfile(student, reviews));
    }

    private void handleDettaglioExchange() {
        System.out.print("ID exchange: ");
        String id = scanner.nextLine().trim();
        Exchange exchange = state.getExchanges().stream()
                .filter(e -> e.getExchangeId().equals(id))
                .findFirst().orElse(null);
        if (exchange == null) {
            System.out.println("Exchange non trovato.");
            return;
        }
        System.out.println(printer.printExchangeDetails(exchange));
    }

    private void handleLeaderboard() {
        System.out.println(printer.printLeaderboard(state.getStudents()));
    }

    private void handleMatchOneWay() {
        System.out.print("ID studente: ");
        String studentId = scanner.nextLine().trim();
        if (findStudentById(studentId) == null) {
            System.out.println("Studente non trovato.");
            return;
        }
        List<MatchResult> matches = matchingService.findOneWayMatches(studentId);
        System.out.println(printer.printMatches(matches));
    }

    private void handleMatchSwap() {
        System.out.print("ID studente: ");
        String studentId = scanner.nextLine().trim();
        if (findStudentById(studentId) == null) {
            System.out.println("Studente non trovato.");
            return;
        }
        List<MatchResult> matches = matchingService.findSwapMatches(studentId);
        System.out.println(printer.printMatches(matches));
    }

    // ─── UTILITY ──────────────────────────────────────────────
    private Student findStudentById(String id) {
        return state.getStudents().stream()
                .filter(s -> s.getStudentId().equals(id))
                .findFirst().orElse(null);
    }

    private Skill findSkillById(String id) {
        return state.getSkills().stream()
                .filter(sk -> sk.getSkillId().equals(id))
                .findFirst().orElse(null);
    }
}