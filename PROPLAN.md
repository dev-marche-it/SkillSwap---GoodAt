# SkillSwap School — Piano di Professionalizzazione (Fase 2)

> Documento di riferimento per la seconda fase di sviluppo.  
> Le 6 milestone sono completate. Questa fase trasforma il prototipo in un progetto professionale,  
> mantenendo la stessa architettura ma alzando la qualità del codice a standard aziendali.

---

## Indice

1. [Obiettivo della Fase 2](#1-obiettivo-della-fase-2)
2. [Fase 2.1 — Pulizia e isolamento del Main](#2-fase-21--pulizia-e-isolamento-del-main)
3. [Fase 2.2 — Introduzione degli Enum di dominio](#3-fase-22--introduzione-degli-enum-di-dominio)
4. [Fase 2.3 — Validazione centralizzata](#4-fase-23--validazione-centralizzata)
5. [Fase 2.4 — Eccezioni di dominio custom](#5-fase-24--eccezioni-di-dominio-custom)
6. [Fase 2.5 — Copertura dei test](#6-fase-25--copertura-dei-test)
7. [Fase 2.6 — Clean Code e refactoring](#7-fase-26--clean-code-e-refactoring)
8. [Fase 2.7 — Documentazione tecnica (Javadoc)](#8-fase-27--documentazione-tecnica-javadoc)
9. [Fase 2.8 — Preparazione all'integrazione Web](#9-fase-28--preparazione-allintegrazione-web)
10. [Ordine di esecuzione e branch strategy](#10-ordine-di-esecuzione-e-branch-strategy)

---

## 1. Obiettivo della Fase 2

Il codice prodotto nelle milestone è funzionante ma strutturato come prototipo.  
L'obiettivo di questa fase è renderlo **manutenibile, testabile ed estendibile**,  
come ci si aspetta in un contesto professionale o aziendale.

### Criteri di qualità da raggiungere

| Criterio | Stato attuale | Obiettivo |
|---|---|---|
| Main senza logica di business | ❌ | ✅ |
| Enum per tutti i valori costanti | ⚠️ parziale | ✅ |
| Validazione centralizzata | ❌ | ✅ |
| Eccezioni custom di dominio | ❌ | ✅ |
| Copertura test ≥ 80% della logica | ⚠️ parziale | ✅ |
| Nessun metodo > 20 righe | ❌ | ✅ |
| Javadoc sui metodi pubblici | ❌ | ✅ |
| Service stateless e testabili | ⚠️ parziale | ✅ |

---

## 2. Fase 2.1 — Pulizia e isolamento del Main

### Obiettivo
Ridurre `Main.java` a puro **entry point**: nessuna logica di business, nessuna manipolazione di dati, nessun ciclo di matching o gestione degli scambi scritto direttamente nel metodo `main`.

### Prompt tecnico
> Refactoring di `Main.java`: estrarre tutta la logica applicativa in una classe dedicata `AppController` (o `AppRunner`) nel package `it.skillswap.app`. Il metodo `main` deve istanziare il controller, caricare lo stato tramite `Storage.load()` e delegare interamente il flusso CLI al controller. Il controller espone metodi come `run()`, `handleCreaStudente()`, `handleAggiungiOffer()`, `handleListaMatch()` ecc., ciascuno responsabile di una sola operazione. Nessun `Scanner`, `System.out` o logica condizionale deve rimanere nel `main`.

### File coinvolti
```
app/Main.java          ← svuotato a entry point puro
app/AppController.java ← nuovo, contiene il loop CLI e i metodi handler
```

### Risultato atteso
```java
// Main.java dopo il refactoring
public class Main {
    public static void main(String[] args) {
        Storage storage = new FileStorage("data/");
        SkillSwapState state = storage.load();
        new AppController(state, storage).run();
    }
}
```

---

## 3. Fase 2.2 — Introduzione degli Enum di dominio

### Obiettivo
Sostituire tutte le `String` che rappresentano valori costanti con **enum tipizzati**, eliminando il rischio di errori per typo e rendendo il codice autodocumentante.

### Prompt tecnico
> Identificare tutti i campi di tipo `String` nelle classi di dominio che rappresentano un insieme finito di valori e sostituirli con enum dedicati nel package `it.skillswap.domain`. In particolare: il campo `level` in `Offer` e `minLevel` in `Request` diventano `SkillLevel`; il campo `category` in `Skill` diventa `SkillCategory`; lo stato in `Exchange` è già `ExchangeStatus` ma va verificato che sia usato in modo coerente in tutto il codebase. Aggiornare costruttori, getter, metodi di parsing CSV e tutti i punti di utilizzo. Aggiungere un metodo statico `fromString(String)` a ciascun enum per il parsing dai file CSV.

### Enum da creare

```java
// domain/SkillLevel.java
public enum SkillLevel {
    BEGINNER, INTERMEDIATE, ADVANCED;
    public static SkillLevel fromString(String s) { ... }
    public boolean isSufficientFor(SkillLevel minLevel) { ... }
}

// domain/SkillCategory.java
public enum SkillCategory {
    SUBJECT, LANGUAGE, SPORT, ART, OTHER;
    public static SkillCategory fromString(String s) { ... }
}
```

### File coinvolti
```
domain/SkillLevel.java     ← nuovo
domain/SkillCategory.java  ← nuovo
domain/Offer.java          ← aggiornato
domain/Request.java        ← aggiornato
domain/Skill.java          ← aggiornato
service/MatchingService.java ← aggiornato (isLevelSufficient usa enum)
storage/FileStorage.java   ← aggiornato (parsing CSV)
```

---

## 4. Fase 2.3 — Validazione centralizzata

### Obiettivo
Raccogliere tutta la logica di validazione in classi dedicate, evitando che i controlli siano dispersi tra service, costruttori e CLI.

### Prompt tecnico
> Creare una classe `Validator` nel package `it.skillswap.service` (o in un nuovo package `it.skillswap.validation`) con metodi statici di validazione richiamabili da qualsiasi punto del sistema. I metodi devono restituire un oggetto `ValidationResult` che incapsula esito (booleano) e messaggio di errore, oppure lanciare eccezioni custom (vedi Fase 2.4). Le validazioni da centralizzare sono: stelle recensione (1–5), transizioni di stato `Exchange`, unicità recensione per scambio, offerta attiva prima di `propose()`, e formato email studente.

### Classe da creare

```java
// service/Validator.java
public class Validator {
    public static void validateStars(int stars) { ... }
    public static void validateStateTransition(ExchangeStatus from, ExchangeStatus to) { ... }
    public static void validateOfferActive(Offer offer) { ... }
    public static void validateEmail(String email) { ... }
}
```

---

## 5. Fase 2.4 — Eccezioni di dominio custom

### Obiettivo
Sostituire i controlli con `if + System.out.println` o return `null` con **eccezioni tipizzate**, rendendo gli errori espliciti, tracciabili e gestibili in modo uniforme.

### Prompt tecnico
> Creare una gerarchia di eccezioni nel package `it.skillswap.domain.exception`. La classe base è `SkillSwapException` (unchecked, estende `RuntimeException`). Le eccezioni specifiche coprono i casi di errore principali del dominio. Ogni service deve catturare queste eccezioni nel controller e mostrarle all'utente con un messaggio leggibile, senza propagare stack trace grezzi in console.

### Gerarchia da creare

```
domain/exception/
├── SkillSwapException.java          ← base
├── StudentNotFoundException.java
├── InvalidStateTransitionException.java
├── OfferNotActiveException.java
├── DuplicateReviewException.java
└── InvalidStarsException.java
```

---

## 6. Fase 2.5 — Copertura dei test

### Obiettivo
Portare la copertura dei test a coprire tutti i casi critici del dominio, con una struttura chiara **Given / When / Then** e uso di `InMemoryStorage` per isolare la logica dai file.

### Prompt tecnico
> Espandere la suite di test esistente fino a raggiungere un minimo di 20 test distribuiti su tutte le aree funzionali. Ogni test deve seguire la struttura Given/When/Then, usare esclusivamente `InMemoryStorage` o dati costruiti in memoria, avere un solo `assert` principale e un nome descrittivo in formato `should<Behavior>When<Condition>`. Aggiungere test per i casi limite: studente non trovato, transizione di stato non valida, recensione duplicata, offerta non attiva, livello insufficiente nel matching.

### Distribuzione target

| Classe di test | N° test | Casi da coprire |
|---|---|---|
| `MatchingServiceTest` | 6 | one-way, swap, no self-match, inattiva, score, nessun match |
| `ExchangeServiceTest` | 5 | propose, accept, complete, cancel, transizione invalida |
| `ReviewServiceTest` | 4 | add review, duplicata, exchange non completed, stelle invalide |
| `ValidatorTest` | 3 | stelle fuori range, transizione invalida, email malformata |
| `FileStorageTest` | 3 | load, save, atomic rename |

```
test/
├── MatchingServiceTest.java
├── ExchangeServiceTest.java
├── ReviewServiceTest.java
├── ValidatorTest.java
└── FileStorageTest.java
```

---

## 7. Fase 2.6 — Clean Code e refactoring

### Obiettivo
Applicare i principi **SOLID** e **Clean Code** a tutto il codebase: metodi brevi, nomi descrittivi, nessuna duplicazione, responsabilità singola per classe.

### Prompt tecnico
> Eseguire un refactoring sistematico del codebase seguendo le seguenti regole: (1) nessun metodo deve superare le 20 righe — estrarre metodi privati dove necessario; (2) nessuna classe deve avere più di una responsabilità — verificare SRP su tutti i service; (3) eliminare tutti i commenti che spiegano cosa fa il codice (il codice deve essere autoesplicativo) — mantenere solo i Javadoc; (4) eliminare codice morto, variabili non usate, import non necessari; (5) i nomi di variabili e metodi devono essere in inglese, descrittivi e senza abbreviazioni.

### Checklist per ogni classe

```
□ Nessun metodo > 20 righe
□ Nessuna variabile con nome generico (es. x, temp, obj)
□ Nessun import non utilizzato
□ Nessun blocco di codice commentato
□ Responsabilità singola verificata
□ Nessun System.out.println nei service (solo nel controller/printer)
```

---

## 8. Fase 2.7 — Documentazione tecnica (Javadoc)

### Obiettivo
Aggiungere **Javadoc** a tutti i metodi pubblici di domain, service e storage, rendendo il codice comprensibile senza bisogno di leggere l'implementazione.

### Prompt tecnico
> Aggiungere Javadoc completo a tutti i metodi pubblici e alle interfacce del progetto. Ogni Javadoc deve includere: descrizione funzionale di una riga, tag `@param` per ogni parametro con descrizione, tag `@return` con descrizione del valore restituito, tag `@throws` per ogni eccezione che il metodo può lanciare. Le classi di dominio devono avere un Javadoc a livello di classe che descrive il ruolo dell'entità nel sistema.

### Esempio standard da seguire

```java
/**
 * Finds all one-way matches for a given student.
 * A one-way match occurs when another student offers a skill
 * that the given student is looking for.
 *
 * @param studentId the ID of the student looking for matches
 * @return a list of {@link MatchResult}, sorted by score descending
 * @throws StudentNotFoundException if no student with the given ID exists
 */
public List<MatchResult> findOneWayMatches(String studentId) { ... }
```

---

## 9. Fase 2.8 — Preparazione all'integrazione Web

### Obiettivo
Verificare che l'architettura sia pronta per esporre i service tramite un layer web (Flask o Spring Boot), senza modifiche alla logica di business.

### Prompt tecnico
> Verificare che tutti i service (`MatchingService`, `ExchangeService`, `ReviewService`) siano **stateless** rispetto alla richiesta: non devono mantenere stato interno tra una chiamata e l'altra, lo stato del sistema deve essere sempre passato come parametro o iniettato via costruttore. Aggiungere una cartella `web/` nella root del progetto con un file `README_WEB.md` che descrive gli endpoint REST che i service esporrebbero se integrati con Spring Boot o Flask, con metodo HTTP, path, parametri e risposta JSON attesa. Questo documento sarà la base per lo sviluppo dell'interfaccia web nella Fase 3.

### Struttura da creare

```
web/
└── README_WEB.md    ← mappa degli endpoint futuri

Esempio contenuto:
GET  /api/students              → lista tutti gli studenti
GET  /api/matches/{studentId}   → one-way matches per studente
POST /api/exchange/propose      → body: { offerId, requestId }
POST /api/review                → body: { exchangeId, reviewerId, stars, comment }
```

---

## 10. Ordine di esecuzione e branch strategy

### Sequenza consigliata

```
2.1 → 2.2 → 2.3 → 2.4 → 2.5 → 2.6 → 2.7 → 2.8
```

Le fasi 2.3 e 2.4 (validazione ed eccezioni) devono precedere l'espansione dei test (2.5),  
perché i test verificheranno proprio che le eccezioni vengano lanciate correttamente.

### Branch strategy

```bash
# Una branch per fase
git checkout -b refactor/2.1-main-cleanup
git checkout -b refactor/2.2-enums
git checkout -b refactor/2.3-validation
git checkout -b refactor/2.4-exceptions
git checkout -b refactor/2.5-tests
git checkout -b refactor/2.6-clean-code
git checkout -b refactor/2.7-javadoc
git checkout -b refactor/2.8-web-prep
```

### Commit message convention

```
refactor: extract business logic from Main into AppController
refactor: replace String levels with SkillLevel enum
feat: add centralized Validator class
feat: add custom domain exceptions hierarchy
test: expand test suite to 20 tests with Given/When/Then
refactor: apply Clean Code rules across all service classes
docs: add Javadoc to all public methods
docs: add REST endpoint map for future web integration
```

---

*Documento generato per SkillSwap School — Fase 2 di sviluppo*