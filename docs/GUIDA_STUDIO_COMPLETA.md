# SkillSwap GoodAt — Guida di studio completa

> **Tipo documento:** manuale didattico per lo studio integrale del progetto  
> **Pubblico:** studenti, revisori, docenti  
> **Versione:** 2.0 — maggio 2026 (include layer web Spring Boot + UI)  
> **Progetto Maven:** `it.skillswap:skillswap-goodat:0.0.1-SNAPSHOT` · Java 17

---

## Come usare questa guida

| Se vuoi… | Vai a… |
|----------|--------|
| Capire *cos’è* il progetto in 10 minuti | [§1](#1-visione-e-contesto) + [§2](#2-mappa-completa-del-repository) |
| Studiare il modello dati | [§4](#4-modello-di-dominio-studio-dettagliato) |
| Capire matching, scambi, recensioni | [§5](#5-logica-applicativa-i-service) |
| Imparare persistenza CSV | [§6](#6-persistenza) |
| Usare CLI o Web | [§7](#7-interfacce-utente) · [§8](#8-layer-web-spring-boot) |
| Preparare un’interrogazione | [§12](#12-domande-di-autoverifica) · [§13](#13-esercizi-di-studio-consigliati) |
| Dettaglio tecnico file-per-file | `DOCUMENTAZIONE_COMPLETA.md` (riferimento tecnico) |
| Milestone storiche di sviluppo | `GUIDELINE.md` |

---

## Indice generale

1. [Visione e contesto](#1-visione-e-contesto)
2. [Mappa completa del r    epository](#2-mappa-completa-del-repository)
3. [Architettura software](#3-architettura-software)
4. [Modello di dominio (studio dettagliato)](#4-modello-di-dominio-studio-dettagliato)
5. [Logica applicativa: i Service](#5-logica-applicativa-i-service)
6. [Persistenza](#6-persistenza)
7. [Interfacce utente](#7-interfacce-utente)
8. [Layer Web Spring Boot](#8-layer-web-spring-boot)
9. [Frontend statico](#9-frontend-statico)
10. [Suite di test](#10-suite-di-test)
11. [Documentazione e cartelle ausiliarie](#11-documentazione-e-cartelle-ausiliarie)
12. [Domande di autoverifica](#12-domande-di-autoverifica)
13. [Esercizi di studio consigliati](#13-esercizi-di-studio-consigliati)
14. [Percorso di studio in 7 giorni](#14-percorso-di-studio-in-7-giorni)
15. [Glossario esteso](#15-glossario-esteso)
16. [Appendice A — API REST](#appendice-a--api-rest-completa)
17. [Appendice B — Menu CLI](#appendice-b--menu-cli-completo)
18. [Appendice C — Inventario classi Java](#appendice-c--inventario-classi-java)

---

# 1. Visione e contesto

## 1.1 Problema affrontato

Nelle scuole, molti studenti hanno competenze complementari: uno è forte in matematica, un altro in informatica. **SkillSwap School** formalizza questo scambio:

- chi **sa** qualcosa pubblica un’**Offerta** (`Offer`);
- chi **vuole imparare** pubblica una **Richiesta** (`Request`);
- il sistema **abbina** le coppie (matching);
- due studenti avviano uno **Scambio** (`Exchange`);
- al termine lasciano una **Recensione** (`Review`) che alimenta un **rating** e una **classifica**.

## 1.2 Obiettivi didattici del codice

Studiando questo repository impari:

| Area | Cosa pratichi |
|------|----------------|
| **OOP / dominio** | Entità, enum, eccezioni custom, aggregato di stato |
| **Service layer** | Regole di business separate dalla UI |
| **Pattern** | Repository-like (`Storage`), DTO, state machine |
| **Persistenza** | Serializzazione su file CSV, scrittura atomica |
| **CLI** | Controller testuale, menu, validazione input |
| **Web** | Spring Boot REST, JSON, SPA statica |
| **Test** | JUnit 5, Given-When-Then |

## 1.3 Due modalità di esecuzione

```
┌─────────────────────┐     ┌─────────────────────┐
│  mvn exec:java      │     │  mvn spring-boot:run │
│  Main → AppController│     │  SkillSwapWebApplication │
│  (CLI)              │     │  + browser :8080     │
└──────────┬──────────┘     └──────────┬──────────┘
           │                           │
           └───────────┬───────────────┘
                       ▼
              FileStorage → data/*.csv
              (stesso backend di dominio e service)
```

**Regola importante:** non eseguire CLI e server web insieme: entrambi scrivono `data/` e si sovrascrivono.

---

# 2. Mappa completa del repository

```
SkillSwap---GoodAt/
│
├── pom.xml                          # Maven: Spring Boot 3.3.5, Java 17, JUnit, Lombok
├── README.md                        # Introduzione rapida e comandi
│
├── data/                            # Persistenza runtime (CSV)
│   ├── students.csv
│   ├── skills.csv
│   ├── offers.csv
│   ├── requests.csv
│   ├── exchanges.csv
│   └── reviews.csv
│
├── docs/                            # Documentazione
│   ├── GUIDA_STUDIO_COMPLETA.md     # ← questo file (studio)
│   ├── DOCUMENTAZIONE_COMPLETA.md   # riferimento tecnico approfondito
│   ├── GUIDELINE.md                 # milestone M1–M6 originali
│   ├── Web_integration.md           # roadmap Flask (alternativa storica)
│   ├── PROPLAN.md
│   ├── MAJOR_FIXES.md
│   └── IMPROVEMENTS_AND_FIXES_1.md
│
├── web/                             # Prototipo Flask (NON usato in produzione attuale)
│   ├── app.py                       # Server minimale fase W1
│   ├── config.py
│   ├── requirements.txt
│   └── README_WEB.md                # Mappa API (valida anche per Spring)
│
├── test/                            # Test JUnit (cartella root, non src/test/java)
│   ├── MatchingServiceTest.java
│   ├── ExchangeServiceTest.java
│   ├── ReviewServiceTest.java
│   ├── FileStorageTest.java
│   └── ValidatorTest.java
│
└── src/main/
    ├── java/it/skillswap/
    │   ├── app/                     # CLI
    │   │   ├── Main.java
    │   │   └── AppController.java
    │   ├── domain/                  # Modello
    │   │   ├── Student, Skill, Offer, Request, Exchange, Review
    │   │   ├── SkillSwapState.java
    │   │   ├── SkillCategory, SkillLevel, ExchangeStatus
    │   │   └── exception/
    │   ├── service/                 # Business logic
    │   │   ├── MatchingService, MatchResult
    │   │   ├── ExchangeService, ReviewService
    │   │   ├── Validator, ValidationResult
    │   │   └── ConsoleReportPrinter
    │   ├── storage/
    │   │   ├── Storage.java
    │   │   ├── FileStorage.java
    │   │   └── InMemoryStorage.java
    │   └── web/                     # Spring Boot
    │       ├── SkillSwapWebApplication.java
    │       ├── ApplicationState.java
    │       └── api/
    │           ├── *Controller.java (9 controller)
    │           ├── dto/             # Record JSON
    │           ├── IdGenerator.java
    │           ├── ApiExceptionHandler.java
    │           └── ErrorResponse.java
    │
    └── resources/
        ├── application.properties   # server.port=8080
        └── static/                  # Frontend web
            ├── index.html
            ├── css/app.css
            └── js/api.js, app.js
```

---

# 3. Architettura software

## 3.1 Layer e dipendenze

```
┌──────────────────────────────────────────────────────────────┐
│ PRESENTAZIONE                                                 │
│  • AppController (CLI, 19 voci menu)                          │
│  • ConsoleReportPrinter                                       │
│  • *Controller Spring + static/index.html                     │
└────────────────────────────┬─────────────────────────────────┘
                             │ chiama
┌────────────────────────────▼─────────────────────────────────┐
│ SERVICE (regole di business)                                    │
│  MatchingService | ExchangeService | ReviewService            │
│  Validator (utility, usato soprattutto nei test)              │
└────────────────────────────┬─────────────────────────────────┘
                             │ legge/scrive
┌────────────────────────────▼─────────────────────────────────┐
│ DOMINIO + STATO                                               │
│  SkillSwapState + entità collegate per riferimento            │
└────────────────────────────┬─────────────────────────────────┘
                             │ persistito da
┌────────────────────────────▼─────────────────────────────────┐
│ STORAGE                                                       │
│  FileStorage (CSV)  |  InMemoryStorage (test)                 │
└──────────────────────────────────────────────────────────────┘
```

## 3.2 Principi chiave

1. **Un solo stato in memoria:** `SkillSwapState` contiene tutte le liste; i service ricevono lo stesso riferimento.
2. **Oggetti condivisi:** `Offer` contiene riferimenti a `Student` e `Skill` reali, non solo ID — coerenza referenziale in JVM.
3. **Service senza persistenza interna:** dopo ogni mutazione, chi invoca (CLI o Web) deve chiamare `storage.save(state)`.
4. **Eccezioni di dominio:** errori prevedibili → `SkillSwapException` e sottoclassi.

## 3.3 Pattern riconoscibili

| Pattern | Dove |
|---------|------|
| **Aggregate Root** | `SkillSwapState` |
| **Service Layer** | `MatchingService`, `ExchangeService`, `ReviewService` |
| **Repository** | interfaccia `Storage` |
| **DTO** | record in `it.skillswap.web.api.dto` |
| **State Machine** | `ExchangeStatus` + `ExchangeService` |
| **Strategy (soft/strict)** | `Validator` + `ValidationResult` |

---

# 4. Modello di dominio (studio dettagliato)

## 4.1 Diagramma entità-relazioni       

```
     Student ─────┬──────────────┐
                  │              │
                  ▼              ▼
     Skill ──► Offer         Request
                  │              │
                  └──────┬───────┘
                         ▼
                    Exchange ──────► Review
                         │
                    (rating aggiorna Student reviewee)
```

## 4.2 `Student`

**File:** `domain/Student.java`

| Campo | Tipo | Note |
|-------|------|------|
| `studentId` | `String` | Immutabile (es. `S1`) |
| `name`, `className`, `email` | `String` | Immutabili |
| `ratingAvg` | `double` | Media recensioni, parte da 0 |
| `ratingCount` | `int` | Numero voti nella media |

**Metodo da memorizzare — `addRating(int stars)`:**

\[
\text{nuova media} = \frac{\text{ratingAvg} \times \text{ratingCount} + \text{stars}}{\text{ratingCount} + 1}
\]

Poi `ratingCount++`. Usato da `ReviewService` dopo ogni recensione valida.

## 4.3 `Skill` e `SkillCategory`

**Skill:** `skillId`, `name`, `category` (enum).

**SkillCategory:** `SUBJECT`, `LANGUAGE`, `SPORT`, `ART`, `OTHER`  
`fromString(s)` — case-insensitive; `null` → `SUBJECT`.

## 4.4 `SkillLevel`

**Valori ordinati:** `BEGINNER` (0) < `INTERMEDIATE` (1) < `ADVANCED` (2).

**Metodo `isSufficientFor(minLevel)`:**  
`this.ordinal() >= minLevel.ordinal()` — l’offerta copre il livello minimo richiesto?

## 4.5 `Offer`

| Campo | Significato |
|-------|-------------|
| `student` | Chi insegna |
| `skill` | Cosa insegna |
| `level` | A che livello |
| `note` | Testo libero |
| `active` | Se `false`, non partecipa a nuovi match né a `propose` |

**Regola:** `ExchangeService.complete` imposta `offer.setActive(false)`.

## 4.6 `Request`

| Campo | Significato |
|-------|-------------|
| `student` | Chi chiede aiuto |
| `skill` | Cosa vuole imparare |
| `minLevel` | Livello minimo accettabile del tutor |
| `note` | Testo libero |

## 4.7 `Exchange` e macchina a stati

```
PROPOSED ──accept()──► ACCEPTED ──complete()──► COMPLETED
    │
    └──cancel()──► CANCELLED
```

| Stato | Significato operativo |
|-------|----------------------|
| `PROPOSED` | Proposta inviata, in attesa |
| `ACCEPTED` | Entrambi hanno accettato, scambio in corso |
| `COMPLETED` | Chiuso con successo → si possono scrivere recensioni |
| `CANCELLED` | Annullato mentre era solo proposto |

**Campi temporali:** `createdAt` al costruttore; `closedAt` quando stato → `COMPLETED` o `CANCELLED`.

## 4.8 `Review`

| Campo | Significato |
|-------|-------------|
| `reviewer` | Chi scrive |
| `reviewee` | Chi riceve il voto (l’altro partecipante) |
| `stars` | 1–5 |
| `exchange` | Scambio completato |

**Vincoli (`ReviewService`):**
- Exchange deve essere `COMPLETED`;
- `reviewer` deve essere offer-side o request-side;
- una sola recensione per coppia `(exchangeId, reviewerId)`.

## 4.9 `SkillSwapState`

Contenitore con **6 liste mutabili** (`ArrayList`). Non usa `Map` per ID: le ricerche per id sono stream/filter nei service.

## 4.10 Eccezioni (`domain/exception/`)

| Classe | Quando |
|--------|--------|
| `SkillSwapException` | Base runtime |
| `OfferNotActiveException` | Proposta su offerta disattivata |
| `InvalidStateTransitionException` | Transizione exchange illegale |
| `InvalidStarsException` | Stelle ∉ [1,5] |
| `DuplicateReviewException` | Seconda recensione stesso reviewer/exchange |
| `StudentNotFoundException` | Definita ma non usata dai service attuali |

---

# 5. Logica applicativa: i Service

## 5.1 `MatchingService` — algoritmo di punteggio

### One-way (`findOneWayMatches(studentId)`)

Per ogni **Request** dello studente, cerca **Offer** di altri tali che:

1. `offer.active == true`
2. `offer.student ≠ seeker`
3. `offer.skill == request.skill` (stesso `skillId`)

**Punteggio:**

| Criterio | Punti |
|----------|-------|
| Skill identica | +3 |
| `offer.level` sufficiente per `request.minLevel` | +2 |
| Stessa `className` | +1 |

**Massimo:** 6. Risultato ordinato per score decrescente → `MatchResult(offerId, requestId, score, reason)`.

### Swap (`findSwapMatches(studentId)`)

Cerca due studenti A (seeker) e B tali che:

- B offre ciò che A richiede;
- A offre ciò che B richiede.

Score = `calculateScore(offerB, requestA) + calculateScore(offerA, requestB)` — massimo teorico **12**.

### Esempio con dati demo attuali

| Studente | Offer | Request |
|----------|-------|---------|
| Alessandro (S1) | K2 Java BEGINNER (O2 inactive, O3 active) | K1 Matematica BEGINNER (R1) |
| Riccardo (S2) | K1 Matematica INTERMEDIATE (O1) | K2 Java BEGINNER (R2) |

**Match one-way per Alessandro:** offerta O1 di Riccardo su skill K1 soddisfa R1 → score tipico 6 (stessa classe 4A).

**Swap:** Alessandro offre Java, cerca Matematica; Riccardo offre Matematica, cerca Java → match swap possibile.

## 5.2 `ExchangeService`

| Metodo | Precondizione | Effetto |
|--------|---------------|---------|
| `propose` | Offer attiva; studenti distinti | Crea `PROPOSED` |
| `accept` | Stato `PROPOSED` | → `ACCEPTED` |
| `complete` | Stato `ACCEPTED` | → `COMPLETED`, offer inactive |
| `cancel` | Stato `PROPOSED` | → `CANCELLED` |

## 5.3 `ReviewService`

| Metodo | Descrizione |
|--------|-------------|
| `addReview` | Crea review, `reviewee.addRating(stars)` |
| `getReviewsForStudent` | Filtra per `reviewee.studentId` |

## 5.4 `Validator` e `ValidationResult`

Validazione **soft** (`ValidationResult`) vs **strict** (eccezione).  
Usata nei test; la CLI non la invoca su ogni input.

## 5.5 `ConsoleReportPrinter`

Formattazione testo per CLI: profilo, match, dettaglio exchange, leaderboard (solo studenti con `ratingCount > 0`).

---

# 6. Persistenza

## 6.1 Interfaccia `Storage`

```java
SkillSwapState load();
void save(SkillSwapState state);
```

## 6.2 `FileStorage`

- Directory: `data/` (relativa alla working directory JVM).
- Separatore: **`;`**
- Scrittura: file `.tmp` poi rename atomico.
- Lettura: righe malformate saltate; file assente → vuoto.
- Errori I/O: stampati su stdout, **nessuna eccezione**.

### Ordine di caricamento

`students` → `skills` → `offers` → `requests` → `exchanges` → `reviews`

### Schema colonne (memorizza per l’esame)

| File | Colonne |
|------|---------|
| `students.csv` | id; nome; classe; email; rating_avg; rating_count |
| `skills.csv` | id; nome; category |
| `offers.csv` | id; student_id; skill_id; level; note; active |
| `requests.csv` | id; student_id; skill_id; min_level; note |
| `exchanges.csv` | id; offer_id; request_id; status; created_at; closed_at |
| `reviews.csv` | id; exchange_id; reviewer_id; reviewee_id; stars; comment; created_at |

### Limitazioni da conoscere

- Timestamp `createdAt`/`closedAt` su exchange/review in **load** non sempre riletti dal CSV (costruttore usa `now()`).
- Nessuna intestazione header nei file scritti da Java.
- Nessun lock: due processi che salvano → rischio corruzione.

## 6.3 `InMemoryStorage`

Per test unitari: tiene un riferimento a `SkillSwapState`, `save` sostituisce il riferimento.

---

# 7. Interfacce utente

## 7.1 CLI — `Main` + `AppController`

**Avvio:** `mvn exec:java` → `Main` → `FileStorage.load()` → menu loop.

**Salvataggio:** solo all’uscita (opzione `0`), non dopo ogni comando.

**Generazione ID:** `S` + (students.size()+1), analogamente `K`, `O`, `R`, `E`, `V`.

**Deleghe:**
- CRUD semplici (studenti, skill, offer, request) → modifica diretta liste;
- Exchange e Review → `ExchangeService` / `ReviewService`.

Vedi [Appendice B](#appendice-b--menu-cli-completo) per l’elenco completo delle 20 voci.

## 7.2 Web — panoramica

**Avvio:** `mvn spring-boot:run` → http://localhost:8080  
**Login:** email da `students.csv` (demo: `alessandro@scuola.it`, `riccardo@scuola.it`)  
**Salvataggio:** dopo ogni mutazione API (`ApplicationState.persist()`).

---

# 8. Layer Web Spring Boot

## 8.1 Componenti

| Classe | Ruolo |
|--------|-------|
| `SkillSwapWebApplication` | `@SpringBootApplication`, scan `it.skillswap` |
| `ApplicationState` | `@Component`: carica stato, espone 3 service, `persist()` |
| `*Controller` | REST JSON sotto `/api` |
| `ApiExceptionHandler` | `@RestControllerAdvice` → `{ "error": "..." }` |
| `IdGenerator` | Stessi prefissi ID della CLI |
| `*Dto` | Record per risposte JSON senza cicli di riferimento |

## 8.2 Flusso richiesta HTTP

```
Browser fetch /api/...
    → Controller
    → ApplicationState.getXxxService()
    → modifica SkillSwapState
    → ApplicationState.persist()
    → FileStorage.save()
    → JSON Response
```

## 8.3 `ApplicationState` (cuore integrazione web)

```java
// Costruttore (semplificato)
storage = new FileStorage();
state = storage.load();
matchingService = new MatchingService(state);
exchangeService = new ExchangeService(state);
reviewService = new ReviewService(state);
```

**Nota didattica:** i service **non sono bean Spring** — sono istanziati manualmente. Spring gestisce solo i controller e `ApplicationState`. Questo evita di modificare il codice domain/service esistente.

---

# 9. Frontend statico

## 9.1 File

| File | Contenuto |
|------|-----------|
| `index.html` | Shell: login, sidebar, 5 pagine, modale recensione |
| `css/app.css` | Design system (teal/indaco, Plus Jakarta Sans) |
| `js/api.js` | Wrapper `fetch` verso `/api` |
| `js/app.js` | Logica UI, sessionStorage, rendering |

## 9.2 Pagine

| Pagina | Funzione |
|--------|----------|
| **Home** | Hero, statistiche, scorciatoie |
| **Trova match** | One-way / swap, proponi scambio |
| **I miei scambi** | Accetta, completa, recensisci |
| **Pubblica skill** | Tab offerta / richiesta / esplora |
| **Classifica** | Podio + tabella ranking |

## 9.3 Sessione utente

`sessionStorage` chiave `skillswap_student` — JSON dello studente loggato. Nessuna password (prototipo scolastico).

---

# 10. Suite di test

| File | Cosa verifica |
|------|----------------|
| `MatchingServiceTest` | One-way, swap, score, offerte inactive |
| `ExchangeServiceTest` | propose, accept, complete, cancel, transizioni invalide |
| `ReviewServiceTest` | addReview, duplicate, exchange non completed |
| `FileStorageTest` | Round-trip save/load |
| `ValidatorTest` | Stelle, email, transizioni, offer active |

**Ubicazione:** cartella `test/` alla root (non `src/test/java`). Il `pom.xml` potrebbe richiedere configurazione `maven-surefire-plugin` per eseguire `mvn test` correttamente.

**Pattern:** commenti Given-When-Then, JUnit 5 assertions.

---

# 11. Documentazione e cartelle ausiliarie

| File | Contenuto |
|------|-----------|
| `DOCUMENTAZIONE_COMPLETA.md` | Reference tecnico unificato (architettura, CSV, limiti) |
| `GUIDELINE.md` | Milestone M1–M6, piano test originale |
| `Web_integration.md` | Roadmap Flask W1–W8 (approccio alternativo) |
| `web/README_WEB.md` | Contratto API REST |
| `PROPLAN.md` / `MAJOR_FIXES.md` / `IMPROVEMENTS_AND_FIXES_1.md` | Storia modifiche |

**Cartella `web/` (Python/Flask):** prototipo iniziale **non** usato dall’app attuale; l’integrazione ufficiale è Spring Boot in `src/main/java/it/skillswap/web`.

---

# 12. Domande di autoverifica

### Livello base
1. Quali sono le sei entità principali e come si collegano?
2. Quali sono i quattro stati di un `Exchange`?
3. Che significa `active=false` su un’`Offer`?
4. Dove vengono salvati i dati e con quale separatore?

### Livello intermedio
5. Spiega il punteggio 0–6 del matching one-way.
6. Perché `propose` rifiuta offer e request dello stesso studente?
7. Quando si può aggiungere una `Review`?
8. Differenza tra `Validator` soft e strict?

### Livello avanzato
9. Perché non conviene avviare CLI e Web insieme?
10. Come `ApplicationState` riusa i service senza duplicare la logica?
11. Quali timestamp CSV non vengono reidratati correttamente al load?
12. Progetta un nuovo criterio di matching (+punti se stessa email domain) — quali classi toccheresti?

---

# 13. Esercizi di studio consigliati

| # | Esercizio | Obiettivo |
|---|-----------|-----------|
| 1 | Traccia su carta un flusso completo: login Alessandro → match → propose → accept → complete → review | End-to-end |
| 2 | Aggiungi studente C via API POST, crea offer+request, verifica CSV | REST + persistenza |
| 3 | Prova `cancel` su exchange `ACCEPTED` — quale eccezione? | State machine |
| 4 | Scrivi un test JUnit per score=6 con stessa classe | Matching |
| 5 | Leggi `FileStorage.load()` riga per riga e disegna le mappe temporanee | Persistenza |
| 6 | Confronta `AppController.handleProponiExchange` e `ExchangeController.propose` | CLI vs Web |
| 7 | Modifica frontend: mostra email nel profilo sidebar | Frontend |
| 8 | Documenta un bug: due exchange sulla stessa coppia offer/request | Analisi dati reali |

---

# 14. Percorso di studio in 7 giorni

| Giorno | Argomento | Attività |
|--------|-----------|----------|
| 1 | Visione + struttura repo | Leggi §1–2, esplora cartelle in IDE |
| 2 | Dominio | Studia §4, disegna ER, compila glossario |
| 3 | Service | §5, esegui test Matching e Exchange |
| 4 | Persistenza + CSV | §6, apri `data/`, fai save/load manualmente |
| 5 | CLI | §7 + Appendice B, `mvn exec:java` |
| 6 | Web | §8–9, `spring-boot:run`, prova tutte le pagine |
| 7 | Ripasso | §12 domande, esercizi 1–3, simula interrogazione |

---

# 15. Glossario esteso

| Termine | Definizione |
|---------|-------------|
| **Offer** | Pubblicazione: «so insegnare X a livello Y» |
| **Request** | Pubblicazione: «voglio imparare X, livello minimo Y» |
| **Exchange** | Accordo formale offer↔request con ciclo di vita |
| **Match one-way** | Solo le mie request abbinano offerte altrui |
| **Match swap** | Scambio reciproco di competenze |
| **Score** | Punteggio qualità abbinamento (0–6 o 0–12) |
| **Reviewee** | Studente che riceve la recensione |
| **Reviewer** | Studente che scrive la recensione |
| **SkillSwapState** | Snapshot in-memory dell’intera piattaforma |
| **DTO** | Data Transfer Object — record JSON per API |
| **Aggregate** | Cluster di oggetti modificati insieme (`SkillSwapState`) |

---

# Appendice A — API REST completa

Base URL: `http://localhost:8080` · Content-Type: `application/json`

### Autenticazione
| Metodo | Path | Body | Risposta |
|--------|------|------|----------|
| POST | `/api/auth/login` | `{ "email": "alessandro@scuola.it", "password": "..." }` | `StudentDto` |

### Studenti
| Metodo | Path | Note |
|--------|------|------|
| GET | `/api/students` | Lista |
| GET | `/api/students/{id}` | Dettaglio |
| GET | `/api/students/{id}/reviews` | Recensioni ricevute |
| POST | `/api/students` | `{ name, className, email }` |

### Skill
| GET | `/api/skills` | Lista |
| POST | `/api/skills` | `{ name, category }` |

### Offer / Request
| GET | `/api/offers?studentId=&active=` | Filtri opzionali |
| POST | `/api/offers` | `{ studentId, skillId, level, note }` |
| GET | `/api/requests?studentId=` | |
| POST | `/api/requests` | `{ studentId, skillId, minLevel, note }` |

### Matching
| GET | `/api/students/{id}/matches/one-way` | `MatchResultDto[]` |
| GET | `/api/students/{id}/matches/swap` | `MatchResultDto[]` |

### Exchange
| GET | `/api/exchanges?studentId=` | |
| GET | `/api/exchanges/{id}` | |
| POST | `/api/exchanges` | `{ offerId, requestId [, exchangeId] }` |
| PUT | `/api/exchanges/{id}/accept` | |
| PUT | `/api/exchanges/{id}/complete` | |
| PUT | `/api/exchanges/{id}/cancel` | |

### Review / Ranking
| POST | `/api/reviews` | `{ exchangeId, reviewerStudentId, stars, comment }` |
| GET | `/api/ranking` | Studenti con rating, ordinati |

**Errori:** `400` / `409` con `{ "error": "messaggio" }` via `ApiExceptionHandler`.

---

# Appendice B — Menu CLI completo

| # | Azione |
|---|--------|
| 1 | Crea studente |
| 2 | Aggiungi skill |
| 3 | Aggiungi offer |
| 4 | Aggiungi request |
| 5 | Lista studenti |
| 6 | Lista offer |
| 7 | Lista request |
| 8 | Proponi exchange |
| 9 | Accetta exchange |
| 10 | Completa exchange |
| 11 | Cancella exchange |
| 12 | Lista exchange |
| 13 | Aggiungi recensione |
| 14 | Recensioni di uno studente |
| 15 | Profilo studente |
| 16 | Dettaglio exchange |
| 17 | Leaderboard |
| 18 | Match one-way |
| 19 | Match swap |
| 0 | Esci (salva CSV) |

---

# Appendice C — Inventario classi Java

### `it.skillswap.domain` (13 classi + 6 exception)
`Student`, `Skill`, `SkillCategory`, `SkillLevel`, `Offer`, `Request`, `Exchange`, `ExchangeStatus`, `Review`, `SkillSwapState`, + package `exception`

### `it.skillswap.service` (7 classi)
`MatchingService`, `MatchResult`, `ExchangeService`, `ReviewService`, `Validator`, `ValidationResult`, `ConsoleReportPrinter`

### `it.skillswap.storage` (3 classi)
`Storage`, `FileStorage`, `InMemoryStorage`

### `it.skillswap.app` (2 classi)
`Main`, `AppController`

### `it.skillswap.web` (2 + 9 controller + 7 DTO + 3 utility)
`SkillSwapWebApplication`, `ApplicationState`  
Controllers: `Auth`, `Student`, `Skill`, `Offer`, `Request`, `Matching`, `Exchange`, `Review`, `Ranking`  
DTO: `StudentDto`, `SkillDto`, `OfferDto`, `RequestDto`, `ExchangeDto`, `ReviewDto`, `MatchResultDto`  
`IdGenerator`, `ApiExceptionHandler`, `ErrorResponse`

**Totale classi Java principali:** ~45 file sorgente in `src/main/java`.

---

## Riferimento incrociato documenti

```
GUIDA_STUDIO_COMPLETA.md  ← studio, esami, percorso didattico (questo file)
DOCUMENTAZIONE_COMPLETA.md ← dettaglio tecnico implementativo
GUIDELINE.md              ← storia milestone
web/README_WEB.md         ← contratto API
README.md                 ← quick start
```

---

*Fine guida di studio — SkillSwap GoodAt. Per aggiornamenti al codice, allineare §2, §8 e appendici alle modifiche del repository.*
