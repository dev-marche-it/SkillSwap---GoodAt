# SkillSwap GoodAt — Documentazione tecnica completa

> **Versione documento:** 1.0 — maggio 2026  
> **Progetto:** `skillswap-goodat` (`it.skillswap`)  
> **Scopo:** descrivere per filo e per segno architettura, dominio, servizi, persistenza, CLI, test, integrazione web e percorsi evolutivi del repository.

---

## Indice

1. [Visione e obiettivi](#1-visione-e-obiettivi)
2. [Stack tecnologico e build](#2-stack-tecnologico-e-build)
3. [Struttura del repository](#3-struttura-del-repository)
4. [Architettura a strati](#4-architettura-a-strati)
5. [Modello di dominio](#5-modello-di-dominio)
6. [Enumerazioni e eccezioni](#6-enumerazioni-e-eccezioni)
7. [Aggregato applicativo `SkillSwapState`](#7-aggregato-applicativo-skillswapstate)
8. [Servizi applicativi](#8-servizi-applicativi)
9. [Validazione (`Validator`)](#9-validazione-validator)
10. [Persistenza e formato CSV](#10-persistenza-e-formato-csv)
11. [Layer applicativo CLI](#11-layer-applicativo-cli)
12. [Flussi operativi end-to-end](#12-flussi-operativi-end-to-end)
13. [Suite di test](#13-suite-di-test)
14. [Documentazione correlata nella cartella `docs/`](#14-documentazione-correlata-nella-cartella-docs)
15. [Integrazione web: stato attuale e opzioni](#15-integrazione-web-stato-attuale-e-opzioni)
16. [Limitazioni note e debito tecnico](#16-limitazioni-note-e-debito-tecnico)
17. [Glossario](#17-glossario)

---

## 1. Visione e obiettivi

**SkillSwap School** (artifact Maven `skillswap-goodat`) è una piattaforma educativa per lo **scambio di competenze tra studenti** della stessa scuola. Uno studente può:

- **offrire** una competenza che sa insegnare (`Offer`);
- **richiedere** aiuto su una competenza che vuole imparare (`Request`);
- trovare **match** automatici (one-way o swap reciproco) tramite un algoritmo a punteggio;
- aprire uno **scambio** (`Exchange`) con ciclo di vita controllato;
- chiudere lo scambio e lasciare una **recensione** (`Review`) che aggiorna il **rating** del valutato;
- consultare **profilo**, **dettaglio scambio** e **classifica** (leaderboard).

Il backend di business è **interamente in Java 17**, organizzato in package Maven standard. L’interfaccia utente attuale è una **CLI interattiva** (`AppController`). I dati persistono su **file CSV** nella cartella `data/` tramite `FileStorage`.

Non esiste (ancora) un server HTTP Java in esecuzione: l’evoluzione verso il web è pianificata (cartella `web/` con Flask in fase W1, oppure Spring Boot come layer REST sopra i service esistenti).

---

## 2. Stack tecnologico e build

| Componente | Dettaglio |
|------------|-----------|
| Linguaggio | Java 17 (`maven.compiler.release`) |
| Build | Apache Maven 3.x |
| GroupId / ArtifactId | `it.skillswap` / `skillswap-goodat` |
| Versione | `0.0.1-SNAPSHOT` |
| Dipendenze runtime | Nessuna (solo JDK) |
| Dipendenze compile-time | Lombok `1.18.42` (scope `provided`) — **attualmente le classi di dominio non usano annotazioni Lombok**; la dipendenza è pronta per ridurre boilerplate futuro |
| Test | JUnit Jupiter `5.10.2` |

### Comandi Maven essenziali

```bash
# Compilazione
mvn clean compile

# Esecuzione applicazione console
mvn exec:java
# equivalente esplicito:
mvn exec:java -Dexec.mainClass=it.skillswap.app.Main

# Test (richiede plugin surefire se i test non sono in src/test/java standard)
mvn test
```

> **Nota sui test:** i file di test si trovano in `test/` alla root del progetto, non in `src/test/java`. Il `pom.xml` attuale **non** configura `maven-surefire-plugin` né `testSourceDirectory`; per eseguire i test con Maven potrebbe essere necessario spostare i test o aggiungere la configurazione Surefire.

### Entry point

`it.skillswap.app.Main`:

1. Istanzia `FileStorage`.
2. Chiama `storage.load()` → ottiene `SkillSwapState`.
3. Avvia `new AppController(state, storage).run()`.
4. All’uscita dal menu (opzione `0`), `AppController` invoca `storage.save(state)`.

---

## 3. Struttura del repository

```
SkillSwap---GoodAt/
├── pom.xml                          # Maven: Java 17, Lombok, JUnit, exec-maven-plugin
├── README.md                        # Introduzione rapida
│
├── src/main/java/it/skillswap/
│   ├── app/
│   │   ├── Main.java                # Entry point
│   │   └── AppController.java       # Menu CLI (19 operazioni + uscita)
│   ├── domain/
│   │   ├── Student.java
│   │   ├── Skill.java
│   │   ├── SkillCategory.java
│   │   ├── SkillLevel.java
│   │   ├── Offer.java
│   │   ├── Request.java
│   │   ├── Exchange.java
│   │   ├── ExchangeStatus.java
│   │   ├── Review.java
│   │   ├── SkillSwapState.java
│   │   └── exception/               # Gerarchia SkillSwapException
│   ├── service/
│   │   ├── MatchingService.java
│   │   ├── MatchResult.java
│   │   ├── ExchangeService.java
│   │   ├── ReviewService.java
│   │   ├── Validator.java
│   │   ├── ValidationResult.java
│   │   └── ConsoleReportPrinter.java
│   └── storage/
│       ├── Storage.java
│       ├── FileStorage.java
│       └── InMemoryStorage.java
│
├── test/                            # Test JUnit (fuori da src/test/java)
│   ├── MatchingServiceTest.java
│   ├── ExchangeServiceTest.java
│   ├── ReviewServiceTest.java
│   ├── FileStorageTest.java
│   └── ValidatorTest.java
│
├── data/                            # CSV (creati al primo save; possono essere assenti all’avvio)
│   ├── students.csv
│   ├── skills.csv
│   ├── offers.csv
│   ├── requests.csv
│   ├── exchanges.csv
│   └── reviews.csv
│
├── docs/                            # Documentazione di progetto
│   ├── DOCUMENTAZIONE_COMPLETA.md   # ← questo file
│   ├── GUIDELINE.md                 # Milestone di sviluppo originarie
│   ├── Web_integration.md           # Roadmap Flask (fasi W1–W8)
│   ├── PROPLAN.md
│   ├── MAJOR_FIXES.md
│   └── IMPROVEMENTS_AND_FIXES_1.md
│
└── web/                             # Prototipo web (Flask) — fase W1 avviata
    ├── app.py
    ├── config.py
    ├── requirements.txt
    └── README_WEB.md                # Mappa API REST per futuro layer Java
```

---

## 4. Architettura a strati

Il progetto segue una **separazione netta** tra dominio, servizi, persistenza e presentazione:

```
┌─────────────────────────────────────────────────────────────┐
│  PRESENTAZIONE (oggi)                                       │
│  AppController + ConsoleReportPrinter + System.out            │
│  (futuro: REST Controller Spring / Thymeleaf / SPA)           │
└───────────────────────────┬─────────────────────────────────┘
                            │ usa
┌───────────────────────────▼─────────────────────────────────┐
│  SERVIZI APPLICATIVI                                        │
│  MatchingService | ExchangeService | ReviewService          │
│  Validator (utility)                                        │
└───────────────────────────┬─────────────────────────────────┘
                            │ legge/scrive
┌───────────────────────────▼─────────────────────────────────┐
│  DOMINIO + STATO                                            │
│  SkillSwapState + entità (Student, Offer, …)                │
└───────────────────────────┬─────────────────────────────────┘
                            │ persistito da
┌───────────────────────────▼─────────────────────────────────┐
│  PERSISTENZA                                                │
│  Storage ← FileStorage (CSV) | InMemoryStorage (test)         │
└─────────────────────────────────────────────────────────────┘
```

### Principi progettuali

- **Un solo aggregato in memoria:** `SkillSwapState` contiene liste mutabili di tutte le entità; i service ricevono il riferimento e modificano le stesse liste.
- **Service stateless rispetto al DB:** non c’è database relazionale; lo “stato globale” è l’oggetto `SkillSwapState` caricato una volta all’avvio.
- **Regole di business nei service:** `AppController` per exchange e review delega a `ExchangeService` e `ReviewService`; per studenti/skill/offer/request modifica direttamente le liste (CRUD “semplice” senza service dedicati).
- **Eccezioni di dominio:** errori prevedibili (transizione stato, offerta non attiva, recensione duplicata) propagano `SkillSwapException` e sottoclassi.

---

## 5. Modello di dominio

### Diagramma relazioni

```
                    ┌──────────┐
                    │ Student  │
                    └────┬─────┘
         ┌───────────────┼───────────────┐
         │               │               │
         ▼               ▼               ▼
    ┌────────┐     ┌─────────┐    ┌──────────┐
    │ Offer  │     │ Request │    │ (rating) │
    └───┬────┘     └────┬────┘    └──────────┘
        │    Skill      │
        │   ┌───────────┘
        ▼   ▼
    ┌──────────┐
    │ Exchange │──────► Review
    └──────────┘
         ▲
    Skill (catalogo competenze)
```

### `Student`

| Campo | Tipo | Mutabilità | Descrizione |
|-------|------|------------|-------------|
| `studentId` | `String` | immutabile | Identificativo (es. `S1`, generato in CLI come `S` + (size+1)) |
| `name` | `String` | immutabile | Nome visualizzato |
| `className` | `String` | immutabile | Classe/cohort (es. `4A`) — usata nel matching (+1 se uguale) |
| `email` | `String` | immutabile | Email (login web futuro) |
| `ratingAvg` | `double` | mutabile | Media voti 1–5, aggiornata da `addRating` |
| `ratingCount` | `int` | mutabile | Numero recensioni nella media |

**`addRating(int stars)`:** ricalcola la media incrementale:

\[
\text{ratingAvg}_{\text{nuovo}} = \frac{\text{ratingAvg} \times \text{ratingCount} + \text{stars}}{\text{ratingCount} + 1}
\]

Poi incrementa `ratingCount`.

---

### `Skill`

| Campo | Tipo | Descrizione |
|-------|------|-------------|
| `skillId` | `String` | Es. `K1` |
| `name` | `String` | Nome competenza |
| `category` | `SkillCategory` | Macro-categoria |

---

### `Offer`

Rappresenta “**io so insegnare questa skill a questo livello**”.

| Campo | Tipo | Descrizione |
|-------|------|-------------|
| `offerId` | `String` | Es. `O1` |
| `student` | `Student` | Autore dell’offerta |
| `skill` | `Skill` | Competenza offerta |
| `level` | `SkillLevel` | Livello dichiarato |
| `note` | `String` | Testo libero |
| `active` | `boolean` | Default `true`; messo a `false` quando uno scambio viene **completato** su quell’offerta |

---

### `Request`

Rappresenta “**cerco aiuto su questa skill, livello minimo X**”.

| Campo | Tipo | Descrizione |
|-------|------|-------------|
| `requestId` | `String` | Es. `R1` |
| `student` | `Student` | Richiedente |
| `skill` | `Skill` | Competenza cercata |
| `minLevel` | `SkillLevel` | Livello minimo accettabile dall’offerta del pari |
| `note` | `String` | Testo libero |

---

### `Exchange`

Collega un’`Offer` a una `Request` in un processo negoziato.

| Campo | Tipo | Descrizione |
|-------|------|-------------|
| `exchangeId` | `String` | Es. `E1` |
| `offer` | `Offer` | Lato “chi insegna” |
| `request` | `Request` | Lato “chi impara” |
| `status` | `ExchangeStatus` | Ciclo di vita |
| `createdAt` | `LocalDateTime` | Impostato al costruttore (`now()`) |
| `closedAt` | `LocalDateTime` | Impostato in `setStatus` se `COMPLETED` o `CANCELLED` |

**Macchina a stati:**

```
PROPOSED ──accept──► ACCEPTED ──complete──► COMPLETED
    │
    └──cancel──► CANCELLED
```

| Transizione | Metodo service | Da | A |
|-------------|----------------|-----|---|
| Proposta | `ExchangeService.propose` | — | `PROPOSED` |
| Accettazione | `accept` | `PROPOSED` | `ACCEPTED` |
| Completamento | `complete` | `ACCEPTED` | `COMPLETED` (+ `offer.active = false`) |
| Annullamento | `cancel` | `PROPOSED` | `CANCELLED` |

Transizioni non elencate → `InvalidStateTransitionException`.

---

### `Review`

Feedback post-scambio **solo se** `Exchange.status == COMPLETED`.

| Campo | Tipo | Descrizione |
|-------|------|-------------|
| `reviewId` | `String` | Es. `V1` |
| `exchange` | `Exchange` | Scambio recensito |
| `reviewer` | `Student` | Chi scrive |
| `reviewee` | `Student` | Chi riceve (l’altro partecipante) |
| `stars` | `int` | 1–5 (validato nel costruttore e in `ReviewService`) |
| `comment` | `String` | Testo |
| `createdAt` | `LocalDateTime` | `now()` al create |

**Regole `ReviewService.addReview`:**

1. Exchange deve esistere e essere `COMPLETED`.
2. `reviewerId` deve essere lo studente dell’**offer** o della **request** (non terzi).
3. `reviewee` = l’altro partecipante.
4. **Una sola recensione per (exchange, reviewer)** → altrimenti `DuplicateReviewException`.
5. Dopo insert, `reviewee.addRating(stars)`.

---

## 6. Enumerazioni e eccezioni

### `SkillCategory`

Valori: `SUBJECT`, `LANGUAGE`, `SPORT`, `ART`, `OTHER`.

`SkillCategory.fromString(s)` — case-insensitive; `null` → `SUBJECT`; altrimenti `IllegalArgumentException`.

### `SkillLevel`

Valori ordinati: `BEGINNER` (0) < `INTERMEDIATE` (1) < `ADVANCED` (2).

- `fromString(s)` — come sopra, default `BEGINNER`.
- `isSufficientFor(SkillLevel minLevel)` — `this.ordinal() >= minLevel.ordinal()`.

### `ExchangeStatus`

`PROPOSED`, `ACCEPTED`, `COMPLETED`, `CANCELLED`.

### Gerarchia eccezioni (`it.skillswap.domain.exception`)

| Classe | Estende | Quando |
|--------|---------|--------|
| `SkillSwapException` | `RuntimeException` | Base |
| `OfferNotActiveException` | ↑ | Proposta scambio su offerta `active=false` |
| `InvalidStateTransitionException` | ↑ | Transizione exchange illegale |
| `InvalidStarsException` | ↑ | Stelle ∉ [1,5] in modalità strict |
| `DuplicateReviewException` | ↑ | Seconda recensione stesso reviewer/exchange |
| `StudentNotFoundException` | ↑ | Definita ma **non usata** dai service attuali (cercano con `null`) |

`AppController` cattura `SkillSwapException` e stampa `Errore di dominio: …`; altre eccezioni → `Errore: …`.

---

## 7. Aggregato applicativo `SkillSwapState`

Contenitore **unico** dello stato runtime:

```java
List<Student> students;
List<Skill> skills;
List<Offer> offers;
List<Request> requests;
List<Exchange> exchanges;
List<Review> reviews;
```

- Costruttore: inizializza liste vuote (`ArrayList`).
- Getter restituiscono le **liste mutabili** (nessuna copia difensiva).
- Non usa `Map` per id: lookup per id avviene con stream/filter nei service e in `AppController`.

**Implicazione:** coerenza referenziale (stesso oggetto `Student` in Offer e Request) è garantita perché le entità sono oggetti Java condivisi, non id scollegati.

---

## 8. Servizi applicativi

### `MatchingService`

Dipende da `SkillSwapState`. Non modifica lo stato (solo lettura).

#### `findOneWayMatches(String studentId)`

Trova offerte di **altri** studenti compatibili con le **request** dello studente indicato.

**Filtri per ogni coppia (request del seeker, offer nel catalogo):**

1. Request appartiene a `studentId`.
2. Offer `active == true`.
3. Offer non dello stesso studente.
4. `offer.skill.skillId == request.skill.skillId`.

**Punteggio** (`calculateScore`):

| Criterio | Punti | Note |
|----------|-------|------|
| Skill identica | +3 | Già garantito dal filtro, confermato nel calcolo |
| Livello offer ≥ minLevel request | +2 | `offer.level.isSufficientFor(request.minLevel)` |
| Stessa classe offer/request | +1 | `className` uguale |

**Massimo teorico one-way:** 6.

Risultato: lista di `MatchResult(offerId, requestId, score, reason)` ordinata per `score` decrescente.

Se `studentId` sconosciuto → lista vuota (nessuna eccezione).

#### `findSwapMatches(String studentId)`

Cerca **scambi reciproci**: esiste un altro studente tale che:

- la sua **offer** soddisfa una **mia request** (skill + filtri come one-way);
- una **mia offer** soddisfa una **sua request** sulla skill che io offro.

Algoritmo (semplificato):

```
per ogni miaOffer attiva:
  per ogni miaRequest:
    per ogni theirOffer attiva (altro studente):
      se theirOffer.skill == miaRequest.skill:
        per ogni theirRequest dello stesso studente:
          se theirRequest.skill == miaOffer.skill:
            score = calculate(theirOffer, miaRequest) + calculate(miaOffer, theirRequest)
            reason = "SWAP: " + buildReason(theirOffer, miaRequest)
```

Ordinamento per score decrescente. Score massimo teorico swap: 12 (6+6).

#### `MatchResult`

DTO immutabile: `offerId`, `requestId`, `score`, `reason` (stringa leggibile, es. `"skill identica (+3), livello sufficiente (+2), stessa classe (+1)"`).

---

### `ExchangeService`

| Metodo | Precondizioni | Effetto |
|--------|---------------|---------|
| `propose(exchangeId, offerId, requestId)` | Offer/request esistono; offer attiva; studenti distinti | Crea `Exchange` PROPOSED, aggiunge a `state.exchanges` |
| `accept(exchangeId)` | Stato PROPOSED | → ACCEPTED |
| `complete(exchangeId)` | Stato ACCEPTED | → COMPLETED, `offer.setActive(false)` |
| `cancel(exchangeId)` | Stato PROPOSED | → CANCELLED |

Errori: `IllegalArgumentException` (entità mancante), `OfferNotActiveException`, `IllegalStateException` (auto-match), `InvalidStateTransitionException`.

---

### `ReviewService`

| Metodo | Descrizione |
|--------|-------------|
| `addReview(reviewId, exchangeId, reviewerId, stars, comment)` | Crea `Review`, aggiorna rating reviewee |
| `getReviewsForStudent(studentId)` | Stream filtrato su `reviewee.studentId` → lista immutabile |

---

### `ConsoleReportPrinter`

Presentation helper per CLI (non è un service di dominio):

| Metodo | Output |
|--------|--------|
| `printStudentProfile(student, reviews)` | Box ASCII con anagrafica, media, elenco recensioni |
| `printMatches(matches)` | Elenco offer/request/score/reason |
| `printExchangeDetails(exchange)` | Stato, offer, request, timestamp |
| `printLeaderboard(students)` | Solo studenti con `ratingCount > 0`, ordinati per `ratingAvg` desc |

---

## 9. Validazione (`Validator`)

Utility **statica** con due stili:

- **Soft:** `ValidationResult` (`success()` / `failure(msg)`).
- **Strict:** eccezioni (`InvalidStarsException`, `InvalidStateTransitionException`).

Metodi:

| Metodo | Controllo |
|--------|-----------|
| `validateStars` / `validateStarsStrict` | 1 ≤ stars ≤ 5 |
| `validateStateTransition` | Non uscire da COMPLETED/CANCELLED |
| `validateOfferActive` | `offer.isActive()` |
| `validateEmail` | Regex semplice `^[A-Za-z0-9+_.-]+@(.+)$` |
| `validateUniqueReview` | Nessuna review già presente per quell’exchange (qualsiasi reviewer) |

> **Nota:** `ReviewService` implementa regole più strette (un reviewer per exchange) rispetto a `validateUniqueReview` (una review totale per exchange). `Validator` è usato nei test; la CLI non invoca `Validator` per ogni input.

---

## 10. Persistenza e formato CSV

### Interfaccia `Storage`

```java
SkillSwapState load();
void save(SkillSwapState state);
```

### `FileStorage`

- Directory: `data/` (relativa alla working directory del processo JVM).
- Separatore campi: **`;`** (punto e virgola).
- Scrittura: file temporaneo `*.tmp` poi `Files.move` atomico con replace.
- Lettura: righe malformate **saltate** (`continue`); file assente → lista vuota.
- Errori I/O: messaggio su `System.out`, **nessuna eccezione** propagata.

#### Ordine di caricamento (dipendenze referenziali)

1. `students.csv` → mappa `studentMap`
2. `skills.csv` → mappa `skillMap`
3. `offers.csv` (richiede student + skill)
4. `requests.csv`
5. `exchanges.csv` (richiede offer + request)
6. `reviews.csv` (richiede exchange + studenti)

#### Schema file (colonne in ordine)

**`students.csv`** — 6 campi per riga:

```
student_id;name;class;email;rating_avg;rating_count
```

Esempio: `S1;Anna Rossi;4A;anna@scuola.it;4.6;5`

**`skills.csv`** — 3 campi:

```
skill_id;name;category
```

`category` = nome enum `SkillCategory`.

**`offers.csv`** — 6 campi:

```
offer_id;student_id;skill_id;level;note;active
```

`level` = `SkillLevel`; `active` = `true`/`false` (parsed con `Boolean.parseBoolean`).

**`requests.csv`** — 5 campi:

```
request_id;student_id;skill_id;min_level;note
```

**`exchanges.csv`** — 6 campi (`split(";", -1)` per campi vuoti):

```
exchange_id;offer_id;request_id;status;created_at;closed_at
```

`status` = `ExchangeStatus.name()`. Alla load, `createdAt`/`closedAt` del CSV **non sono riletti** nel modello (il costruttore imposta `createdAt=now()`; solo `setStatus` aggiorna `closedAt` se COMPLETED/CANCELLED). **Limitazione:** persistenza timestamp su exchange in load è parziale.

**`reviews.csv`** — 7 campi:

```
review_id;exchange_id;reviewer_student_id;reviewee_student_id;stars;comment;created_at
```

Alla load, `createdAt` della review è sempre ricreato nel costruttore (`now()`), non dal CSV.

#### Salvataggio rating studenti

`saveStudents` scrive `rating_avg` formattato `%.1f` e `rating_count` — allineato al modello dopo recensioni.

### `InMemoryStorage`

Tiene un riferimento a un unico `SkillSwapState`; `save` sostituisce il riferimento (non deep copy). Usato nei test unitari senza filesystem.

---

## 11. Layer applicativo CLI

### `AppController` — menu completo

| # | Azione | Implementazione |
|---|--------|-----------------|
| 1 | Crea studente | Aggiunge a `state.students`, id `S{n}` |
| 2 | Aggiungi skill | `SkillCategory.fromString`, id `K{n}` |
| 3 | Aggiungi offer | Collega student/skill per id, `SkillLevel`, id `O{n}` |
| 4 | Aggiungi request | Id `R{n}` |
| 5 | Lista studenti | `forEach println` |
| 6 | Lista offer | |
| 7 | Lista request | |
| 8 | Proponi exchange | `ExchangeService.propose` |
| 9 | Accetta exchange | `accept` |
| 10 | Completa exchange | `complete` |
| 11 | Cancella exchange | `cancel` |
| 12 | Lista exchange | |
| 13 | Aggiungi recensione | `ReviewService.addReview` — **attenzione:** `Integer.parseInt` su stelle senza try dedicato |
| 14 | Recensioni studente | `getReviewsForStudent` |
| 15 | Profilo studente | `ConsoleReportPrinter.printStudentProfile` |
| 16 | Dettaglio exchange | `printExchangeDetails` |
| 17 | Leaderboard | `printLeaderboard` |
| 18 | Match one-way | `MatchingService.findOneWayMatches` |
| 19 | Match swap | `findSwapMatches` |
| 0 | Esci | `storage.save(state)` |

**Generazione ID:** sequenziale basata su `list.size() + 1` (non verifica collisioni se si eliminano elementi).

**Lookup:** `findStudentById`, `findSkillById` — stream su liste, ritorno `null` se assente.

---

## 12. Flussi operativi end-to-end

### Flusso A — Primo utilizzo (dataset vuoto)

```
Avvio Main → FileStorage.load() → SkillSwapState vuoto
→ Menu → crea studenti, skill, offer, request
→ Match (18/19) → propose exchange → accept → complete
→ addReview → rating aggiornato
→ Leaderboard / Profilo
→ Esci → save CSV in data/
```

### Flusso B — Riapertura con dati esistenti

```
load() ricostruisce grafo oggetti con riferimenti condivisi
→ operazioni CLI
→ save() sovrascrive tutti e 6 i CSV
```

### Flusso C — Match one-way (logica)

```
Studente S1 ha Request R1 su skill K1 (min BEGINNER)
Studente S2 ha Offer O1 su K1 livello INTERMEDIATE, active
→ findOneWayMatches("S1") include (O1, R1)
→ score tipico: 3+2+1=6 se stessa classe, altrimenti 5
```

### Flusso D — Vincolo recensione

```
Exchange E1 in COMPLETED
S1 (offer side) recensisce → reviewee = studente della request
S2 può recensire a sua volta (reviewer diverso) — ammesso
S1 seconda recensione stesso E1 → DuplicateReviewException
```

---

## 13. Suite di test

| File | Copertura principale |
|------|----------------------|
| `MatchingServiceTest` | One-way, swap, score, offerte inactive, stesso studente |
| `ExchangeServiceTest` | propose, accept, complete, cancel, transizioni invalide |
| `ReviewServiceTest` | addReview, duplicate, stars, exchange non completed |
| `FileStorageTest` | Round-trip save/load su filesystem temporaneo |
| `ValidatorTest` | Stars, email, state transition, offer active |

Pattern **Given-When-Then** nei commenti. Framework: JUnit 5 assertions statiche.

---

## 14. Documentazione correlata nella cartella `docs/`

| File | Contenuto |
|------|-----------|
| `GUIDELINE.md` | Milestone M1–M6, regole sistema, piano test originale |
| `Web_integration.md` | Roadmap Flask W1–W8, template HTML, matching Python |
| `PROPLAN.md` | Pianificazione evolutiva |
| `MAJOR_FIXES.md` / `IMPROVEMENTS_AND_FIXES_1.md` | Storico correzioni |
| `web/README_WEB.md` | Contratto API REST per layer HTTP futuro |

Questo documento **integra e unifica** le fonti sopra in un unico riferimento tecnico aggiornato allo stato del codice Java.

---

## 15. Integrazione web: stato attuale e opzioni

### Stato attuale (maggio 2026)

| Componente | Stato |
|------------|--------|
| Backend Java (dominio + service + CSV) | **Completo** per uso CLI |
| Cartella `web/` | **W1:** Flask risponde `"SkillSwap School — server attivo"` su `/` |
| `data/` nel repo | Può essere assente; viene creata al primo `save` |
| Spring Boot / REST Java | **Non implementato** — solo documentato |

### È possibile una interfaccia web con backend Java?

**Sì.** Tre approcci, in ordine di allineamento con “tutto il lavoro Java”:

#### Opzione 1 — Spring Boot REST (consigliata per backend Java puro)

Aggiungere al `pom.xml`:

- `spring-boot-starter-web`
- (opzionale) `spring-boot-starter-thymeleaf` per HTML server-side

Creare controller REST che:

1. All’avvio: `SkillSwapState state = fileStorage.load()`.
2. Delegano a `MatchingService`, `ExchangeService`, `ReviewService` esistenti **senza modifiche**.
3. Dopo ogni mutazione: `fileStorage.save(state)`.
4. Espongono JSON come in `web/README_WEB.md` (es. `GET /api/students/{id}/matches/one-way`).

Frontend rudimentale: HTML/CSS/JS statico in `src/main/resources/static` o cartella `web/static` servita da Spring.

**Vantaggi:** una sola logica di business (Java); niente duplicazione matching in Python.  
**Sforzo stimato prototipo:** 1–2 giorni per CRUD + match + leaderboard.

#### Opzione 2 — Flask + CSV condivisi (già avviata)

Java (CLI) e Flask **non devono scrivere contemporaneamente** gli stessi CSV. Flusso tipico:

1. Uso console Java → genera/aggiorna `data/*.csv`.
2. Avvio `python web/app.py` → Flask legge CSV (fasi W2–W7 in `Web_integration.md`).

**Vantaggi:** prototipo UI veloce senza toccare Java.  
**Svantaggi:** logica matching duplicata in Python; rischio disallineamento.

#### Opzione 3 — Process bridge (sconsigliata)

Flask invoca `java -jar` per ogni operazione. Fragile, lento, difficile da gestire su Windows/Linux.

### Roadmap rudimentale consigliata (Java backend)

```
Fase 1: spring-boot-starter-web + CORS + SkillSwapState @Configuration
Fase 2: StudentController, RankingController (GET)
Fase 3: MatchingController (one-way / swap)
Fase 4: ExchangeController + ReviewController (POST/PUT)
Fase 5: index.html + fetch API + pagine login (email da students.csv)
```

Dettaglio endpoint: vedere `web/README_WEB.md` e sezione W8 di `Web_integration.md`.

---

## 16. Limitazioni note e debito tecnico

| Area | Dettaglio |
|------|-----------|
| ID generation | Solo incrementale su `size+1`; no UUID; no delete in CLI |
| Concorrenza | Nessun lock su CSV; due processi che salvano corrompono dati |
| Load exchange/review dates | Timestamp da CSV non sempre reidratati |
| `StudentNotFoundException` | Non integrata nei service |
| `Validator` vs CLI | Validazione email/stars non applicata uniformemente in `AppController` |
| Test Maven | Cartella `test/` non standard; Surefire non configurato |
| Lombok | Dipendenza presente, codice ancora manuale |
| Sicurezza web | Login per email senza password (solo prototipo scolastico) |
| Header CSV | `FileStorage` non scrive righe di intestazione; compatibile con load che ignora righe corte |

---

## 17. Glossario

| Termine | Significato |
|---------|-------------|
| **Offer** | Offerta di insegnamento su una skill |
| **Request** | Richiesta di apprendimento su una skill |
| **Exchange** | Accordo tra un’offer e una request |
| **One-way match** | Solo “altri” soddisfano le mie request |
| **Swap match** | Scambio reciproco di competenze |
| **Score** | Punteggio 0–6 (one-way) qualità abbinamento |
| **Leaderboard** | Classifica per `ratingAvg` tra chi ha almeno 1 recensione |
| **SkillSwapState** | Snapshot in-memory dell’intera piattaforma |

---

*Fine documentazione — SkillSwap GoodAt (`it.skillswap`) — per aggiornamenti al codice, allineare questo file alle modifiche di dominio, service e CSV.*
