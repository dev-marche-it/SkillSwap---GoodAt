package it.skillswap.app;

import it.skillswap.domain.*;
import it.skillswap.service.ExchangeService;
import it.skillswap.service.ReviewService;
import it.skillswap.storage.InMemoryStorage;
import it.skillswap.storage.Storage;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static Storage storage = new InMemoryStorage();
    private static SkillSwapState state = storage.load();
    private static Scanner scanner = new Scanner(System.in);
    private static ExchangeService exchangeService = new ExchangeService(state);
    private static ReviewService reviewService = new ReviewService(state);

    public static void main(String[] args) {
        System.out.println("=== SkillSwap School ===");
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> creaStudente();
                case "2" -> aggiungiSkill();
                case "3" -> aggiungiOffer();
                case "4" -> aggiungiRequest();
                case "5" -> listaStudenti();
                case "6" -> listaOffer();
                case "7" -> listaRequest();
                case "8" -> proponiExchange();
                case "9" -> accettaExchange();
                case "10" -> completaExchange();
                case "11" -> cancellaExchange();
                case "12" -> listaExchange();
                case "13" -> aggiungiRecensione();
                case "14" -> listaRecensioni();
                case "0" -> {
                    storage.save(state);
                    System.out.println("Arrivederci!");
                    running = false;
                }
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    private static void printMenu() {
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
        System.out.println("0.  Esci");
        System.out.print("Scelta: ");
    }

    private static void creaStudente() {
        System.out.print("ID studente: ");
        String id = scanner.nextLine().trim();
        System.out.print("Nome: ");
        String name = scanner.nextLine().trim();
        System.out.print("Classe: ");
        String className = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        Student s = new Student(id, name, className, email);
        state.getStudents().add(s);
        System.out.println("Studente aggiunto: " + s);
    }

    private static void aggiungiSkill() {
        System.out.print("ID skill: ");
        String id = scanner.nextLine().trim();
        System.out.print("Nome: ");
        String name = scanner.nextLine().trim();
        System.out.print("Categoria: ");
        String category = scanner.nextLine().trim();

        Skill skill = new Skill(id, name, category);
        state.getSkills().add(skill);
        System.out.println("Skill aggiunta: " + skill);
    }

    private static void aggiungiOffer() {
        System.out.print("ID offer: ");
        String id = scanner.nextLine().trim();
        System.out.print("ID studente: ");
        String studentId = scanner.nextLine().trim();
        System.out.print("ID skill: ");
        String skillId = scanner.nextLine().trim();
        System.out.print("Livello (BEGINNER/INTERMEDIATE/ADVANCED): ");
        String level = scanner.nextLine().trim();
        System.out.print("Note: ");
        String note = scanner.nextLine().trim();

        Student student = state.getStudents().stream()
                .filter(s -> s.getStudentId().equals(studentId))
                .findFirst().orElse(null);
        Skill skill = state.getSkills().stream()
                .filter(sk -> sk.getSkillId().equals(skillId))
                .findFirst().orElse(null);

        if (student == null || skill == null) {
            System.out.println("Studente o skill non trovati.");
            return;
        }

        Offer offer = new Offer(id, student, skill, level, note);
        state.getOffers().add(offer);
        System.out.println("Offer aggiunta: " + offer);
    }

    private static void aggiungiRequest() {
        System.out.print("ID request: ");
        String id = scanner.nextLine().trim();
        System.out.print("ID studente: ");
        String studentId = scanner.nextLine().trim();
        System.out.print("ID skill: ");
        String skillId = scanner.nextLine().trim();
        System.out.print("Livello minimo (BEGINNER/INTERMEDIATE/ADVANCED): ");
        String minLevel = scanner.nextLine().trim();
        System.out.print("Note: ");
        String note = scanner.nextLine().trim();

        Student student = state.getStudents().stream()
                .filter(s -> s.getStudentId().equals(studentId))
                .findFirst().orElse(null);
        Skill skill = state.getSkills().stream()
                .filter(sk -> sk.getSkillId().equals(skillId))
                .findFirst().orElse(null);

        if (student == null || skill == null) {
            System.out.println("Studente o skill non trovati.");
            return;
        }

        Request request = new Request(id, student, skill, minLevel, note);
        state.getRequests().add(request);
        System.out.println("Request aggiunta: " + request);
    }

    private static void listaStudenti() {
        if (state.getStudents().isEmpty()) {
            System.out.println("Nessuno studente registrato.");
            return;
        }
        state.getStudents().forEach(System.out::println);
    }

    private static void listaOffer() {
        if (state.getOffers().isEmpty()) {
            System.out.println("Nessuna offer registrata.");
            return;
        }
        state.getOffers().forEach(System.out::println);
    }

    private static void listaRequest() {
        if (state.getRequests().isEmpty()) {
            System.out.println("Nessuna request registrata.");
            return;
        }
        state.getRequests().forEach(System.out::println);
    }

    private static void proponiExchange() {
        System.out.print("ID exchange: ");
        String id = scanner.nextLine().trim();
        System.out.print("ID offer: ");
        String offerId = scanner.nextLine().trim();
        System.out.print("ID request: ");
        String requestId = scanner.nextLine().trim();

        try {
            Exchange e = exchangeService.propose(id, offerId, requestId);
            System.out.println("Exchange proposto: " + e);
        } catch (Exception ex) {
            System.out.println("Errore: " + ex.getMessage());
        }
    }

    private static void accettaExchange() {
        System.out.print("ID exchange: ");
        String id = scanner.nextLine().trim();
        try {
            Exchange e = exchangeService.accept(id);
            System.out.println("Exchange accettato: " + e);
        } catch (Exception ex) {
            System.out.println("Errore: " + ex.getMessage());
        }
    }

    private static void completaExchange() {
        System.out.print("ID exchange: ");
        String id = scanner.nextLine().trim();
        try {
            Exchange e = exchangeService.complete(id);
            System.out.println("Exchange completato: " + e);
        } catch (Exception ex) {
            System.out.println("Errore: " + ex.getMessage());
        }
    }

    private static void cancellaExchange() {
        System.out.print("ID exchange: ");
        String id = scanner.nextLine().trim();
        try {
            Exchange e = exchangeService.cancel(id);
            System.out.println("Exchange cancellato: " + e);
        } catch (Exception ex) {
            System.out.println("Errore: " + ex.getMessage());
        }
    }

    private static void listaExchange() {
        if (state.getExchanges().isEmpty()) {
            System.out.println("Nessun exchange registrato.");
            return;
        }
        state.getExchanges().forEach(System.out::println);
    }

    private static void aggiungiRecensione() {
        System.out.print("ID recensione: ");
        String id = scanner.nextLine().trim();
        System.out.print("ID exchange: ");
        String exchangeId = scanner.nextLine().trim();
        System.out.print("ID studente recensore: ");
        String reviewerId = scanner.nextLine().trim();
        System.out.print("Stelle (1-5): ");
        int stars = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Commento: ");
        String comment = scanner.nextLine().trim();

        try {
            Review r = reviewService.addReview(id, exchangeId, reviewerId, stars, comment);
            System.out.println("Recensione aggiunta: " + r);
        } catch (Exception ex) {
            System.out.println("Errore: " + ex.getMessage());
        }
    }

    private static void listaRecensioni() {
        System.out.print("ID studente: ");
        String studentId = scanner.nextLine().trim();
        List<Review> reviews = reviewService.getReviewsForStudent(studentId);
        if (reviews.isEmpty()) {
            System.out.println("Nessuna recensione trovata.");
            return;
        }
        reviews.forEach(System.out::println);
    }
}