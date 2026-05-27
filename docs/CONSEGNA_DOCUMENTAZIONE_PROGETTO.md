# SkillSwap GoodAt - Documentazione di consegna (resoconto completo)

Versione: maggio 2026  
Progetto: `it.skillswap:skillswap-goodat`  
Stack: Java 17, Maven, Spring Boot 3.3.5, frontend statico (HTML/CSS/JS), persistenza CSV

---

## 1) Presentazione del progetto

SkillSwap GoodAt e una piattaforma educativa per lo scambio di competenze tra studenti.
Il sistema permette di trasformare il classico "passaparola" scolastico in un processo strutturato, tracciabile e misurabile.

Il progetto copre tutto il ciclo operativo:
- creazione profili studenti;
- pubblicazione offerte e richieste;
- ricerca match;
- avvio e gestione scambi;
- recensione finale e aggiornamento reputazione;
- visualizzazione classifica.

### Obiettivo didattico e tecnico
L'obiettivo non e solo avere una UI funzionante, ma mostrare un progetto software completo:
- dominio ben modellato;
- regole di business isolate;
- persistenza semplice ma consistente;
- doppia interfaccia (CLI e Web) che riusa lo stesso backend.

---

## 2) Ambito funzionale implementato

### 2.1 Gestione entita principali
Sono gestite tutte le entita necessarie al flusso:
- `Student`
- `Skill`
- `Offer`
- `Request`
- `Exchange`
- `Review`

Lo stato applicativo e contenuto in `SkillSwapState`, che funge da snapshot in-memory.

### 2.2 Matching
Il matching supporta due modalita:
- **one-way**: altri studenti che soddisfano le mie richieste;
- **swap**: scambio reciproco di competenze.

Il punteggio e esplicito:
- skill identica: +3
- livello sufficiente: +2
- stessa classe: +1

Questa scelta rende il sistema spiegabile e verificabile.

### 2.3 Workflow scambi
Lo scambio segue una macchina a stati:
- `PROPOSED -> ACCEPTED -> COMPLETED`
- `PROPOSED -> CANCELLED`

Transizioni non ammesse vengono bloccate.
Lo stato governa quali azioni sono disponibili in UI e API.

### 2.4 Recensioni e ranking
La recensione e consentita solo su scambi completati.
Ogni recensione contiene:
- voto (1-5),
- commento,
- reviewer e reviewee.

Dopo la review, il backend aggiorna media e numero voti dello studente recensito.
La classifica mostra i risultati aggregati.

---

## 3) Architettura e componenti principali

### 3.1 Architettura a strati
La soluzione e organizzata in quattro strati:
1. Presentazione (CLI + Web)
2. Service layer (business rules)
3. Dominio e stato
4. Persistenza

Il vantaggio e la separazione delle responsabilita: la UI non contiene logica critica, ma invoca servizi dedicati.

### 3.2 Package e classi principali

#### `it.skillswap.domain`
- Entita: `Student`, `Skill`, `Offer`, `Request`, `Exchange`, `Review`
- Stato aggregato: `SkillSwapState`
- Enum: `SkillCategory`, `SkillLevel`, `ExchangeStatus`
- Eccezioni: `SkillSwapException` e derivate

#### `it.skillswap.service`
- `MatchingService`: algoritmo di abbinamento
- `ExchangeService`: ciclo di vita scambi
- `ReviewService`: vincoli review + update rating
- `AuthService`, `PasswordHasher`, `Validator`, `EntityIdGenerator`, report utility

#### `it.skillswap.storage`
- `Storage`: contratto astratto
- `FileStorage`: persistenza CSV
- `InMemoryStorage`: storage in memoria (test/supporto)

#### `it.skillswap.app`
- `Main`: entrypoint CLI
- `AppController`: menu operativo
- `ConsoleBanner`, `ConsoleReportPrinter`

#### `it.skillswap.web` e `it.skillswap.web.api`
- `SkillSwapWebApplication`
- `ApplicationState` (stato condiviso + servizi)
- Controller REST (`AuthController`, `StudentController`, `OfferController`, `RequestController`, `MatchingController`, `ExchangeController`, `ReviewController`, `RankingController`, `CommunityController`)
- DTO in `web/api/dto`
- Error handling: `ApiExceptionHandler`, `ErrorResponse`

---

## 4) Componenti usati nel progetto

### 4.1 Lato backend
- Java 17
- Maven
- Spring Boot Web
- strutture dati Java standard (`List`, stream API, enums, eccezioni custom)
- persistenza CSV tramite `java.nio.file`

### 4.2 Lato frontend
- HTML semantico single-page
- CSS custom (design system + responsive + dark mode)
- JavaScript vanilla modulare:
  - `api.js` per chiamate REST
  - `app.js` per stato UI e rendering
  - `icons.js` per SVG custom

### 4.3 Persistenza
- File CSV sotto `data/`
- Storage astratto con due implementazioni (`FileStorage`, `InMemoryStorage`)

---

## 5) Resoconto sviluppo (alto livello, fase per fase)

### Fase 1 - Nucleo dominio e console
- Definizione entita, enum e stato aggregato.
- Prima UI testuale con flussi base.
- Inserimento meccanismi di validazione minima.

### Fase 2 - Business services
- Implementazione `MatchingService`.
- Implementazione `ExchangeService` con stati e vincoli.
- Implementazione `ReviewService` con update reputazione.

### Fase 3 - Persistenza e stabilita dati
- Introduzione `FileStorage` con caricamento e salvataggio CSV.
- Gestione linking tra entita in fase di load.
- Supporto `InMemoryStorage` per scenari di test.

### Fase 4 - Estensione web
- Introduzione layer REST Spring Boot.
- DTO per serializzazione pulita.
- Frontend statico con pagine funzionali principali.

### Fase 5 - Hardening e UX
- Correzioni multi-utente su bacheca e scambi.
- Fix su recensioni, stelle, alias payload.
- Anti-duplicati scambi attivi.
- Miglioramento layout login, brand e componenti.

---

## 6) Istruzioni di esecuzione

Prerequisiti:
- JDK 17
- Maven disponibile nel PATH

Comandi principali:

```bash
mvn clean compile

# Modalita console
mvn exec:java -Dexec.mainClass=it.skillswap.app.Main

# Modalita web
mvn spring-boot:run
# http://localhost:8080
```

Nota operativa:
- Non avviare simultaneamente CLI e Web sullo stesso dataset CSV.

---

## 7) Esempio sessione console (scenario completo)

```text
1) Creo due studenti
2) Inserisco skill Matematica e Java
3) Uno pubblica Offer su Matematica
4) L'altro pubblica Request su Matematica
5) Eseguo match one-way e verifico score
6) Propongo exchange (PROPOSED)
7) Accetto exchange (ACCEPTED)
8) Completo exchange (COMPLETED)
9) Lascio review con 5 stelle
10) Verifico ranking aggiornato
11) Esco e salvo su CSV
```

Questo scenario dimostra il flusso end-to-end completo.

---

## 8) Stato avanzamento rispetto alle milestone

Riferimento base: `docs/GUIDELINE.md`.

| Milestone | Stato | Output raggiunto |
|---|---|---|
| M1 - Dominio e struttura base | Completata | Modello completo + CLI iniziale |
| M2 - MatchingService | Completata | One-way e swap con scoring |
| M3 - ExchangeService | Completata | Workflow a stati validato |
| M4 - ReviewService | Completata | Vincoli review + rating |
| M5 - Output console | Completata | Report leggibili e menu completo |
| M6 - Persistenza file | Completata | `FileStorage` CSV + ricostruzione relazioni |
| Estensione web | Completata | API REST + frontend statico |
| Stabilizzazione finale | Completata iterativa | Fix funzionali + UX polish |

---

## 9) Scelte di robustezza applicate

- Regole business centralizzate nei service, non nel frontend.
- Eccezioni di dominio per errori controllati.
- Error mapping REST consistente.
- Coerenza dati su scambi/recensioni con validazioni server-side.
- Flussi UI guidati da permessi/flag calcolati dal backend.

---

## 10) Sezione dedicata - Scelte implementative

Questa sezione raccoglie in modo esplicito le principali scelte implementative adottate durante lo sviluppo, con il razionale tecnico e l'impatto sul progetto.

### 10.1 Scelta architetturale: backend unico riusabile
- Abbiamo scelto di concentrare le regole di business nei service Java, mantenendo CLI e Web come soli layer di presentazione.
- Questo ha evitato duplicazione logica tra interfacce diverse.
- Impatto: evoluzione piu rapida verso il Web e minore rischio di comportamenti incoerenti.

### 10.2 Scelta modellazione: `SkillSwapState` come aggregato applicativo
- Lo stato applicativo e gestito tramite un contenitore unico in memoria (`SkillSwapState`).
- Le entita restano collegate per riferimento oggetto e non solo tramite stringhe ID.
- Impatto: flussi semplici da gestire in runtime e serializzazione CSV centralizzata.

### 10.3 Scelta matching: scoring trasparente
- `MatchingService` usa una formula chiara (skill, livello, classe) invece di criteri opachi.
- Ogni risultato e spiegabile, testabile e confrontabile.
- Impatto: migliore leggibilita didattica e debugging piu rapido.

### 10.4 Scelta workflow scambi: state machine esplicita
- `ExchangeService` implementa transizioni consentite e blocca quelle invalide.
- Sono stati aggiunti controlli di ruolo e anti-duplicati su scambi attivi.
- Impatto: prevenzione inconsistenze e UX allineata al reale stato dello scambio.

### 10.5 Scelta recensioni: vincoli forti lato backend
- `ReviewService` permette review solo su scambi completati e solo ai partecipanti.
- Una review per coppia `(exchange, reviewer)`, stelle 1..5 e update reputazione immediato.
- Impatto: ranking affidabile e protezione da feedback impropri o duplicati.

### 10.6 Scelta persistenza: CSV + astrazione storage
- `FileStorage` e usato in produzione didattica per trasparenza e semplicita.
- `InMemoryStorage` e mantenuto per test/simulazioni.
- Il caricamento CSV e ordinato per ricostruire i legami tra entita.
- Impatto: buona manutenibilita in un contesto non-DB e facilita di reset controllato.

### 10.7 Scelta integrazione Web: Spring REST + frontend statico
- L'API REST e stata costruita come adapter dei service Java esistenti.
- DTO dedicati e gestione errori centralizzata hanno stabilizzato la comunicazione client/server.
- Impatto: interfaccia moderna senza riscrivere il cuore applicativo.

### 10.8 Scelta UX/Branding: coerenza e leggibilita
- Introduzione di design system CSS, icone SVG custom, tema persistente e layout responsive.
- Progressivo hardening della UX (bacheca, login, stelle recensione, allineamenti).
- Impatto: prodotto piu usabile e presentabile, mantenendo semplicita tecnica.

---

## 11) Reset amministratore e gestione dataset

Per azzerare i dati senza compromettere il funzionamento:
1. Fermare server.
2. Fare backup di `data/`.
3. Svuotare (lasciando header):  
   `students.csv`, `offers.csv`, `requests.csv`, `exchanges.csv`, `reviews.csv`
4. Non svuotare `skills.csv`.
5. Riavviare applicazione e validare login/bacheca/ranking.

Questa procedura evita dati orfani e preserva il catalogo competenze.

---

## 12) Limiti noti e possibili evoluzioni

Limiti noti:
- storage file-based (niente transazioni DB complete),
- concorrenza limitata su CSV,
- reset dati manuale.

Evoluzioni consigliate:
- migrazione a database relazionale,
- autenticazione piu forte (token/sessione server-side),
- audit eventi e pannello admin dedicato,
- test di integrazione end-to-end automatizzati.

