# SkillSwap School — Guida alle Milestone di Sviluppo

> Documento di riferimento per lo sviluppo del progetto.  
> Segui le milestone nell'ordine indicato: ogni fase costruisce sulle precedenti.



## Indice

1. [Panoramica del progetto](#1-panoramica-del-progetto)
2. [Struttura del repository](#2-struttura-del-repository)
3. [Modello del dominio](#3-modello-del-dominio)
4. [Struttura dei file CSV](#4-struttura-dei-file-csv)
5. [Milestone 1 — Dominio e struttura base](#5-milestone-1--dominio-e-struttura-base)
6. [Milestone 2 — MatchingService](#6-milestone-2--matchingservice)
7. [Milestone 3 — ExchangeService](#7-milestone-3--exchangeservice)
8. [Milestone 4 — ReviewService](#8-milestone-4--reviewservice)
9. [Milestone 5 — Output leggibile con StringBuilder](#9-milestone-5--output-leggibile-con-stringbuilder)
10. [Milestone 6 — Persistenza su file](#10-milestone-6--persistenza-su-file)
11. [Piano dei test](#11-piano-dei-test)
12. [Regole del sistema](#12-regole-del-sistema)
13. [Evoluzione futura — Web App](#13-evoluzione-futura--web-app)



## 1. Panoramica del progetto

**SkillSwap School** è un prototipo di piattaforma per lo scambio di competenze tra studenti.

Gli studenti possono:
- offrire competenze che possiedono
- richiedere aiuto su competenze che vogliono imparare
- trovare un match con un altro studente
- aprire uno scambio (Exchange)
- chiudere lo scambio con una recensione e un rating

Il progetto è sviluppato come **applicazione console Java** con **persistenza su file CSV**.

### Package consigliati

| Package | Contenuto |
|||
| `it.skillswap.domain` | Classi del dominio (`Student`, `Skill`, `Offer`, `Request`, `Exchange`, `Review`) |
| `it.skillswap.service` | Logica applicativa (`MatchingService`, `ExchangeService`, `ReviewService`) |
| `it.skillswap.storage` | Persistenza (`Storage`, `InMemoryStorage`, `FileStorage`) |
| `it.skillswap.app` | Programma principale, CLI |



## 2. Struttura del repository

```
skillswap-school/
├── src/
│   └── main/
│       └── java/
│           └── it/skillswap/
│               ├── domain/
│               ├── service/
│               ├── storage/
│               └── app/
├── src/
│   └── test/
│       └── java/
│           └── it/skillswap/
├── data/
│   ├── students.csv
│   ├── skills.csv
│   ├── offers.csv
│   ├── requests.csv
│   ├── exchanges.csv
│   └── reviews.csv
├── GUIDE.md
└── README.md
```

### Branch strategy

```
main                  ← codice stabile e funzionante
feature/matching      ← sviluppo MatchingService
feature/persistence   ← sviluppo FileStorage
feature/exchange      ← sviluppo ExchangeService
feature/review        ← sviluppo ReviewService
```

> **Regola:** commit piccoli e descrittivi, un branch per feature.



## 3. Modello del dominio

### Relazioni tra le entità

```
Student ──────────┐
                  ▼
Skill ──────► Offer ──────► Exchange ──────► Review
                  ▲              ▲
Skill ──────► Request ───────────┘
                  ▲
Student ──────────┘
```

### Descrizione delle entità

| Entità | Contiene |
|||
| `Student` | id, nome, classe, email, rating medio, conteggio rating |
| `Skill` | id, nome, categoria |
| `Offer` | uno `Student`, una `Skill`, livello, nota, flag `active` |
| `Request` | uno `Student`, una `Skill`, livello minimo, nota |
| `Exchange` | una `Offer`, una `Request`, stato, data apertura, data chiusura |
| `Review` | un `Exchange`, `Student` recensore, `Student` recensito, stelle, commento |

### Stati di un Exchange

```
PROPOSED ──► ACCEPTED ──► COMPLETED
    │
    └──────────────────────────────► CANCELLED
```



## 4. Struttura dei file CSV

Tutti i file si trovano nella cartella `data/`.  
Il separatore è il punto e virgola `;`.

### `students.csv`
```
student_id;name;class;email;rating_avg;rating_count
S1;Anna Rossi;4A;anna@scuola.it;4.6;5
S2;Luca Bianchi;4A;luca@scuola.it;4.2;3
```

### `skills.csv`
```
skill_id;name;category
K1;Programmazione C;SUBJECT
K2;Matematica;SUBJECT
```

### `offers.csv`
```
offer_id;student_id;skill_id;level;note;active
O1;S2;K1;ADVANCED;puntatori e array;true
O2;S1;K2;ADVANCED;algebra e funzioni;true
```

### `requests.csv`
```
request_id;student_id;skill_id;min_level;note
R1;S1;K1;BEGINNER;mi blocco sulle stringhe
R2;S2;K2;BEGINNER;ho debiti sulle equazioni
```

### `exchanges.csv`
```
exchange_id;offer_id;request_id;status;created_at;closed_at
E1;O1;R2;ACCEPTED;2026-03-04T10:30;
```
> `closed_at` è vuoto finché lo scambio non è completato o annullato.

### `reviews.csv`
```
review_id;exchange_id;reviewer_student_id;reviewee_student_id;stars;comment;created_at
V1;E1;S1;S2;5;spiega benissimo;2026-03-04T12:10
```



## 5. Milestone 1 — Dominio e struttura base

**Obiettivo:** implementare le classi del dominio, la CLI minima e lo stato in memoria.

### Classi da implementare

#### `Student`
```java
// Campi principali
String studentId
String name
String className
String email
double ratingAvg
int ratingCount
```

#### `Skill`
```java
String skillId
String name
String category  // es. "SUBJECT"
```

#### `Offer`
```java
String offerId
Student student
Skill skill
String level     // es. "BEGINNER", "INTERMEDIATE", "ADVANCED"
String note
boolean active
```

#### `Request`
```java
String requestId
Student student
Skill skill
String minLevel
String note
```

#### `Exchange`
```java
String exchangeId
Offer offer
Request request
ExchangeStatus status  // enum: PROPOSED, ACCEPTED, COMPLETED, CANCELLED
LocalDateTime createdAt
LocalDateTime closedAt
```

#### `Review`
```java
String reviewId
Exchange exchange
Student reviewer
Student reviewee
int stars         // validato: 1–5
String comment
LocalDateTime createdAt
```

#### `SkillSwapState`
Classe contenitore di tutti i dati in memoria:
```java
Map<String, Student>  students
Map<String, Skill>    skills
Map<String, Offer>    offers
Map<String, Request>  requests
Map<String, Exchange> exchanges
Map<String, Review>   reviews
```

#### `InMemoryStorage`
Implementa l'interfaccia `Storage`, mantiene i dati in memoria (utile per test):
```java
public interface Storage {
    SkillSwapState load();
    void save(SkillSwapState state);
}
```

### CLI minima — comandi da supportare

| Comando | Descrizione |
|||
| `crea studente` | Registra un nuovo studente |
| `aggiungi offer` | Aggiunge un'offerta di competenza |
| `aggiungi request` | Aggiunge una richiesta di competenza |
| `lista offer` | Mostra tutte le offerte |
| `lista request` | Mostra tutte le richieste |

### Validazioni obbligatorie

- Stelle recensione: intero compreso tra 1 e 5
- Un'offerta con `active = false` non può essere usata per aprire uno scambio
- Un'offerta o richiesta deve essere associata a uno studente esistente

### Criteri di completamento

- [ ] Tutte le classi domain compilano senza errori
- [ ] `InMemoryStorage` carica e salva correttamente
- [ ] CLI risponde ai 5 comandi base
- [ ] Validazione stelle implementata



## 6. Milestone 2 — MatchingService

**Obiettivo:** implementare la logica di matching tra studenti.

### Metodi principali

```java
List<MatchResult> findOneWayMatches(String studentId)
List<MatchResult> findSwapMatches(String studentId)
```

### Tipi di matching

**One-way match:** trova chi offre la skill che io sto cercando.
```
Anna cerca K1 (Programmazione C)
→ trova Luca che offre K1
```

**Swap match (reciproco):** io offro X e cerco Y, un altro offre Y e cerca X.
```
Anna offre K2 e cerca K1
Luca offre K1 e cerca K2
→ match reciproco tra Anna e Luca
```

### Sistema di punteggio (score)

| Condizione | Punti |
|||
| Skill identica | +3 |
| Livello sufficiente | +2 |
| Stessa classe | +1 |

### Classe `MatchResult`

```java
String offerId
String requestId
int score
String reason     // spiegazione testuale del punteggio
```

### Regole

- Uno studente **non può fare match con sé stesso**
- Solo offerte con `active = true` partecipano al matching

### Criteri di completamento

- [ ] `findOneWayMatches` restituisce i match ordinati per score decrescente
- [ ] `findSwapMatches` identifica correttamente le coppie reciproche
- [ ] Lo studente stesso è escluso dai risultati
- [ ] Offerte non attive sono ignorate



## 7. Milestone 3 — ExchangeService

**Obiettivo:** gestire il ciclo di vita degli scambi.

### Metodi principali

```java
Exchange propose(String offerId, String requestId)
Exchange accept(String exchangeId)
Exchange complete(String exchangeId)
Exchange cancel(String exchangeId)
```

### Transizioni di stato valide

| Da | A | Metodo |
||||
| — | `PROPOSED` | `propose()` |
| `PROPOSED` | `ACCEPTED` | `accept()` |
| `ACCEPTED` | `COMPLETED` | `complete()` |
| `PROPOSED` | `CANCELLED` | `cancel()` |

> Qualsiasi altra transizione deve lanciare un'eccezione o restituire un errore.

### Regole

- `propose()` fallisce se l'offerta ha `active = false`
- Alla chiusura (COMPLETED o CANCELLED), impostare `closedAt` con la data/ora corrente
- Non è possibile riaprire uno scambio già chiuso

### Criteri di completamento

- [ ] Tutte le transizioni valide funzionano correttamente
- [ ] Le transizioni non valide sono bloccate con messaggio di errore
- [ ] `propose()` controlla lo stato `active` dell'offerta
- [ ] `closedAt` viene impostato correttamente



## 8. Milestone 4 — ReviewService

**Obiettivo:** gestire le recensioni al termine di uno scambio.

### Metodo principale

```java
Review addReview(String exchangeId, String reviewerId, int stars, String comment)
```

### Regole

- La recensione è possibile **solo se lo scambio è `COMPLETED`**
- Ogni studente può lasciare **al massimo una recensione per scambio**
- Le stelle devono essere un intero tra **1 e 5**
- Dopo ogni nuova recensione, aggiornare `ratingAvg` e `ratingCount` dello studente recensito

### Aggiornamento del rating

```java
// Formula per aggiornare il rating medio
newRatingAvg = ((ratingAvg * ratingCount) + newStars) / (ratingCount + 1)
ratingCount++
```

### Criteri di completamento

- [ ] Recensione bloccata se lo scambio non è COMPLETED
- [ ] Doppia recensione dello stesso studente per lo stesso scambio bloccata
- [ ] Stelle fuori range (< 1 o > 5) bloccate
- [ ] Rating medio dello studente aggiornato correttamente dopo ogni recensione



## 9. Milestone 5 — Output leggibile con StringBuilder

**Obiettivo:** produrre output formattato e leggibile sulla console.

### Classe `ConsoleReportPrinter`

```java
String printStudentProfile(Student student)
String printMatches(List<MatchResult> matches)
String printExchangeDetails(Exchange exchange)
String printLeaderboard(List<Student> students)
```

### Linee guida per la formattazione

Usare `StringBuilder` per:
- Intestazioni con separatori (`===`, ``)
- Indentazione coerente
- Tabelle testuali allineate


### Criteri di completamento

- [ ] Tutti e 4 i metodi sono implementati
- [ ] Nessun `System.out.println` diretto nel corpo dei metodi (usare `StringBuilder`)
- [ ] L'output è visivamente coerente e leggibile
- [ ] Il leaderboard è ordinato per `ratingAvg` decrescente



## 10. Milestone 6 — Persistenza su file

**Obiettivo:** implementare `FileStorage` che legge e scrive i CSV nella cartella `data/`.

### Strategia di caricamento (ordine obbligatorio)

```
1. Carica students  → Map<String, Student>
2. Carica skills    → Map<String, Skill>
3. Carica offers    → recupera Student e Skill dalla mappa → crea Offer
4. Carica requests  → recupera Student e Skill dalla mappa → crea Request
5. Carica exchanges → recupera Offer e Request → crea Exchange
6. Carica reviews   → recupera Exchange, Student reviewer, Student reviewee → crea Review
```

> L'ordine è fondamentale: ogni entità dipende da quelle caricate prima.

### Strategia di salvataggio sicuro (atomic save)

Per evitare file corrotti in caso di crash durante la scrittura:

```
1. Scrivi su file temporaneo:  offers.csv.tmp
2. Solo se la scrittura va a buon fine, rinomina:  offers.csv.tmp → offers.csv
```

Questo garantisce che il file originale non venga mai sovrascritto parzialmente.

### Implementazione consigliata

```java
public class FileStorage implements Storage {

    private final Path dataDir;

    @Override
    public SkillSwapState load() {
        // 1. Leggi students.csv
        // 2. Leggi skills.csv
        // 3. Leggi offers.csv  (join con students e skills)
        // 4. Leggi requests.csv (join con students e skills)
        // 5. Leggi exchanges.csv (join con offers e requests)
        // 6. Leggi reviews.csv (join con exchanges e students)
        // 7. Restituisci SkillSwapState popolato
    }

    @Override
    public void save(SkillSwapState state) {
        // Per ogni entità:
        // 1. Scrivi su file .tmp
        // 2. Rinomina in .csv
    }
}
```

### Possibili evoluzioni della persistenza

| Implementazione | Descrizione |
|||
| `InMemoryStorage` | Dati in memoria, ideale per test |
| `FileStorage` | Lettura/scrittura CSV su disco (milestone 6) |
| `DbStorage` *(futuro)* | Salvataggio su database SQL, senza modifiche al resto dell'app |

### Criteri di completamento

- [ ] `FileStorage.load()` carica correttamente tutti i CSV nell'ordine corretto
- [ ] Tutte le relazioni tra entità sono ricostruite (join by id)
- [ ] `FileStorage.save()` usa file temporanei prima di rinominare
- [ ] Il programma si avvia correttamente con dati pre-esistenti nella cartella `data/`
- [ ] Un secondo avvio del programma mostra i dati del primo avvio



## 11. Piano dei test

**Obiettivo minimo:** 12–15 test totali.


### Struttura consigliata: Given / When / Then

```java
@Test
void shouldFindOneWayMatch() {
    // Given
    Student anna = new Student("S1", "Anna", "4A", "anna@scuola.it");
    Student luca = new Student("S2", "Luca", "4A", "luca@scuola.it");
    Skill progC = new Skill("K1", "Programmazione C", "SUBJECT");
    Offer offertaLuca = new Offer("O1", luca, progC, "ADVANCED", "", true);
    Request richiestaAnna = new Request("R1", anna, progC, "BEGINNER", "");

    // When
    List<MatchResult> matches = matchingService.findOneWayMatches("S1");

    // Then
    assertEquals(1, matches.size());
    assertEquals("O1", matches.get(0).getOfferId());
}
```

### Linee guida

- Un solo `assert` principale per test
- Dati piccoli e chiari, non caricare file CSV nei test unitari
- Usare `InMemoryStorage` per isolare la logica dai file

### Casi test consigliati

**Matching (5 test)**
1. One-way match trovato correttamente
2. Nessun match se lo studente ha già tutto
3. Lo studente non fa match con sé stesso
4. Offerta non attiva esclusa dal matching
5. Swap match reciproco trovato correttamente

**Exchange workflow (4 test)**
1. `propose()` crea uno scambio in stato PROPOSED
2. `accept()` porta da PROPOSED ad ACCEPTED
3. `complete()` porta da ACCEPTED a COMPLETED e imposta `closedAt`
4. Transizione non valida lancia eccezione

**Review e rating (3 test)**
1. Recensione aggiunge la review e aggiorna il rating
2. Doppia recensione dello stesso studente bloccata
3. Recensione su scambio non COMPLETED bloccata

**Persistenza (2–3 test)**
1. Salvataggio e ricaricamento producono lo stesso stato
2. File temporaneo rinominato correttamente
3. *(Opzionale)* Gestione file mancante → stato vuoto restituito



## 12. Regole del sistema

Queste regole devono essere rispettate in tutta l'applicazione:

| Regola | Dove applicarla |
|||
| Uno studente non può fare match con sé stesso | `MatchingService` |
| Un'offerta non attiva non può aprire uno scambio | `ExchangeService.propose()` |
| Le transizioni di stato devono essere valide | `ExchangeService` |
| Una recensione è possibile solo se lo scambio è COMPLETED | `ReviewService` |
| Uno studente può lasciare al massimo una recensione per scambio | `ReviewService` |
| Le stelle devono essere tra 1 e 5 | `ReviewService` / `Review` |



## 13. Evoluzione futura — Web App

> **Nota:** questa sezione riguarda una fase successiva al completamento di tutte le milestone Java.

Al termine dello sviluppo del prototipo console, è prevista l'integrazione di una **interfaccia web** per rendere il sistema accessibile via browser.

### Funzionalità previste

- **Visualizzazione** di studenti, match, scambi e recensioni tramite pagine HTML/CSS
- **Sistema di login** per autenticare gli studenti
- **Classifica (ranking)** interattiva degli studenti per rating
- **Accessibilità sulla rete locale**: la web app sarà servita localmente, accessibile da tutti i dispositivi sulla stessa rete

### Approccio di integrazione

I metodi e i service sviluppati nelle milestone Java (MatchingService, ExchangeService, ReviewService) verranno esposti tramite un layer web, senz
a modificare la logica di business già implementata.  
La separazione tra dominio, servizi e presentazione (già garantita dall'architettura a package) faciliterà questa evoluzione.

> Lo sviluppo della web app inizierà **solo dopo il completamento e il collaudo di tutte le milestone**.



*Documento generato per il progetto SkillSwap School — aggiornato a marzo 2026*